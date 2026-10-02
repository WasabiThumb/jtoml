import tasks.GeneratePackageMetadataTask
import tasks.PeerResourcesTask

plugins {
    alias(libs.plugins.indra.core)
    alias(libs.plugins.indra.publishing)
    alias(libs.plugins.indra.git)
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
    // Annotations
    api(libs.annotations)
    api(libs.jspecify)

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

val peerResources = tasks.register("peerResources", PeerResourcesTask::class) {
    description = "Collates resources from peer projects"
    fromPeerProjects(peers)
}

val generatePackageMetadata = tasks.register("generatePackageMetadata", GeneratePackageMetadataTask::class) {
    description = "Creates the files that will be shipped in META-INF/jtoml"
}

tasks.processResources {
    // Shade peer resources
    dependsOn(peerResources)
    from(peerResources.map { it.outputs.files.singleFile })

    // Shade module classes and resources
    peers.forEach { src ->
        dependsOn(src.tasks.processResources)
        dependsOn(src.tasks.assemble)
        from(src.layout.buildDirectory.dir("classes/java/main"))
    }

    // Add package metadata
    dependsOn(generatePackageMetadata)
    into("META-INF/jtoml") {
        from(generatePackageMetadata)
    }
}

tasks.jar {
    // Add Git-Commit and Git-Branch to manifest
    indraGit.applyVcsInformationToManifest(manifest)
    manifest.attributes["Library-Version"] = "${rootProject.version}"
}

tasks.named<Jar>("sourcesJar") {
    // Include module sources
    peers.forEach { peer ->
        from(peer.sourceSets.main.get().allJava)
    }
}
