import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.publish)
}

description = "TOML integration for Configurate"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":"))
    api(project(":api"))
    api(platform("org.spongepowered:configurate-bom:4.2.0"))
    api("org.spongepowered:configurate-core")

    testImplementation(project(":internals:test-utils"))
    // JUnit Test Platform
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

//

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    coordinates("${project.group}", "jtoml-configurate", "${project.version}")
    pom {
        name.set("JToml Configurate")
        description.set(project.description!!)
        inceptionYear.set("2025")
        url.set("https://github.com/WasabiThumb/jtoml")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("wasabithumb")
                name.set("Xavier Pedraza")
                url.set("https://github.com/WasabiThumb/")
            }
        }
        scm {
            url.set("https://github.com/WasabiThumb/jtoml/")
            connection.set("scm:git:git://github.com/WasabiThumb/jtoml.git")
        }
    }
}
