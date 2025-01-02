plugins {
    id("application")
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
    implementation("io.github.alphameo:linear_algebra:1.2.0")

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
