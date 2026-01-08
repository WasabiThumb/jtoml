import tasks.FetchTestsTask

allprojects {
    apply(plugin = "java-library")

    group = "io.github.wasabithumb"
    version = "1.4.2"
}

//

plugins {
    id("java-library")
    alias(libs.plugins.indra.core)
    alias(libs.plugins.indra.licenser)
    alias(libs.plugins.indra.publishing)
    alias(libs.plugins.indra.sonatype)
    alias(libs.plugins.indra.git) apply false
    alias(libs.plugins.jvm) apply false
}

description = "Fully compliant TOML library for Java"

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
        artifactId = "jtoml"
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

sourceSets.test {
    multirelease {
        alternateVersions(17)
    }
}

dependencies {
    compileOnly(libs.annotations)
    api(project(":api"))
    implementation(project(":internals"))

    testImplementation(project(":serializer-gson"))
    testImplementation(project(":serializer-reflect"))
    testImplementation("com.google.code.gson:gson:2.13.1")
    testImplementation(project(":internals:test-utils"))

    // JUnit 5
    testImplementation(platform("org.junit:junit-bom:5.14.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.register<FetchTestsTask>("fetchTests") {
    outputs.upToDateWhen { false }
    outDir.set(layout.projectDirectory.dir("src/test/resources"))
}

tasks.processTestResources {
    dependsOn(tasks.named("fetchTests"))
}
