
description = "Gson integration for JToml"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":api"))
    implementation("com.google.code.gson:gson:2.13.1")
}

//

centralPortal {
    name = "jtoml-serializer-gson"
    jarTask = tasks.jar
    sourcesJarTask = tasks.sourcesJar
    javadocJarTask = tasks.javadocJar
    pom {
        name = "JToml Gson Serializer"
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
