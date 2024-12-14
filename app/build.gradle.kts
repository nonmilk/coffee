plugins {
    id("application")
}

group = "io.github.nonmilk"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

// application {
//     mainClass = "..."
// }
