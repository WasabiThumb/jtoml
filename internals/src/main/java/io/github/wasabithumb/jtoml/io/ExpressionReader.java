package io.github.wasabithumb.jtoml.io;

import io.github.wasabithumb.jtoml.JToml;
import io.github.wasabithumb.jtoml.comment.Comments;
import io.github.wasabithumb.jtoml.except.TomlException;
import io.github.wasabithumb.jtoml.except.parse.TomlDateTimeException;
import io.github.wasabithumb.jtoml.expression.Expression;
import io.github.wasabithumb.jtoml.io.source.BufferedCharSource;
import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.option.JTomlOption;
import io.github.wasabithumb.jtoml.option.JTomlOptions;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.TomlValueFlags;
import io.github.wasabithumb.jtoml.value.UnsafePrimitives;
import io.github.wasabithumb.jtoml.value.array.TomlArray;
import io.github.wasabithumb.jtoml.value.primitive.TomlPrimitive;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.io.Closeable;
import java.time.*;
import java.util.LinkedList;
import java.util.List;

public class ExpressionReader implements Closeable {

    protected final BufferedCharSource in;
    protected final JTomlOptions options;

    public ExpressionReader(@NotNull BufferedCharSource in, @NotNull JTomlOptions options) {
        this.in = in;
        this.options = options;
    }

    //

    public @Nullable Expression readExpression() throws TomlException {
        if (!this.in.skipWhitespace()) return null;
        char c0 = this.in.nextChar();
        Expression ret;

        if (c0 == '\r') {        // newline (CRLF)
            if (this.in.next() != '\n') this.in.raise("Carriage return without matching newline");
            return Expression.empty();
        } else if (c0 == '\n') { // newline (LF)
            return Expression.empty();
        } else if (c0 == '#') {  // comment
            ret = Expression.empty();
            ret.setComment(this.in.finishExpression(true));
            return ret;
        } else if (c0 == '[') {  // std table or array table
            boolean isArray = this.in.peek() == '[';
            if (isArray) this.in.next();
            if (!this.in.skipWhitespace()) this.in.raise("Incomplete table header");
            TomlKey key = this.readKey(new StringBuilder(), ']', '\0');
            if (isArray && this.in.next() != ']') this.in.raise("Missing 2nd closing bracket for array table");
            ret = Expression.table(key, isArray);
        } else {                 // key-values
            StringBuilder sb = new StringBuilder();
            sb.append(c0);
            TomlKey key = this.readKey(sb, '=', (c0 == '\"' || c0 == '\'') ? c0 : '\0');
            if (!this.in.skipWhitespace()) this.in.raise("Expected value, got EOF");
            TomlValue value = this.readValue();
            ret = Expression.keyValue(key, value);
        }

        ret.setComment(this.in.finishExpression(false));
        return ret;
    }

    @Override
    public void close() throws TomlException {
        this.in.close();
    }

    //

    /**
     * Reads a key. If the first character of the key has already been read,
     * "buf" should contain that character and "quot" should be set appropriately
     * @param buf A buffer containing at most 1 character to hold the key characters while parsing
     * @param terminatedBy The character which, if present and unescaped, signals the end of the key
     * @param quot The escape delimiter currently in use ({@code "} or {@code '}), or {@code \0} if not escaped
     */
    @Contract(mutates = "this, param1")
    private @NotNull TomlKey readKey(
            @NotNull StringBuilder buf,
            int terminatedBy,
            char quot
    ) throws TomlException {
        boolean escaped = false;
        int next;
        char c;

        while (true) {
            next = this.in.next();
            if (next == -1) this.in.raise("Encountered EOF while reading key");
            if (next == '\r' || next == '\n') this.in.raise("Invalid newline within key");
            if (quot == '\0') {
                if (next == terminatedBy) {
                    this.stripBareWhitespace(buf);
                    try {
                        return TomlKey.parse(buf);
                    } catch (IllegalArgumentException e) {
                        this.in.raise("Invalid key", e);
                    }
                }
            }
            c = (char) next;
            if (escaped) {
                escaped = false;
                buf.append(c);
                int n = (c == 'U') ? 8 : ((c == 'u') ? 4 : 0);
                for (int z=0; z < n; z++) {
                    next = this.in.next();
                    if (next == -1) this.in.raise("Incomplete escape sequence");
                    buf.append((char) next);
                }
                continue;
            }
            if (c == '\\') {
                if (quot == '"') {
                    escaped = true;
                } else if (quot == '\0') {
                    this.in.raise("Disallowed escape sequence");
                }
            } else if (quot == '\0') {
                if (c == '\0') {
                    this.in.raise("Disallowed NULL character in bare key");
                } else if (c == '"' || c == '\'') {
                    quot = c;
                } else if (c == ' ' || c == '\t') {
                    // Convert whitespace in bare sections to NULL, this
                    // is useful for distinguishing between escaped whitespace
                    // within stripBareWhitespace
                    c = '\0';
                }
            } else if (c == quot) {
                quot = '\0';
            }
            buf.append(c);
        }
    }

