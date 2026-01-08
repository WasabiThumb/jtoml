
plugins {
    alias(libs.plugins.indra.core)
    alias(libs.plugins.indra.licenser)
    alias(libs.plugins.indra.publishing)
}

description = "TOML integration for Configurate"

repositories {
    mavenCentral()
}

indra {
    github("WasabiThumb", "jtoml")
    apache2License()
    javaVersions {
        target(8)
        minimumToolchain(17)
        strictVersions(true)
    }
    configurePublications {
        artifactId = "jtoml-configurate"
        pom.developers {
            developer {
                id = "wasabithumb"
                name = "Xavier Pedraza"
                url = "https://github.com/WasabiThumb"
            }
            developer {
                id = "jmp"
                name = "Jason Penilla"
                url = "https://github.com/jpenilla"
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
    implementation(project(":"))
    api(project(":api"))
    api(platform(libs.configurate.bom))
    api("org.spongepowered:configurate-core")
    testImplementation(project(":internals:test-utils"))

    // JUnit 5
    testImplementation(platform("org.junit:junit-bom:5.14.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
