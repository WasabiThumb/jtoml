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

package io.github.wasabithumb.jtoml.route.impl;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.comment.Comments;
import io.github.wasabithumb.jtoml.route.Sentinel;
import io.github.wasabithumb.jtoml.route.TestRoute;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.primitive.TomlPrimitiveType;
import io.github.wasabithumb.jtoml.value.table.TomlTable;

import static org.junit.jupiter.api.Assertions.*;
import static io.github.wasabithumb.jtoml.comment.Comment.*;
import static io.github.wasabithumb.jtoml.comment.Comments.comments;

public final class ReadCommentsTestRoute implements TestRoute {

    @Sentinel("comments.toml")
    private TomlTable table;

    //

    @Override
    public String displayName() {
        return "Read Comments";
    }

    @Override
    public void execute(JToml instance) {
        TomlValue innerValue = this.table.get("inner");
        assertNotNull(innerValue);
        assertTrue(innerValue.isTable());

        TomlTable inner = innerValue.asTable();
        Comments innerComments = inner.comments();
        assertEquals(
                comments(
                        pre("PRE 1"),
                        pre("PRE 2"),
                        inline("INLINE A")
                ),
                innerComments
        );

        TomlValue value = inner.get("value");
        assertNotNull(value);
        assertTrue(value.isPrimitive());
        assertEquals(TomlPrimitiveType.INTEGER, value.asPrimitive().type());

        Comments valueComments = value.comments();
        assertEquals(
                comments(
                        pre("PRE 1"),
                        pre("PRE 2"),
                        inline("INLINE B"),
                        post("POST 1"),
                        post("POST 2")
                ),
                valueComments
        );
    }

}
