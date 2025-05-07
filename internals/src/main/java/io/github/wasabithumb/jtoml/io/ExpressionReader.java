package io.github.wasabithumb.jtoml.io;

import io.github.wasabithumb.jtoml.except.TomlException;
import io.github.wasabithumb.jtoml.expression.Expression;
import io.github.wasabithumb.jtoml.io.source.BufferedCharSource;
import io.github.wasabithumb.jtoml.key.TomlKey;
import io.github.wasabithumb.jtoml.option.JTomlOption;
import io.github.wasabithumb.jtoml.option.JTomlOptions;
import io.github.wasabithumb.jtoml.value.TomlValue;
import io.github.wasabithumb.jtoml.value.array.TomlArray;
import io.github.wasabithumb.jtoml.value.primitive.TomlPrimitive;
import io.github.wasabithumb.jtoml.value.table.TomlTable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;

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
        } else if (c0 == '#') {        // comment
            this.in.finishExpression(true);
            return Expression.empty();
        } else if (c0 == '[') { // std table or array table
            boolean isArray = this.in.peek() == '[';
            if (isArray) this.in.next();
            if (!this.in.skipWhitespace()) this.in.raise("Incomplete table header");
            TomlKey key = this.readKey(new StringBuilder(), ']', '\0');
            if (isArray && this.in.next() != ']') this.in.raise("Missing 2nd closing bracket for array table");
            ret = Expression.table(key, isArray);
        } else {                // key-values
            StringBuilder sb = new StringBuilder();
            sb.append(c0);
            TomlKey key = this.readKey(sb, '=', (c0 == '\"' || c0 == '\'') ? c0 : '\0');
            if (!this.in.skipWhitespace()) this.in.raise("Expected value, got EOF");
            TomlValue value = this.readValue();
            ret = Expression.keyValue(key, value);
        }

        this.in.finishExpression(false);
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
            if (quot == '\0') {
                if (next == terminatedBy) {
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
                if (quot != '"') this.in.raise("Disallowed escape sequence");
                escaped = true;
            } else if (quot == '\0') {
                if (c == '"' || c == '\'') {
                    quot = c;
                } else if (c == ' ' || c == '\t') {
                    continue;
                }
            } else if (c == quot) {
                quot = '\0';
            }
            buf.append(c);
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
                    if (next == 'X' || next == 'x' ||     // Hexadecimal
                            next == 'O' || next == 'o' || // Octal
                            next == 'B' || next == 'b'    // Binary
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
            return this.parseDateTime(sb);
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
        if (specifier == 'x' || specifier == 'X') {
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
        } else if (specifier == 'o' || specifier == 'O') {
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
        } else if (specifier == 'b' || specifier == 'B') {
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
                if ('0' <= d && d <= '9') {
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
                    n = Math.multiplyExact(n, 10);
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
                return TomlPrimitive.of(negative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
            } else if (c0 == 'n' && c1 == 'a' && c2 == 'n') {
                return TomlPrimitive.of(Double.NaN);
            }
        }

        // Read integer part
        long ip;
        c = str.charAt(head++);
        if (c < '0' || c > '9') this.in.raise("Invalid integer part");
        ip = (c - '0');
        while (head < len) {
            c = str.charAt(head);
            if (c == '_') {
                head++;
                if (head >= len || (c = str.charAt(head)) < '0' || c > '9')
                    this.in.raise("Illegal underscore placement");
                continue;
            }
            if (c < '0' || c > '9') break;
            head++;
            try {
                ip = Math.multiplyExact(ip, 10);
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
            none = false;
            if (head >= len) this.in.raise("Expected digits after decimal point");
            double s = 0.1d;
            c = str.charAt(head++);
            if (c < '0' || c > '9') this.in.raise("Invalid fractional part");
            frac += (s * (c - '0'));
            while (head < len) {
                c = str.charAt(head++);
                if (c == '_') {
                    if (head >= len || (c = str.charAt(head)) < '0' || c > '9')
                        this.in.raise("Illegal underscore placement");
                    continue;
                }
                if (c < '0' || c > '9') break;
                s /= 10;
                frac += (s * (c - '0'));
            }
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
                    exp = Math.multiplyExact(exp, 10);
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
        return TomlPrimitive.of(d);
    }

    private @NotNull TomlPrimitive parseDateTime(@NotNull CharSequence str) throws TomlException {
        final int len = str.length();
        if (len < 8) {
            this.in.raise("Datetime sequence is too short");
        }

        if (str.charAt(2) == ':') { // Local Time
            return TomlPrimitive.of(this.parsePartialTime(str, 0, len), this.options.get(JTomlOption.TIME_ZONE));
        }

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
                offset = ZoneOffset.ofHoursMinutes(hour, minute);
            } else {
                offset = ZoneOffset.UTC;
            }
            return TomlPrimitive.of(dateTime.atOffset(offset));
        }
    }

    private @NotNull LocalTime parsePartialTime(@NotNull CharSequence str, int off, int len) throws TomlException {
        if (len < 8) this.in.raise("Partial time sequence is too short");
        if (str.charAt(off + 2) != ':' || str.charAt(off + 5) != ':') this.in.raise("Missing time separator(s)");

        final int hour = this.parseNDigits(str, off, 2);
        final int minute = this.parseNDigits(str, off + 3, 2);
        final int second = this.parseNDigits(str, off + 6, 2);

        if (hour < 0 || hour > 23) this.in.raise("Hour out of range (got " + hour + ")");
        if (minute < 0 || minute > 59) this.in.raise("Minute out of range (got " + minute + ")");
        if (second < 0 || second > 59) this.in.raise("Minute out of range (got " + second + ")");

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
                if (pk == '\r') {
                    trimming = true;
                    this.in.next();
                    pk = this.in.next();
                    if (pk != '\n') this.in.raise("Carriage return without matching line feed");
                } else if (pk == '\n') {
                    trimming = true;
                    this.in.next();
                }
                if (!trimming) this.readEscapeSequence(sb);
            } else if (next == '"') {
                next = this.in.next();
                if (next != '\"') this.in.raise("Single quotation marks in a multi-line basic string is not allowed");
                next = this.in.next();
                if (next != '\"') this.in.raise("Double quotation marks in a multi-line basic string is not allowed");
                return TomlPrimitive.of(sb.toString());
            } else if (next == ' ' || next == '\t') {
                if (!trimming) sb.append(next);
            } else if (next < ' ' || next == 0x7F) {
                this.in.raise("Disallowed control character in literal string");
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

        switch (c) {
            case '"':  dest.append('"'); break;
            case '\\': dest.append('\\'); break;
            case 'b':  dest.append('\b'); break;
            case 'f':  dest.append('\f'); break;
            case 'n':  dest.append('\n'); break;
            case 'r':  dest.append('\r'); break;
            case 't':  dest.append('\t'); break;
            case 'U':
                uc = 8;
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
                if (uc == 8) {
                    dest.append(Character.toChars(v));
                } else {
                    dest.append((char) v);
                }
                break;
            default:
                this.in.raise("Invalid escape sequence character");
        }
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
        char c;

        while (true) {
            if (!this.in.skipWhitespace()) this.in.raise("Unclosed inline table");
            c = this.in.nextChar();
            if (c == '}') return ret;
            if (expectComma) {
                if (c != ',') this.in.raise("Expected inline table separator or closing char");
                if (!this.in.skipWhitespace()) this.in.raise("Unclosed inline table");
                c = this.in.nextChar();
                if (c == '}') return ret;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(c);
            TomlKey key = this.readKey(sb, '=', (c == '"' || c == '\'') ? c : '\0');
            if (!this.in.skipWhitespace()) this.in.raise("Expected value, got EOF");
            TomlValue value = this.readValue();
            ret.put(key, value);
            expectComma = true;
        }
    }

    private @NotNull TomlArray readArray() throws TomlException {
        TomlArray ret = TomlArray.create();

        char ctrl = this.readArrayControl();
        if (ctrl == ',') this.in.raise("Comma precedes array values");
        if (ctrl == ']') return ret;

        while (true) {
            ret.add(this.readValue(ctrl));
            ctrl = this.readArrayControl();
            if (ctrl == ',') {
                ctrl = this.readArrayControl();
                if (ctrl == ',') this.in.raise("Double comma in array");
            }
            if (ctrl == ']') return ret;
        }
    }

    /** Skip specific to arrays */
    private char readArrayControl() throws TomlException {
        boolean inComment = false;
        int next;

        while (true) {
            next = this.in.next();
            if (next == -1) this.in.raise("Unclosed array");
            if (next == '\r') {
                if (this.in.next() != '\n') this.in.raise("Carriage return without matching newline");
                inComment = false;
            } else if (next == '\n') {
                inComment = false;
            } else if (next == '#') {
                inComment = true;
            } else if (next != ' ' && next != '\t') {
                if (!inComment) return (char) next;
                if (next < ' ') this.in.raise("Disallowed control character in comment");
            }
        }
    }

}