    private void stripBareWhitespace(@NotNull StringBuilder sb) throws TomlException {
        int trimStart = 0;

        // Trim leading whitespace
        while (true) {
            if (trimStart >= sb.length()) this.in.raise("Empty key");
            if (sb.charAt(trimStart) == '\0') {
                trimStart++;
            } else {
                break;
            }
        }
        if (trimStart != 0) {
            int rem = sb.length() - trimStart;
            for (int i = 0; i < rem; i++) {
                sb.setCharAt(i, sb.charAt(trimStart + i));
            }
            sb.setLength(rem);
        }

        // Trim trailing whitespace
        trimStart = sb.length();
        while (true) {
            if (sb.charAt(trimStart - 1) != '\0') {
                sb.setLength(trimStart);
                break;
            }
            trimStart--;
        }

        // Ensure that all other whitespace is neighbored
        // by a separator (.) and then trim it
        int head = 1;
        char c;

        while (head < sb.length()) {
            c = sb.charAt(head);
            if (c != '\0') {
                head++;
                continue;
            }

            boolean neighbored = sb.charAt(head - 1) == '.';
            int end = head + 1;
            while (end < sb.length()) {
                c = sb.charAt(end);
                if (c != '\0') {
                    if (c == '.') neighbored = true;
                    break;
                }
                end++;
            }

            if (!neighbored)
                this.in.raise("Disallowed whitespace in bare key");

            int rem = sb.length() - end;
            for (int z=0; z < rem; z++) {
                sb.setCharAt(head + z, sb.charAt(end + z));
            }
            sb.setLength(head + rem);
        }
    }

    private @NotNull TomlValue readValue() throws TomlException {
        return this.readValue(-1);
    }

    private @NotNull TomlValue readValue(int firstIfKnown) throws TomlException {
        char c0 = (firstIfKnown != -1) ? ((char) firstIfKnown) : this.in.nextChar();

        if (c0 == '"')              return this.readBasicString();
        if (c0 == '\'')             return this.readLiteralString();
        if (c0 == 't' || c0 == 'f') return this.readBoolean();
        if (c0 == '[')              return this.readArray();
        if (c0 == '{')              return this.readInlineTable();

        // Collect chars; the remaining formats are
        // hard to distinguish by first few chars.
        // Mode constants:
        // 0 : Unknown (resolve to Integer)
        // 1 : Unknown non-integer (resolve to Float)
        // 2 : Integer
        // 3 : Float
        // 4 : Date/Time
        int mode = 0;
        StringBuilder sb = new StringBuilder();
        int next = c0;
        boolean firstChar = true;

        do {
            boolean valid = true;
            do {
                // Present in all types for one reason or another
                if ('0' <= next && next <= '9') break;
                if (next == '+' || next == '-') break;
                if (next == ' ') break;

                // Integer specific chars
                if (mode == 2) {
                    if ('A' <= next && next <= 'F') continue; // Hexadecimal
                    if ('a' <= next && next <= 'f') continue; // Hexadecimal
                }
                if (mode == 0 || mode == 2) {
                    if (next == 'x' ||     // Hexadecimal
                            next == 'o' || // Octal
                            next == 'b'    // Binary
                    ) {
                        mode = 2;
                        break;
                    }
                    if (next == '_') break;
                }

                // Float specific chars
                if (mode == 0 || mode == 1 || mode == 3) {
                    if (next == 'e' || next == 'E' ||                                // Exponent
                            next == 'i' || next == 'n' || next == 'f' || next == 'a' // inf & nan
                    ) {
                        mode = 3;
                        break;
                    }
                    if (next == '.') {
                        if (mode == 0) mode = 1;
                        break;
                    }
                    if (next == '_') break;
                }

                // Datetime specific chars
                if (mode == 0 || mode == 1 || mode == 4) {
                    if (next == 'T' || next == 't' ||      // Time delimiter
                            next == 'Z' || next == 'z' ||                 // Zone suffix
                            next == ':'                                   // Time separator
                    ) {
                        mode = 4;
                        break;
                    }
                    if (next == '.') break;
                }

                valid = false;
            } while (false);

            if (!valid) {
                if (firstChar) this.in.raise("Illegal character for primitive value");
                // If the char is not valid within the wider context, the error will be caught
                // later by #finishExpression
                break;
            }

            if (firstChar) {
                sb.append((char) next);
                firstChar = false;
            } else {
                sb.append(this.in.nextChar());
            }
            next = this.in.peek();
        } while (next != -1);

        // Special case for local date
        if (mode == 0 && sb.length() > 4 && sb.charAt(4) == '-') mode = 4;

        // Trim trailing whitespace
        int len = sb.length();
        while (len > 0 && sb.charAt(len - 1) == ' ') len--;
        sb.setLength(len);

        // The content and inferred type has been collected,
        // defer to individual parsers
        if (mode == 0 || mode == 2) {        // Integer
            return this.parseInteger(sb);
        } else if (mode == 1 || mode == 3) { // Float
            return this.parseFloat(sb);
        } else {                             // DateTime
            try {
                return this.parseDateTime(sb);
            } catch (DateTimeException e) {
                throw new TomlDateTimeException(e);
            }
        }
    }

