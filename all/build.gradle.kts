import java.io.*
import java.nio.file.Files
import kotlin.io.path.name

plugins {
    alias(libs.plugins.indra.core)
    alias(libs.plugins.indra.publishing)
}

description = "All of JToml as a single module"

repositories {
    mavenCentral()
}

indra {
    github("WasabiThumb", "jtoml")
    apache2License()
    javaVersions {
        target(8)
        minimumToolchain(17)
    }
    configurePublications {
        artifactId = "jtoml-all"
        pom.developers {
            developer {
                id = "wasabithumb"
                name = "Xavier Pedraza"
                url = "https://github.com/WasabiThumb"
            }
        }
    }
}

sourceSets {
    main {
        multirelease {
            alternateVersions(9)
            moduleName("io.github.wasabithumb.jtoml")
        }
    }

    // Gradle 10+ requires features to have their own source sets
    register("configurate")
    register("gson")
}

java {
    registerFeature("configurate") {
        usingSourceSet(sourceSets.named("configurate").get())
    }
    registerFeature("gson") {
        usingSourceSet(sourceSets.named("gson").get())
    }
    modularity.inferModulePath = false
    withSourcesJar()
}

configurations {
    compileOnly {
        // Allows javadoc task to find transient dependency classes
        extendsFrom(named("configurateApi"))
        extendsFrom(named("gsonApi"))
    }
}

dependencies {
    // Dependencies for configurate
    "configurateApi"(platform(libs.configurate.bom))
    "configurateApi"("org.spongepowered:configurate-core")

    // Dependencies for serializer-reflect
    implementation(libs.recsup)

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

fun blitFile(src: File, target: File) {
    if (target.exists()) {
        // Treat as service file
        val targetPath = target.toPath()
        val temp = targetPath.parent.resolve(targetPath.name + ".tmp")
        var ok = false
        try {
            Files.newBufferedWriter(temp, Charsets.UTF_8).use { out ->
                val set: MutableSet<String> = mutableSetOf()
                Files.newBufferedReader(src.toPath(), Charsets.UTF_8).useLines { s ->
                    s.forEach {
                        if (it.isEmpty() || !set.add(it)) return@forEach
                        out.write(it)
                        out.write('\n'.code)
                    }
                }
                Files.newBufferedReader(targetPath, Charsets.UTF_8).useLines { s ->
                    s.forEach {
                        if (it.isEmpty() || !set.add(it)) return@forEach
                        out.write(it)
                        out.write('\n'.code)
                    }
                }
                out.flush()
            }
            Files.delete(targetPath)
            Files.move(temp, targetPath)
            ok = true
        } finally {
            if (!ok) Files.delete(temp)
        }
    } else {
        src.copyTo(target, true)
    }
}

fun recursiveCopy(src: File, target: File) {
    if (!src.exists()) return
    if (!target.exists()) target.mkdirs()

    val files = src.listFiles() ?:
        throw Error("Failed to list directory $src")

    files.forEach { sub ->
        val dest = File(target, sub.name)
        if (sub.isDirectory) {
            recursiveCopy(sub, File(target, sub.name))
        } else {
            blitFile(sub, dest)
        }
    }
}

tasks.processResources {
    val tmp = project.layout.buildDirectory.dir("tmp/peerResources")

    // Shade module classes and resources
    peers.forEach { src ->
        dependsOn(src.tasks.processResources)
        dependsOn(src.tasks.assemble)
        from(src.layout.buildDirectory.dir("classes/java/main"))
        from(tmp)
    }

    doFirst {
        peers.forEach { peers ->
            val resources = peers.layout.buildDirectory.dir("resources/main")
            recursiveCopy(resources.get().asFile, tmp.get().asFile)
        }
    }
}

tasks.named<Jar>("sourcesJar") {
    // Include module sources
    peers.forEach { peer ->
        from(peer.sourceSets.main.get().allJava)
    }
}
