plugins {
    id("application")
}

group = "io.github.nonmilk"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(files("../coffee-grinder/lib/build/libs/lib-0.1.0.jar"))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

// application {
//     mainClass = "..."
// }