    private @NotNull TomlPrimitive parseInteger(@NotNull CharSequence str) throws TomlException {
        final int len = str.length();
        if (len == 0) this.in.raise("Cannot parse empty sequence as integer");
        long n = 0L;

        char specifier = '\0';
        if (len > 2 && str.charAt(0) == '0') {
            specifier = str.charAt(1);
        }

        char d;
        int v;
        if (specifier == 'x') {
            // Hexadecimal
            for (int i=2; i < len; i++) {
                d = str.charAt(i);
                if ('0' <= d && d <= '9') {
                    v = (d - '0');
                } else if ('A' <= d && d <= 'F') {
                    v = (d - 'A' + 10);
                } else if ('a' <= d && d <= 'f') {
                    v = (d - 'a' + 10);
                } else if (d == '_') {
                    if (i == 2 || i == (len - 1) || str.charAt(i - 1) == '_')
                        this.in.raise("Illegal underscore placement");
                    continue;
                } else {
                    this.in.raise("Invalid char for hexadecimal integer");
                    break;
                }
                if (Long.numberOfLeadingZeros(n) < 4) this.in.raise("Integer is too large");
                n = (n << 4L) | ((long) v);
            }
        } else if (specifier == 'o') {
            // Octal
            for (int i=2; i < len; i++) {
                d = str.charAt(i);
                if ('0' <= d && d <= '7') {
                    v = (d - '0');
                } else if (d == '_') {
                    if (i == 2 || i == (len - 1) || str.charAt(i - 1) == '_')
                        this.in.raise("Illegal underscore placement");
                    continue;
                } else {
                    this.in.raise("Invalid char for octal integer");
                    break;
                }
                if (Long.numberOfLeadingZeros(n) < 3) this.in.raise("Integer is too large");
                n = (n << 3L) | ((long) v);
            }
        } else if (specifier == 'b') {
            // Binary
            for (int i=2; i < len; i++) {
                d = str.charAt(i);
                if (d == '0') {
                    v = 0;
                } else if (d == '1') {
                    v = 1;
                } else if (d == '_') {
                    if (i == 2 || i == (len - 1) || str.charAt(i - 1) == '_')
                        this.in.raise("Illegal underscore placement");
                    continue;
                } else {
                    this.in.raise("Invalid char for binary integer");
                    break;
                }
                if ((n & Long.MIN_VALUE) != 0L) this.in.raise("Integer is too large");
                n = (n << 1L) | ((long) v);
            }
        } else {
            // Decimal
            boolean negative = false;
            int start = 0;
            d = str.charAt(0);
            if (d == '+') {
                start = 1;
            } else if (d == '-') {
                negative = true;
                start = 1;
            }
            if (start == len) this.in.raise("Expected decimal digits after sign");

            for (int i=start; i < len; i++) {
                d = str.charAt(i);
                if (d == '0') {
                    if (i == start && i != (len - 1))
                        this.in.raise("Disallowed leading 0 in decimal integer");
                    v = 0;
                } else if ('0' < d && d <= '9') {
                    v = (d - '0');
                } else if (d == '_') {
                    if (i == start || i == (len - 1) || str.charAt(i - 1) == '_')
                        this.in.raise("Illegal underscore placement");
                    continue;
                } else {
                    this.in.raise("Invalid char for decimal integer");
                    break;
                }
                try {
                    n = Math.multiplyExact(n, 10L);
                    n = Math.addExact(n, negative ? (-v) : v);
                } catch (ArithmeticException e) {
                    this.in.raise("Integer is too large", e);
                }
            }
        }

        return TomlPrimitive.of(n);
    }

