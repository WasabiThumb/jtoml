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
    registerFeature("recsup") {
        usingSourceSet(sourceSets.main.get())
    }
    registerFeature("gson") {
        usingSourceSet(sourceSets.main.get())
    }

    modularity.inferModulePath = false
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    // Dependencies for configurate
    "configurateApi"(platform(libs.configurate.bom))
    "configurateApi"("org.spongepowered:configurate-core")

    // Dependencies for serializer-reflect
    "recsupImplementation"(libs.recsup)

    // Dependencies for serializer-gson
    "gsonApi"(libs.gson)
}

val peers = listOf(
    project(":"),
    project(":api"),
    project(":internals"),
    project(":configurate"),
    project(":kotlin"),
    project(":serializer-gson"),
    project(":serializer-reflect")
)

tasks.javadoc {
    // Trying to generate javadocs for the module-info is
    // cursed and will break
    exclude("**/module-info.java")

    // Generate javadocs for module sources
    peers.forEach { peer ->
        source(peer.sourceSets.main.get().allJava)
    }
}

tasks.processResources {
    // Shade module classes
    peers.forEach { src ->
        dependsOn(src.tasks.assemble)
        from(src.layout.buildDirectory.dir("classes/java/main"))
    }
}

tasks.named<Jar>("sourcesJar") {
    // Include module sources
    peers.forEach { peer ->
        from(peer.sourceSets.main.get().allJava)
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
