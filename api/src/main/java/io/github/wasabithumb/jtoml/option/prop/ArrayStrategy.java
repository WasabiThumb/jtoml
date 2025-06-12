package io.github.wasabithumb.jtoml.option.prop;

public enum ArrayStrategy {
    /**
     * Array elements are always written on
     * the same line (comments are ignored)
     */
    SHORT,

    /**
     * Array elements are always written on
     * a new line
     */
    TALL,

    /**
     * Array elements are written on a new line
     * when any element has comments defined
     * or is a non-primitive
     */
    DYNAMIC
}
