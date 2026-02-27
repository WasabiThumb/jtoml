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
import io.github.wasabithumb.jtoml.serial.reflect.Defaulting;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
        private final @Nullable Annotation defaultingAnnotation;

        MemberKey(
                @NotNull M member,
                @NotNull KeyConvention defaultConvention
        ) {
            this.member = member;
            this.defaultConvention = defaultConvention;
            this.defaultingAnnotation = findDefaultingAnnotation(member);
        }

        //

        protected abstract Class<?> typeClassOf(M member);

        protected abstract @Nullable Object nonSpecificDefault(Class<?> type);

        @Override
        public boolean isDefaulting() {
            return this.defaultingAnnotation != null;
        }

        @Override
        public @Nullable Object defaultValue() throws UnsupportedOperationException {
            Annotation annotation = this.defaultingAnnotation;
            if (annotation == null) throw new UnsupportedOperationException();
            Class<?> typeClass = this.typeClassOf(this.member);

            if (annotation instanceof Defaulting.ToInt) {
                Defaulting.ToInt qual = (Defaulting.ToInt) annotation;
                if (typeClass.equals(Byte.TYPE) || typeClass.equals(Byte.class)) {
                    return (byte) qual.value();
                } else if (typeClass.equals(Short.TYPE) || typeClass.equals(Short.class)) {
                    return (short) qual.value();
                } else if (typeClass.equals(Integer.TYPE) || typeClass.equals(Integer.class)) {
                    return (int) qual.value();
                } else if (typeClass.equals(Long.TYPE) || typeClass.equals(Long.class)) {
                    return qual.value();
                } else if (typeClass.equals(Character.TYPE) || typeClass.equals(Character.class)) {
                    return (char) qual.value();
                } else {
                    throw new IllegalStateException(
                            "@Defaulting.ToInt cannot be applied to element of type " +
                            typeClass.getName() + " (" + this.member + ")"
                    );
                }
            } else if (annotation instanceof Defaulting.ToFloat) {
                Defaulting.ToFloat qual = (Defaulting.ToFloat) annotation;
                if (typeClass.equals(Float.TYPE) || typeClass.equals(Float.class)) {
                    return (float) qual.value();
                } else if (typeClass.equals(Double.TYPE) || typeClass.equals(Double.class)) {
                    return qual.value();
                } else {
                    throw new IllegalStateException(
                            "@Defaulting.ToFloat cannot be applied to element of type " +
                                    typeClass.getName() + " (" + this.member + ")"
                    );
                }
            } else if (annotation instanceof Defaulting.ToBool) {
                Defaulting.ToBool qual = (Defaulting.ToBool) annotation;
                if (typeClass.equals(Boolean.TYPE) || typeClass.equals(Boolean.class)) {
                    return qual.value();
                } else {
                    throw new IllegalStateException(
                            "@Defaulting.ToBool cannot be applied to element of type " +
                                    typeClass.getName() + " (" + this.member + ")"
                    );
                }
            } else if (annotation instanceof Defaulting.ToString) {
                Defaulting.ToString qual = (Defaulting.ToString) annotation;
                if (typeClass.equals(String.class)) {
                    return qual.value();
                } else {
                    throw new IllegalStateException(
                            "@Defaulting.ToString cannot be applied to element of type " +
                                    typeClass.getName() + " (" + this.member + ")"
                    );
                }
            }

            return this.nonSpecificDefault(typeClass);
        }

        @Override
        public @NotNull TomlKey asTomlKey() {
            io.github.wasabithumb.jtoml.serial.reflect.Key explicitAnnotation = this.member
                    .getDeclaredAnnotation(io.github.wasabithumb.jtoml.serial.reflect.Key.class);
            if (explicitAnnotation != null) return TomlKey.literal(explicitAnnotation.value());

            KeyConvention convention = determineConvention(this.member, this.defaultConvention);
            return convention.toToml(this.member.getName());
        }

        //

        private static <M extends Member & AnnotatedElement> @Nullable Annotation findDefaultingAnnotation(
                M member
        ) {
            return memberAndDeclaringClassAnnotations(member)
                    .filter((Annotation a) ->
                            a instanceof Defaulting ||
                            Defaulting.class.equals(a.annotationType().getDeclaringClass())
                    )
                    .findFirst()
                    .orElse(null);
        }

        private static <M extends Member & AnnotatedElement> @NotNull KeyConvention determineConvention(
                @NotNull M member,
                @NotNull KeyConvention defaultConvention
        ) {
            Iterator<Annotation> annotations = memberAndDeclaringClassAnnotations(member).iterator();
            while (annotations.hasNext()) {
                Annotation next = annotations.next();
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

        private static <M extends Member & AnnotatedElement> @NotNull Stream<Annotation> memberAndDeclaringClassAnnotations(
                M member
        ) {
            Class<?> declaringClass = member.getDeclaringClass();
            final Annotation[] a = member.getDeclaredAnnotations();
            final Annotation[] b = declaringClass.getDeclaredAnnotations();
            final int al = a.length;
            final int tl = al + b.length;
            return IntStream.range(0, tl)
                    .mapToObj((int i) -> i < al ? a[i] : b[i - al]);
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
