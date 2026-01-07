
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
    compileOnly(libs.annotations)
    implementation(project(":api"))
}
