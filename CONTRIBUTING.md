Contributing to JToml
=====================
Thank you for your interest in contributing to JToml!
To help your PR move along more smoothly,
feel free to observe the guidelines below.

## ✅ Do

### Observe conventions
Code contributed to JToml should match the "style"
of existing code on best-effort basis to avoid
confusion and clutter. This style is in line with the
[conventions laid out by Oracle](https://www.oracle.com/java/technologies/javase/codeconventions-contents.html),
which may be used as a reference when the optimal
form for any code is unclear.

### Use comments
Comments should be used when the function of any
piece of code may be unclear. Full sentences
are preferred except when context (typically another
comment) allows the same information to be conveyed
more efficiently. Javadoc comments are expected on
user-facing classes and their members,
and not required for any other class/member.

### Use JetBrains annotations
JToml uses ``org.jetbrains.annotations`` to document
method contracts. The ``@NotNull`` and
``@Nullable`` annotations should be preferred over
``@Contract`` when possible.

### Understand your code
Changes that make sense will be accepted without
exception. Please be prepared to justify your
code if necessary.

## 🚫 Avoid

### Using an organization for your PR
This prevents JToml maintainers from making any
modifications to your PR, which may unnecessarily
delay approval when cosmetic changes are required.

### Writing lines in excess of 120 characters
This is the default line hint in IDEA, the IDE used to
develop JToml. Lines approaching 80 characters or more
may often be shortened in a variety of ways to improve
readability.

### Repeating yourself
[Don't repeat yourself.](https://en.wikipedia.org/wiki/Don%27t_repeat_yourself)
