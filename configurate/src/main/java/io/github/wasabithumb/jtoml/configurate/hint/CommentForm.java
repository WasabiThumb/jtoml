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

package io.github.wasabithumb.jtoml.configurate.hint;

import io.github.wasabithumb.jtoml.comment.CommentPosition;
import io.github.wasabithumb.jtoml.comment.Comments;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@ApiStatus.Internal
public final class CommentForm {

    public static final CommentForm DEFAULT = new CommentForm(Integer.MAX_VALUE, false);

    //

    private final int pre;
    private final boolean inline;

    private CommentForm(int pre, boolean inline) {
        this.pre = pre;
        this.inline = inline;
    }

    public CommentForm(@NotNull Comments comments) {
        this(comments.get(CommentPosition.PRE).size(), comments.getInline() != null);
    }

    //

    public void apply(@NotNull String @NotNull [] src, @NotNull Comments dest) {
        String line;
        for (int i = 0; i < src.length; i++) {
            line = src[i];
            if (i < this.pre) {
                dest.addPre(line);
            } else if (i == this.pre && this.inline) {
                dest.addInline(line);
            } else {
                dest.addPost(line);
            }
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.pre, this.inline);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CommentForm)) return false;
        CommentForm other = (CommentForm) obj;
        return this.pre == other.pre &&
                this.inline == other.inline;
    }

}
