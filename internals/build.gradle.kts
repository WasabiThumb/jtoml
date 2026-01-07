import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.indra.core)
    alias(libs.plugins.indra.licenser)
    alias(libs.plugins.indra.git)
    alias(libs.plugins.publish)
}

description = "Non-API components for JToml"

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
    implementation(project(":api"))
}

tasks.jar {
    // Add Git-Commit and Git-Branch to manifest
    indraGit.applyVcsInformationToManifest(manifest)
}

//

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    coordinates("${project.group}", "jtoml-internals", "${project.version}")
    pom {
        name.set("JToml Internals")
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
