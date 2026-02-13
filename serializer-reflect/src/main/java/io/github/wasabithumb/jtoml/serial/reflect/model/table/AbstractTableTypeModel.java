/*
 * Copyright 2025 Xavier Pedraza
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.wasabithumb.jtoml.serial.reflect.model.table;

import io.github.wasabithumb.jtoml.comment.Comment;
import io.github.wasabithumb.jtoml.comment.Comments;
import io.github.wasabithumb.jtoml.comment.MultiComment;
import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.key.convention.KeyConvention;
import io.github.wasabithumb.jtoml.serial.reflect.Convention;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
abstract class AbstractTableTypeModel<T> implements TableTypeModel<T> {

    @Contract(mutates = "param2")
    protected static void applyAnnotationComments(
            @NotNull AnnotatedElement annotated,
            @NotNull Comments comments
    ) {
        Annotation[] annotations = annotated.getDeclaredAnnotations();
        for (Annotation a : annotations) {
            Class<?> decl = a.annotationType().getDeclaringClass();
            if (decl == null) continue;
            if (Comment.class.equals(decl)) {
                if (a instanceof Comment.Pre) {
                    comments.addPre(((Comment.Pre) a).value());
                } else if (a instanceof Comment.Inline) {
                    comments.addInline(((Comment.Inline) a).value());
                } else if (a instanceof Comment.Post) {
                    comments.addPost(((Comment.Post) a).value());
                }
            } else if (MultiComment.class.equals(decl)) {
                if (a instanceof MultiComment.Pre) {
                    for (Comment.Pre pre : ((MultiComment.Pre) a).value()) {
                        comments.addPre(pre.value());
                    }
                } else if (a instanceof MultiComment.Inline) {
                    for (Comment.Inline inline : ((MultiComment.Inline) a).value()) {
                        comments.addInline(inline.value());
                    }
                } else if (a instanceof MultiComment.Post) {
                    for (Comment.Post post : ((MultiComment.Post) a).value()) {
                        comments.addPost(post.value());
                    }
                }
            }
        }
    }

    //

    protected static abstract class AbstractKey implements Key {

        @Override
        public int hashCode() {
            return this.asTomlKey().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Key)) return false;
            return this.asTomlKey().equals(((Key) obj).asTomlKey());
        }

        @Override
        public String toString() {
            return this.asTomlKey().toString();
        }

    }

    protected static abstract class MemberKey<M extends Member & AnnotatedElement> extends AbstractKey {

        protected final M member;
        protected final KeyConvention defaultConvention;

        MemberKey(
                @NotNull M member,
                @NotNull KeyConvention defaultConvention
        ) {
            this.member = member;
            this.defaultConvention = defaultConvention;
        }

        //

        @Override
        public @NotNull TomlKey asTomlKey() {
            io.github.wasabithumb.jtoml.serial.reflect.Key explicitAnnotation = this.member
                    .getDeclaredAnnotation(io.github.wasabithumb.jtoml.serial.reflect.Key.class);
            if (explicitAnnotation != null) return TomlKey.literal(explicitAnnotation.value());

            KeyConvention convention = determineConvention(this.member, this.defaultConvention);
            return convention.toToml(this.member.getName());
        }

        //

        private static <M extends Member & AnnotatedElement> @NotNull KeyConvention determineConvention(
                @NotNull M member,
                @NotNull KeyConvention defaultConvention
        ) {
            Class<?> declaringClass = member.getDeclaringClass();
            Annotation[] a = member.getDeclaredAnnotations();
            Annotation[] b = declaringClass.getDeclaredAnnotations();
            final int al = a.length;
            final int tl = al + b.length;

            for (int i = 0; i < tl; i++) {
                Annotation next = i < al ? a[i] : b[i - al];
                if (next instanceof Convention) return ((Convention) next).value();
                Class<? extends Annotation> annotationType = next.annotationType();
                if (!Convention.class.equals(annotationType.getDeclaringClass())) continue;
                return resolveConventionHelperAnnotation(annotationType);
            }

            return defaultConvention;
        }

        private static @NotNull KeyConvention resolveConventionHelperAnnotation(Class<? extends Annotation> type) {
            Field field;
            try {
                field = type.getDeclaredField("VALUE");
            } catch (NoSuchFieldException e) {
                throw new IllegalStateException("Convention helper annotation " +
                        type.getName() + " is missing required static field VALUE", e);
            }
            if (!KeyConvention.class.isAssignableFrom(field.getType())) {
                throw new IllegalStateException("VALUE field for convention helper annotation " +
                        type.getName() + " is not of type KeyConvention (is " + field.getType().getName() + ")");
            }
            KeyConvention value;
            try {
                value = (KeyConvention) field.get(null);
            } catch (Exception e) {
                throw new IllegalStateException(
                        "Failed to read VALUE field on convention helper annotation " + type.getName(),
                        e
                );
            }
            return value;
        }

    }

    protected static final class FixedMapper implements Mapper {

        private final Map<TomlKey, Key> map;

        FixedMapper(
                @NotNull TableTypeModel<?> model,
                @NotNull Collection<? extends Key> keys
        ) {
            this.map = buildMap(model, keys);
        }

        //

        @Override
        public @NotNull Map<TomlKey, Key> universe() {
            return this.map;
        }

        @Override
        public @Nullable Key fromTomlKey(@NotNull TomlKey key) {
            return this.map.get(key);
        }

        //

        private static @NotNull Map<TomlKey, Key> buildMap(
                @NotNull TableTypeModel<?> model,
                @NotNull Collection<? extends Key> keys
        ) {
            Map<TomlKey, Key> ret = new HashMap<>(keys.size());
            for (Key key : keys) {
                TomlKey tomlKey = key.asTomlKey();
                Key existing = ret.put(tomlKey, key);
                if (existing == null) continue;
                throw new IllegalStateException(
                        "Serializable type " + model.type().getName() +
                                " declares duplicate key " + tomlKey
                );
            }
            return Collections.unmodifiableMap(ret);
        }

    }

}
