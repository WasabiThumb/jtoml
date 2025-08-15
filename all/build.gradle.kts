import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.publish)
    id("org.glavo.compile-module-info-plugin") version "2.0"
}

description = "All of JToml as a single module"

repositories {
    mavenCentral()
}

java {
    registerFeature("configurate") {
        usingSourceSet(sourceSets.main.get())
    }
    registerFeature("gson") {
        usingSourceSet(sourceSets.main.get())
    }
}

dependencies {
    // Dependencies for configurate
    "configurateApi"(platform(libs.configurate.bom))
    "configurateApi"("org.spongepowered:configurate-core")

    // Dependencies for serializer-gson
    "gsonApi"(libs.gson)
}

// Trying to generate javadocs for the module-info is
// cursed and will break
tasks.javadoc {
    exclude("**/module-info.java")
}

// Shade in all other modules
tasks.processResources {
    val sources = listOf(
        project(":"),
        project(":api"),
        project(":internals"),
        project(":configurate"),
        project(":kotlin"),
        project(":serializer-gson"),
        project(":serializer-reflect")
    )

    sources.forEach { src ->
        dependsOn(src.tasks.assemble)
        from(src.layout.buildDirectory.dir("classes/java/main"))
    }
}

//

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    coordinates("${project.group}", "jtoml-all", "${project.version}")
    pom {
        name.set("JToml Aggregator")
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
