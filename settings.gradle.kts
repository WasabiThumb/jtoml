plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

rootProject.name = "jtoml"
include(
    ":all",
    ":api",
    ":internals",
   ":internals:test-utils",
    ":kotlin",
    ":serializer-gson",
    ":serializer-reflect",
    ":configurate"
)
