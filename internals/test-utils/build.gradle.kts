
plugins {
    alias(libs.plugins.indra.core)
    alias(libs.plugins.indra.licenser)
}

description = "Internal utilities shared between unit tests"

repositories {
    mavenCentral()
}

indra.javaVersions {
    target(8)
    minimumToolchain(17)
    strictVersions(true)
}

dependencies {
    api(libs.annotations)
    api(libs.jspecify)
    implementation(project(":api"))
}
