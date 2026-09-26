/*
 * Copyright 2026 Xavier Pedraza
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

package io.github.wasabithumb.jtoml.except;

import org.jetbrains.annotations.ApiStatus;

import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;

/**
 * <p>
 *     Thrown when attempting to convert a wider type into a narrower TOML
 *     primitive. This cannot be thrown for parsing; only for serialization/writing
 *     and through API usage.
 * </p>
 * <p>
 *     This is currently only used for date-times, specifically when trying to
 *     wrap a Java {@link java.time.OffsetDateTime OffsetDateTime}/{@link java.time.LocalDateTime LocalDateTime}/
 *     {@link java.time.LocalDate LocalDate} which has a {@link java.time.LocalDate#getYear() year} less than
 *     0 or greater than 9999.
 * </p>
 * @since 0.4.0
 */
public final class TomlValueException extends TomlException {

    private static final long serialVersionUID = -8613996824890651456L;

    /**
     * Internal utility for ensuring dates entering JToml
     * are compatible with the TOML specification.
     * @param date An object with date information
     * @throws TomlValueException The specified date is illegal per the TOML specification
     * @throws java.time.DateTimeException A required field could not be read from the given object
     */
    @ApiStatus.Internal
    public static void checkDate(TemporalAccessor date) {
        final int year = date.get(ChronoField.YEAR);
        if (0 <= year && year <= 9999) return;
        throw new TomlValueException("Date-time " + date + " has illegal year: " + year);
    }

    //

    public TomlValueException(String message) {
        super(message);
    }

}