    private @NotNull TomlPrimitive parseFloat(@NotNull CharSequence str) throws TomlException {
        final int len = str.length();
        if (len == 0) this.in.raise("Cannot parse empty sequence as float");
        char c;

        // Handle sign
        boolean negative = false;
        int head = 0;
        c = str.charAt(0);
        if (c == '+') {
            head = 1;
        } else if (c == '-') {
            negative = true;
            head = 1;
        }
        if (head == len) this.in.raise("Expected float after sign");

        // Handle special float (inf & nan)
        int rem = len - head;
        if (rem == 3) {
            char c0 = str.charAt(head);
            char c1 = str.charAt(head + 1);
            char c2 = str.charAt(head + 2);
            if (c0 == 'i' && c1 == 'n' && c2 == 'f') {
                return negative ? UnsafePrimitives.createFloat(Double.NEGATIVE_INFINITY, "-inf") :
                        UnsafePrimitives.createFloat(Double.POSITIVE_INFINITY, "inf");
            } else if (c0 == 'n' && c1 == 'a' && c2 == 'n') {
                return UnsafePrimitives.createFloat(Double.NaN, "nan");
            }
        }

        // Read integer part
        long ip;
        c = str.charAt(head++);
        boolean leadsWithZero;
        if (c == '0') {
            leadsWithZero = true;
            ip = 0;
        } else {
            if (c < '1' || c > '9')
                this.in.raise("Invalid integer part");
            leadsWithZero = false;
            ip = (c - '0');
        }
        while (head < len) {
            c = str.charAt(head);
            if (c == '_') {
                head++;
                if (head >= len || (c = str.charAt(head)) < '0' || c > '9')
                    this.in.raise("Illegal underscore placement");
                continue;
            }
            if (c < '0' || c > '9') break;
            if (leadsWithZero) this.in.raise("Illegal leading zero in float");
            head++;
            try {
                ip = Math.multiplyExact(ip, 10L);
                ip = Math.addExact(ip, (c - '0'));
            } catch (ArithmeticException e) {
                this.in.raise("Integer part is too large", e);
            }
        }

        if (head >= len) this.in.raise("Expected decimal point or exponent");
        double frac = 0d;
        long exp = 0;
        boolean none = true;
        c = str.charAt(head++);

        // Read fractional part
        if (c == '.') {
            StringBuilder buf = new StringBuilder("0.");
            none = false;
            if (head >= len) this.in.raise("Expected digits after decimal point");
            c = str.charAt(head++);
            if (c < '0' || c > '9') this.in.raise("Invalid fractional part");
            buf.append(c);
            while (head < len) {
                c = str.charAt(head++);
                if (c == '_') {
                    if (head >= len || (c = str.charAt(head)) < '0' || c > '9')
                        this.in.raise("Illegal underscore placement");
                    continue;
                }
                if (c < '0' || c > '9') break;
                buf.append(c);
            }
            frac = Double.parseDouble(buf.toString());
        }

        // Read exponent
        if (c == 'e' || c == 'E') {
            boolean exponentNegative = false;
            if (head >= len) this.in.raise("Expected decimal after exponent");
            c = str.charAt(head++);
            if (c == '+') {
                if (head >= len) this.in.raise("Expected digits after sign");
                c = str.charAt(head++);
            } else if (c == '-') {
                if (head >= len) this.in.raise("Expected digits after sign");
                c = str.charAt(head++);
                exponentNegative = true;
            }
            boolean first = true;
            while (true) {
                if (c == '_') {
                    if (first || head >= len || (c = str.charAt(head++)) < '0' || c > '9')
                        this.in.raise("Illegal underscore placement");
                    continue;
                }
                first = false;
                if (c < '0' || c > '9') break;
                try {
                    exp = Math.multiplyExact(exp, 10L);
                    exp = Math.addExact(exp, exponentNegative ? ('0' - c) : (c - '0'));
                } catch (ArithmeticException e) {
                    this.in.raise("Exponent is too large", e);
                }
                if (head >= len) break;
                c = str.charAt(head++);
            }
        } else if (none) {
            this.in.raise("Expected decimal point or exponent");
        }

        if (head < len) {
            this.in.raise("Unprocessable characters in float");
        }

        double d;
        if (exp != 0) {
            double scale = Math.pow(10, exp);
            d = (scale * ip) + (scale * frac);
        } else {
            d = ((double) ip) + frac;
        }
        if (negative) d = -d;
        return UnsafePrimitives.createFloat(d, str.toString());
    }

