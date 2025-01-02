plugins {
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
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

javafx {
    version = "21"
    modules("javafx.controls", "javafx.fxml")
}

// application {
//     mainClass = "..."
// }
