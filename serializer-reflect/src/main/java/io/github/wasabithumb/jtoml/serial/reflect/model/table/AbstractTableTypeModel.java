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
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;

@ApiStatus.Internal
abstract class AbstractTableTypeModel<T> implements TableTypeModel<T> {

    @Contract(mutates = "param2")
    protected static void applyAnnotationComments(
            @NotNull Annotation @NotNull [] annotations,
            @NotNull Comments comments
    ) {
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

}