    private @NotNull TomlPrimitive parseDateTime(@NotNull CharSequence str) throws TomlException, DateTimeException {
        final int len = str.length();
        boolean truncated = false;

        if (len < 5) {
            truncated = true;
        } else if (str.charAt(2) == ':') { // Local Time
            return TomlPrimitive.of(this.parsePartialTime(str, 0, len), this.options.get(JTomlOption.TIME_ZONE));
        } else if (len < 8) {
            truncated = true;
        }

        if (truncated)
            this.in.raise("Datetime sequence is too short");

        if (len < 10 || str.charAt(4) != '-' || str.charAt(7) != '-')
            this.in.raise("Invalid date");

        final int year = this.parseNDigits(str, 0, 4);
        final int month = this.parseNDigits(str, 5, 2);
        final int day = this.parseNDigits(str, 8, 2);
        if (month < 1 || month > 12) this.in.raise("Month out of range (got " + month + ")");
        if (day < 1 || day > 31) this.in.raise("Day out of range (got " + day + ")");
        final LocalDate date = LocalDate.of(year, month, day);

        if (len == 10) { // Local Date
            return TomlPrimitive.of(date, this.options.get(JTomlOption.TIME_ZONE));
        }

        char delim = str.charAt(10);
        if (delim != 'T' && delim != 't' && delim != ' ') this.in.raise("Expected time delimiter");

        int whereOffset = -1;
        boolean numOffset = false;
        for (int i=11; i < len; i++) {
            char c = str.charAt(i);
            if (c == 'Z' || c == 'z') {
                whereOffset = i;
                break;
            } else if (c == '+' || c == '-') {
                whereOffset = i;
                numOffset = true;
                break;
            }
        }

        if (whereOffset == -1) {                                                        // Local Date-Time
            LocalTime time = this.parsePartialTime(str, 11, len - 11);
            return TomlPrimitive.of(LocalDateTime.of(date, time), this.options.get(JTomlOption.TIME_ZONE));
        } else {                                                                        // Offset Date-Time
            LocalTime time = this.parsePartialTime(str, 11, whereOffset - 11);
            LocalDateTime dateTime = LocalDateTime.of(date, time);
            ZoneOffset offset;
            if (numOffset) {
                int rem = len - whereOffset;
                boolean negative;
                if (rem < 6 || (!(negative = (str.charAt(whereOffset) == '-')) && str.charAt(whereOffset) != '+')) {
                    this.in.raise("Invalid time offset");
                    return null;
                }
                int hour = this.parseNDigits(str, whereOffset + 1, 2);
                int minute = this.parseNDigits(str, whereOffset + 4, 2);
                if (negative) hour = -hour;
                if (hour < -18 || hour > 18) this.in.raise("Offset hour out of range (got " + hour + ")");
                if (minute < 0 || minute > 59) this.in.raise("Offset minute out of range (got " + minute + ")");
                offset = ZoneOffset.ofHoursMinutes(hour, (hour < 0) ? -minute : minute);
            } else {
                offset = ZoneOffset.UTC;
            }
            return TomlPrimitive.of(dateTime.atOffset(offset));
        }
    }

    private @NotNull LocalTime parsePartialTime(@NotNull CharSequence str, int off, int len) throws TomlException {
        // v1.1.0 - support datetimes without seconds
        boolean ignoreSeconds = false;
        if (len == 5 && this.options.get(JTomlOption.COMPLIANCE).isAtLeast(1, 1)) {
            ignoreSeconds = true;
        } else {
            if (len < 8) this.in.raise("Partial time sequence is too short");
            if (str.charAt(off + 5) != ':') this.in.raise("Missing time separator after minutes");
        }
        if (str.charAt(off + 2) != ':') this.in.raise("Missing time separator after hours");

        final int hour = this.parseNDigits(str, off, 2);
        final int minute = this.parseNDigits(str, off + 3, 2);
        final int second = ignoreSeconds ? 0 : this.parseNDigits(str, off + 6, 2);

        if (hour < 0 || hour > 23) this.in.raise("Hour out of range (got " + hour + ")");
        if (minute < 0 || minute > 59) this.in.raise("Minute out of range (got " + minute + ")");
        if (second < 0 || second > 59) this.in.raise("Second out of range (got " + second + ")");

        int nanos = 0;
        if (len > 8) {
            char c = str.charAt(off + 8);
            if (c != '.') this.in.raise("Expected decimal point");
            if (len < 10) this.in.raise("Expected digits after decimal point");
            int nd = len - 9;
            if (nd <= 9) {
                nanos = this.parseNDigits(str, off + 9, nd);
                switch (nd) {
                    case 1: nanos *= 100000000; break;
                    case 2: nanos *= 10000000; break;
                    case 3: nanos *= 1000000; break;
                    case 4: nanos *= 100000; break;
                    case 5: nanos *= 10000; break;
                    case 6: nanos *= 1000; break;
                    case 7: nanos *= 100; break;
                    case 8: nanos *= 10; break;
                }
            } else {
                nanos = this.parseNDigits(str, off + 9, 9);
                for (int i=18; i < len; i++) {
                    c = str.charAt(off + i);
                    if (c < '0' || c > '9') this.in.raise("Expected digit");
                }
            }
        }

        return LocalTime.of(hour, minute, second, nanos);
    }

