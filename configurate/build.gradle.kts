
description = "Gson integration for Configurate"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":api"))
    api(platform("org.spongepowered:configurate-bom:4.2.0"))
    api("org.spongepowered:configurate-core")
}

//

centralPortal {
    name = "jtoml-configurate"
    jarTask = tasks.jar
    sourcesJarTask = tasks.sourcesJar
    javadocJarTask = tasks.javadocJar
    pom {
        name = "JToml Configurate"
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
