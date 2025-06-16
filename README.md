# JToml
**[Wiki](https://github.com/WasabiThumb/jtoml/wiki) • [Javadocs](https://javadoc.io/doc/io.github.wasabithumb/jtoml-api)**

The ultimate [TOML](https://toml.io/en/v1.0.0) library for Java 8+.
Goals are feature completeness and a permissive yet type-safe API.
JToml supports the latest version of the TOML spec (``v1.0.0``).
TOML is a first-class citizen; no casts, no wonky date-time handling,
no dependencies. Working with keys is fluid and unambiguous with
methods for parsing, slicing and joining.

## Feature Matrix
|                                                             | [WasabiThumb/jtoml](https://github.com/WasabiThumb/jtoml) | [tomlj/tomlj](https://github.com/tomlj/tomlj) | [mwanji/toml4j](https://github.com/mwanji/toml4j) | [asafh/jtoml](https://github.com/asafh/jtoml) |
|:------------------------------------------------------------|:---------------------------------------------------------:|:---------------------------------------------:|:-------------------------------------------------:|:---------------------------------------------:|
| Key join & split                                            |                             ✅                             |                       ❌                       |                         ❌                         |                       ❌                       |
| ``v1.0.0`` compliance                                       |                             ✅                             |                       ✅                       |                         ❌                         |                       ❌                       |
| Positional errors                                           |                             ✅                             |                       ✅                       |                         ✅                         |                       ✅                       |
| Error recovery                                              |                             ❌                             |                       ✅                       |                         ✅                         |                       ❌                       |
| Configurable read rules                                     |                             ✅                             |                       ✅                       |                         ❌                         |                       ❌                       |
| Configurable write rules                                    |                             ✅                             |                       ❌                       |                         ✅                         |                       ❌                       |
| Enum-based type inspection                                  |                             ✅                             |                       ✅                       |                         ❌                         |                       ❌                       |
| Safe type coercion                                          |                             ✅                             |                       ❌                       |                         ❌                         |                       ❌                       |
| Reflect serialization                                       |                             ✅                             |                       ❌                       |                         ✅                         |                       ✅                       |
| JSON serialization                                          |                             ✅                             |                       ✅                       |                         ✅                         |                       ❌                       |
| Zero dependencies                                           |                             ✅                             |                       ❌                       |                         ❌                         |                       ✅                       |
| Passes [test suite](https://github.com/toml-lang/toml-test) |                             ✅                             |                       ❌                       |                         ❌                         |                       ❌                       |

## License
```text
Copyright 2025 Wasabi Codes

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```