    private int parseNDigits(@NotNull CharSequence str, int off, int len) throws TomlException {
        int d = 0;
        char c;
        for (int i=0; i < len; i++) {
            c = str.charAt(off + i);
            if (c < '0' || c > '9') this.in.raise("Invalid n-digit value");
            d = (d * 10) + (c - '0');
        }
        return d;
    }

    private int openString(char quot) throws TomlException {
        int p0 = this.in.peek();
        if (p0 == -1) this.in.raise("Unclosed string");
        int p1 = this.in.peek();
        if (p1 == -1) {
            if (p0 != quot) this.in.raise("Unclosed string");
            return 0;
        }
        if (p0 == quot && p1 == quot) {
            this.in.next();
            this.in.next();
            return 2;
        }
        return 1;
    }

    private @NotNull TomlPrimitive readBasicString() throws TomlException {
        switch (this.openString('"')) {
            case 0:
                return TomlPrimitive.of("");
            case 2:
                return this.readMultilineBasicString();
        }

        StringBuilder sb = new StringBuilder();
        int next;

        while (true) {
            next = this.in.next();
            if (next == -1) this.in.raise("Unclosed basic string");
            if (next == '\\') {
                this.readEscapeSequence(sb);
                continue;
            }
            if (next == '"') return TomlPrimitive.of(sb.toString());
            if ((next < ' ' && next != '\t') || (next == (char) 0x7F)) {
                this.in.raise("Disallowed control character in basic string");
            }
            sb.append((char) next);
        }
    }

    private @NotNull TomlPrimitive readMultilineBasicString() throws TomlException {
        StringBuilder sb = new StringBuilder();

        // Skip leading newline
        int next = this.in.next();
        if (next == '\r') {
            if (this.in.next() != '\n') this.in.raise("Carriage return without matching line feed");
            next = this.in.next();
        } else if (next == '\n') {
            next = this.in.next();
        }
        boolean trimming = false;

        while (true) {
            if (next == -1) this.in.raise("Unclosed basic string");
            if (next == '\r') {
                next = this.in.next();
                if (next != '\n') this.in.raise("Carriage return without matching line feed");
                if (!trimming) sb.append(this.options.get(JTomlOption.LINE_SEPARATOR));
            } else if (next == '\n') {
                if (!trimming) sb.append(this.options.get(JTomlOption.LINE_SEPARATOR));
            } else if (next == '\\') {
                trimming = false;
                int pk = this.in.peek();
                while (pk == '\t' || pk == ' ') {
                    trimming = true;
                    this.in.next();
                    pk = this.in.peek();
                }
                if (pk == '\r') {
                    trimming = true;
                    this.in.next();
                    pk = this.in.next();
                    if (pk != '\n') this.in.raise("Carriage return without matching line feed");
                } else if (pk == '\n') {
                    trimming = true;
                    this.in.next();
                } else if (trimming) {
                    this.in.raise("Dangling escape character in multiline basic string");
                }
                if (!trimming) this.readEscapeSequence(sb);
            } else if (next == '"') {
                int pk = this.in.peek();
                if (pk != '\"') {
                    sb.append((char) next);
                } else {
                    this.in.next();
                    pk = this.in.peek();
                    if (pk != '\"') {
                        sb.append("\"\"");
                    } else {
                        this.in.next();
                        int extra = 0;
                        while (this.in.peek() == '\"') {
                            this.in.next();
                            if ((++extra) == 3)
                                this.in.raise("Too many closing quotes for multiline basic string");
                            sb.append('\"');
                        }
                        return TomlPrimitive.of(sb.toString());
                    }
                }
            } else if (next == ' ' || next == '\t') {
                if (!trimming) sb.append((char) next);
            } else if (next < ' ' || next == 0x7F) {
                this.in.raise("Disallowed control character in basic string");
            } else {
                trimming = false;
                sb.append((char) next);
            }
            next = this.in.next();
        }
    }

