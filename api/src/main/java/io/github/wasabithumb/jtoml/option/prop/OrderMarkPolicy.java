package io.github.wasabithumb.jtoml.option.prop;

/**
 * Determines how the BOM (<a href="https://en.wikipedia.org/wiki/Byte_order_mark">byte order mark</a>)
 * should be handled when reading and writing
 */
public enum OrderMarkPolicy {
    /**
     * <ul>
     *     <li>When reading: Never attempt to read a BOM (fail if one is present)</li>
     *     <li>When writing: Never write a BOM</li>
     * </ul>
     */
    NEVER,

    /**
     * <ul>
     *     <li>When reading: Always read a BOM (fail if one is not present)</li>
     *     <li>When writing: Always write a BOM</li>
     * </ul>
     */
    ALWAYS,

    /**
     * <ul>
     *     <li>When reading: Read a BOM only if one exists</li>
     *     <li>When writing: Write a BOM only if the document was previously read and contained a BOM</li>
     * </ul>
     */
    IF_PRESENT
}
