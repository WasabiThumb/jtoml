
plugins {
    alias(libs.plugins.indra.core)
    alias(libs.plugins.indra.licenser)
    alias(libs.plugins.indra.publishing)
}

description = "API components for JToml"

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
        artifactId = "jtoml-api"
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
}