    private void readEscapeSequence(@NotNull StringBuilder dest) throws TomlException {
        int c = this.in.next();
        if (c == -1) this.in.raise("Truncated escape sequence");
        int uc = 4;
        boolean valid = true;

        switch (c) {
            case '"':  dest.append('"'); break;
            case '\\': dest.append('\\'); break;
            case 'b':  dest.append('\b'); break;
            case 'f':  dest.append('\f'); break;
            case 'n':  dest.append('\n'); break;
            case 'r':  dest.append('\r'); break;
            case 't':  dest.append('\t'); break;
            case 'x':
                // v1.1.0 - support \x
                if (!this.options.get(JTomlOption.COMPLIANCE).isAtLeast(1, 1)) {
                    this.raiseInvalidEscapeSequence();
                }
                uc = 2;
            case 'U':
                if (uc == 4) uc = 8;
            case 'u':
                int v = 0;
                int n;
                for (int i=0; i < uc; i++) {
                    c = this.in.next();
                    if (c == -1) this.in.raise("Truncated unicode escape sequence");
                    n = Character.digit(c, 16);
                    if (n == -1) this.in.raise("Invalid character in unicode escape sequence");
                    v = (v << 4) | n;
                }
                if (0xD800 <= v && v <= 0xDFFF)
                    this.in.raise("Non-scalar unicode codepoint");
                if (uc == 8) {
                    try {
                        dest.append(Character.toChars(v));
                    } catch (IllegalArgumentException e) {
                        this.in.raise("Invalid unicode codepoint", e);
                    }
                } else {
                    dest.append((char) v);
                }
                break;
            case 'e':
                // v1.1.0 - support \e as an escape sequence for ESC
                if (!this.options.get(JTomlOption.COMPLIANCE).isAtLeast(1, 1)) {
                    this.raiseInvalidEscapeSequence();
                }
                dest.append('\u001b');
                break;
            default:
                this.raiseInvalidEscapeSequence();
        }
    }

    @Contract("-> fail")
    private void raiseInvalidEscapeSequence() throws TomlException {
        this.in.raise("Invalid escape sequence character");
    }

    private @NotNull TomlPrimitive readLiteralString() throws TomlException {
        switch (this.openString('\'')) {
            case 0:
                return TomlPrimitive.of("");
            case 2:
                return this.readMultilineLiteralString();
        }

        StringBuilder sb = new StringBuilder();
        int next;

        while (true) {
            next = this.in.next();
            if (next == -1) this.in.raise("Unclosed literal string");
            if (next == '\'') return TomlPrimitive.of(sb.toString());
            if ((next < ' ' && next != '\t') || (next == (char) 0x7F)) {
                this.in.raise("Disallowed control character in literal string");
            }
            sb.append((char) next);
        }
    }

    private @NotNull TomlPrimitive readMultilineLiteralString() throws TomlException {
        StringBuilder sb = new StringBuilder();

        // Skip leading newline
        int next = this.in.next();
        if (next == '\r') {
            if (this.in.next() != '\n') this.in.raise("Carriage return without matching line feed");
            next = this.in.next();
        } else if (next == '\n') {
            next = this.in.next();
        }

        while (true) {
            if (next == -1) this.in.raise("Unclosed literal string");
            if (next == '\r') {
                next = this.in.next();
                if (next != '\n') this.in.raise("Carriage return without matching line feed");
                sb.append(this.options.get(JTomlOption.LINE_SEPARATOR));
            } else if (next == '\n') {
                sb.append(this.options.get(JTomlOption.LINE_SEPARATOR));
            } else if ((next < ' ' && next != '\t') || (next == (char) 0x7F)) {
                this.in.raise("Disallowed control character in literal string");
            } else {
                sb.append((char) next);
                if (next == '\'') {
                    int cl = sb.length();
                    if (cl >= 3 && sb.charAt(cl - 2) == '\'' && sb.charAt(cl - 3) == '\'') {
                        sb.setLength(cl - 3);
                        int extra = 0;
                        while (this.in.peek() == '\'') {
                            this.in.next();
                            if ((++extra) == 3)
                                this.in.raise("Too many closing quotes for multiline literal string");
                            sb.append('\'');
                        }
                        return TomlPrimitive.of(sb.toString());
                    }
                }
            }
            next = this.in.next();
        }
    }

    private @NotNull TomlPrimitive readBoolean() throws TomlException {
        char[] n3 = new char[3];
        if (this.in.next(n3) == 3) {
            if (n3[0] == 'r' && n3[1] == 'u' && n3[2] == 'e') {
                return TomlPrimitive.of(true);
            } else if (n3[0] == 'a' && n3[1] == 'l' && n3[2] == 's' && this.in.next() == 'e') {
                return TomlPrimitive.of(false);
            }
        }
        this.in.raise("Illegal boolean value");
        return null;
    }

