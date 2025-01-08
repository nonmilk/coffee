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
    // FIXME temporary fix for submodule dependencies
    implementation("io.github.shimeoki.jfx:rasterization:3.0.0")
    implementation("io.github.shimeoki:jshaper:0.15.0")
    implementation("io.github.traunin:triangulation:1.1.1")
    implementation("io.github.alphameo:linear_algebra:2.1.2")

    implementation(files("../grinder/lib/build/libs/lib-0.1.0.jar"))
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

application {
    mainClass = "io.github.nonmilk.coffee.App"
}
