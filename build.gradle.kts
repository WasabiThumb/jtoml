import tasks.FetchTestsTask

allprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "net.thebugmc.gradle.sonatype-central-portal-publisher")

    group = "io.github.wasabithumb"
    version = "0.3.1"

    dependencies {
        compileOnly("org.jetbrains:annotations:26.0.1")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        toolchain.languageVersion.set(JavaLanguageVersion.of(8))
        withSourcesJar()
        withJavadocJar()
    }

    tasks.compileJava {
        options.encoding = "UTF-8"
    }

    tasks.javadoc {
        options.encoding = Charsets.UTF_8.name()
        (options as CoreJavadocOptions).addBooleanOption("Xdoclint:none", true)
    }
}

//

plugins {
    id("java-library")
    id("maven-publish")
    id("signing")
    id("net.thebugmc.gradle.sonatype-central-portal-publisher") version "1.2.4"
}

description = "Fully compliant TOML parser and serializer for Java"

repositories {
    mavenCentral()
}

dependencies {
    api(project(":api"))
    implementation(project(":internals"))

    testImplementation(project(":serializer-gson"))
    testImplementation(project(":serializer-reflect"))
    testImplementation(project(":configurate"))
    testImplementation("com.google.code.gson:gson:2.13.1")

    // JUnit Test Platform
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<FetchTestsTask>("fetchTests") {
    outputs.upToDateWhen { false }
    outDir.set(layout.projectDirectory.dir("src/test/resources"))
}

tasks.processTestResources {
    dependsOn(tasks.named("fetchTests"))
}

//

centralPortal {
    name = "jtoml"
    jarTask = tasks.jar
    sourcesJarTask = tasks.sourcesJar
    javadocJarTask = tasks.javadocJar
    pom {
        name = "JToml"
        description = project.description
        url = "https://github.com/WasabiThumb/jtoml"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "wasabithumb"
                email = "wasabithumbs@gmail.com"
                organization = "Wasabi Codes"
                organizationUrl = "https://wasabithumb.github.io/"
                timezone = "-5"
            }
        }
        scm {
            connection = "scm:git:git://github.com/WasabiThumb/jtoml.git"
            url = "https://github.com/WasabiThumb/jtoml"
        }
    }
}
