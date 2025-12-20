# JToml
**[Wiki](https://github.com/WasabiThumb/jtoml/wiki) • [Javadocs](https://javadoc.io/doc/io.github.wasabithumb/jtoml-api)**

A modular [TOML](https://toml.io/en/v1.1.0) library for Java 8 and above. JToml aims to
be the ultimate solution for all things TOML, fully recreating its type system with a
**robust yet permissive API inspired by Gson**.

## Comparison
|                                                                                                 | Up To Date¹ | Test Coverage² | Reflection³ | Json⁴ | Comments⁵ |
|------------------------------------------------------------------------------------------------:|:-----------:|:--------------:|:-----------:|:-----:|:---------:|
|                                   [``WasabiThumb/jtoml``](https://github.com/WasabiThumb/jtoml) |      ✅      |       ✅        |      ✅      |   ✅   |     ✅     |
| [``FasterXML/jackson-dataformats-text``](https://github.com/FasterXML/jackson-dataformats-text) |      ❌      |       ❌        |      ✅      |   ✅   |     ❌     |
|                                           [``mwanji/toml4j``](https://github.com/mwanji/toml4j) |      ❌      |       ❌        |      ✅      |   ✅   |     ❌     |
|             [``TheElectronWill/night-config``](https://github.com/TheElectronWill/night-config) |      ❌      |       ❌        |      ❌      |   ✅   |     ✅     |
|                                               [``tomlj/tomlj``](https://github.com/tomlj/tomlj) |      ❌      |       ❌        |      ❌      |   ✅   |     ❌     |



> ¹ Supports the latest version of the specification- ``v1.1.0`` as of writing.
>
> ² Passes all tests in the [official test suite](https://github.com/toml-lang/toml-test).
>
> ³ May be used to convert TOML to/from user-defined Java objects in some way.
>
> ⁴ May be used to convert TOML to/from a JSON representation in some way.
>
> ⁵ Supports TOML [comments](https://toml.io/en/v1.1.0#comment) to some extent.


## Star History

<a href="https://www.star-history.com/#WasabiThumb/jtoml&Date">
 <picture>
   <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/svg?repos=WasabiThumb/jtoml&type=Date&theme=dark" />
   <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/svg?repos=WasabiThumb/jtoml&type=Date" />
   <img alt="Star History Chart" src="https://api.star-history.com/svg?repos=WasabiThumb/jtoml&type=Date" />
 </picture>
</a>

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