    private @NotNull TomlTable readInlineTable() throws TomlException {
        TomlTable ret = TomlTable.create();
        boolean expectComma = false;
        int ctrl;
        char c;

        while (true) {
            ctrl = this.readInlineTableControl();
            if (ctrl == -1) this.in.raise("Unclosed inline table");
            c = (char) ctrl;
            if (c == '}') return ret;
            if (expectComma) {
                if (c != ',') this.in.raise("Expected inline table separator or closing char");
                ctrl = this.readInlineTableControl();
                if (ctrl == -1) this.in.raise("Unclosed inline table");
                if (ctrl == '}') {
                    // v1.1.0 - allow trailing commas
                    if (this.options.get(JTomlOption.COMPLIANCE).isAtLeast(1, 1)) {
                        return ret;
                    }
                    this.in.raise("Disallowed trailing comma in inline table");
                }
                c = (char) ctrl;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(c);
            TomlKey key = this.readKey(sb, '=', (c == '"' || c == '\'') ? c : '\0');
            for (int z=1; z < key.size() + 1; z++) {
                TomlKey partialKey = key.slice(0, z);
                TomlValue existing = ret.get(partialKey);
                if (existing == null) continue;
                if (TomlValueFlags.isConstant(existing))
                    this.in.raise(key + " conflicts with previously defined key " + partialKey + " in inline table");
            }
            ctrl = this.readInlineTableControl();
            if (ctrl == -1) this.in.raise("Expected value, got EOF");
            TomlValue value = this.readValue(ctrl);
            ret.put(key, TomlValueFlags.setConstant(value, true));
            expectComma = true;
        }
    }

    private @NotNull TomlArray readArray() throws TomlException {
        final boolean readComments = this.options.get(JTomlOption.READ_COMMENTS);
        TomlArray ret = TomlArray.create();

        ArrayControl ctrl = this.readArrayControl(readComments);
        if (ctrl.character == ',') this.in.raise("Comma precedes array values");
        if (ctrl.character == ']') {
            // TODO: If there were any comments inside the array, it gets thrown out here
            // (no element to attach to). Maybe warn in the future?
            return ret;
        }

        boolean readComma;
        TomlValue next;
        while (true) {
            next = this.readValue(ctrl.character);
            if (readComments) {
                Comments nextComments = next.comments();
                for (String pre : ctrl.comments) nextComments.addPre(pre);
            }
            ret.add(next);
            ctrl = this.readArrayControl(readComments);
            if (ctrl.character == ',') {
                readComma = true;
                ctrl = this.readArrayControl(readComments);
                if (ctrl.character == ',') this.in.raise("Double comma in array");
            } else {
                readComma = false;
            }
            if (ctrl.character == ']') {
                break;
            } else if (!readComma) {
                this.in.raise("Missing array separator");
            }
        }

        // Apply trailing comments to last element
        if (readComma && !ctrl.comments.isEmpty()) {
            TomlValue last = ret.get(ret.size() - 1);
            Comments lastComments = last.comments();
            for (String post : ctrl.comments) lastComments.addPost(post);
        }

        return ret;
    }

    /** Skip specific to arrays */
    private @NotNull ArrayControl readArrayControl(boolean readComments) throws TomlException {
        List<String> comments = readComments ? new LinkedList<>() : null;
        StringBuilder commentBuffer = readComments ? new StringBuilder() : null;
        boolean inComment = false;
        int next;

        while (true) {
            next = this.in.next();
            if (next == -1) this.in.raise("Unclosed array");
            if (next == '\r') {
                next = this.in.next();
                if (next != '\n') this.in.raise("Carriage return without matching newline");
            }
            if (next == '\n') {
                if (readComments && inComment) {
                    comments.add(commentBuffer.toString());
                    commentBuffer.setLength(0);
                }
                inComment = false;
            } else if (next == '#') {
                inComment = true;
            } else if (next == ' ' || next == '\t') {
                if (readComments && inComment && commentBuffer.length() != 0)
                    commentBuffer.append((char) next);
            } else {
                if (!inComment) return new ArrayControl((char) next, comments);
                if (next < ' ' || next == 0x7F) this.in.raise("Disallowed control character in comment");
                if (readComments)
                    commentBuffer.append((char) next);
            }
        }
    }

    /** Skip specific to inline tables */
    private int readInlineTableControl() throws TomlException {
        if (this.options.get(JTomlOption.COMPLIANCE).isAtLeast(1, 1)) {
            // v1.1.0 - support newlines in inline tables
            char c;
            while (this.in.skipWhitespace()) {
                c = this.in.nextChar();
                if (c == '\r') {
                    c = this.in.nextChar();
                    if (c != '\n') this.in.raise("Expected LF after CR within inline table");
                    continue;
                }
                if (c != '\n') return c;
            }
            return -1;
        } else {
            return this.in.skipWhitespace() ? -1 : this.in.nextChar();
        }
    }

    //

    private static final class ArrayControl {

        final char character;
        final List<String> comments;

        ArrayControl(char character, @UnknownNullability List<String> comments) {
            this.character = character;
            this.comments = comments;
        }

    }

}
