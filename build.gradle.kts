import com.vanniktech.maven.publish.SonatypeHost
import tasks.FetchTestsTask

allprojects {
    apply(plugin = "java-library")

    group = "io.github.wasabithumb"
    version = "1.4.1"
}

//

plugins {
    id("java-library")
    alias(libs.plugins.indra.core)
    alias(libs.plugins.indra.licenser)
    alias(libs.plugins.indra.git) apply false
    alias(libs.plugins.publish)
    alias(libs.plugins.jvm) apply false
}

description = "Fully compliant TOML library for Java"

repositories {
    mavenCentral()
}

indra.javaVersions {
    target(8)
    minimumToolchain(17)
    strictVersions(true)
    testWith(17)
}

dependencies {
    compileOnly(libs.annotations)
    api(project(":api"))
    implementation(project(":internals"))

    testImplementation(project(":serializer-gson"))
    testImplementation(project(":serializer-reflect"))
    testImplementation("com.google.code.gson:gson:2.13.1")
    testImplementation(project(":internals:test-utils"))
}

tasks.register<FetchTestsTask>("fetchTests") {
    outputs.upToDateWhen { false }
    outDir.set(layout.projectDirectory.dir("src/test/resources"))
}

tasks.processTestResources {
    dependsOn(tasks.named("fetchTests"))
}

//

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    coordinates("${project.group}", "jtoml", "${project.version}")
    pom {
        name.set("JToml")
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
