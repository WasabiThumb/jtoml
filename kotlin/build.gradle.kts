
plugins {
    alias(libs.plugins.indra.core)
    alias(libs.plugins.indra.licenser)
    alias(libs.plugins.indra.publishing)
    alias(libs.plugins.jvm)
}

description = "Kotlin extensions for JToml"

repositories {
    mavenCentral()
}

indra {
    github("WasabiThumb", "jtoml")
    apache2License()
    javaVersions {
        target(8)
    }
    configurePublications {
        artifactId = "jtoml-kotlin"
        pom.developers {
            developer {
                id = "wasabithumb"
                name = "Xavier Pedraza"
                url = "https://github.com/WasabiThumb"
            }
        }
    }
}

indraSpotlessLicenser {
    licenseHeaderFile(rootProject.file("license_header.txt"))
    newLine(true)
}

dependencies {
    compileOnly(libs.annotations)
    api(project(":api"))
    implementation(project(":"))
}

kotlin {
    jvmToolchain(8)
}
