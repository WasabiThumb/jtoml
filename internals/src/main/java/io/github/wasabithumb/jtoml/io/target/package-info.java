/**
 * Internal abstraction for output streams and writers.
 * Streams must always use UTF-8 encoding per the
 * TOML spec, so they may be treated identically
 * for the purposes of this library.
 * @see CharTarget
 */
@NullMarked
package io.github.wasabithumb.jtoml.io.target;

import org.jspecify.annotations.NullMarked;