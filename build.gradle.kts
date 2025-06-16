import com.vanniktech.maven.publish.SonatypeHost
import tasks.FetchTestsTask

allprojects {
    apply(plugin = "java-library")

    group = "io.github.wasabithumb"
    version = "0.6.2"

    dependencies {
        compileOnly("org.jetbrains:annotations:26.0.1")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    }

    tasks.compileJava {
        options.encoding = "UTF-8"
    }

    tasks.compileTestJava {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    tasks.javadoc {
        options.encoding = Charsets.UTF_8.name()
        (options as CoreJavadocOptions).addBooleanOption("Xdoclint:none", true)
    }
}

//

plugins {
    id("java-library")
    alias(libs.plugins.publish)
    alias(libs.plugins.jvm) apply false
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
    testImplementation("com.google.code.gson:gson:2.13.1")

    testImplementation(project(":internals:test-utils"))
    // JUnit Test Platform
    testImplementation(libs.junit.jupiter)
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
