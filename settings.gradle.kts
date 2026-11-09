plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
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
