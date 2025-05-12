plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

rootProject.name = "jtoml"
include(":api", ":internals", ":serializer-gson", ":serializer-reflect", ":configurate")
