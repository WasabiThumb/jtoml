/**
 * Internal abstraction for input streams and readers.
 * Streams must always use UTF-8 encoding per the
 * TOML spec, so they may be treated identically
 * for the purposes of this library.
 * @see CharSource
 */
@NullMarked
package io.github.wasabithumb.jtoml.io.source;

import org.jspecify.annotations.NullMarked;