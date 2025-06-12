package io.github.wasabithumb.jtoml.serial.reflect.model.table;

import io.github.wasabithumb.jtoml.comment.Comment;
import io.github.wasabithumb.jtoml.comment.Comments;
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
            if (a instanceof Comment.Pre) comments.addPre(((Comment.Pre) a).value());
            if (a instanceof Comment.Inline) comments.addInline(((Comment.Inline) a).value());
            if (a instanceof Comment.Post) comments.addPost(((Comment.Post) a).value());
        }
    }

}
