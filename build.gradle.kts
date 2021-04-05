plugins {
    kotlin("js") version "1.4.32"
    kotlin("plugin.serialization") version "1.4.32"
}

group = "io.github.rednesto"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers")
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")

    implementation("org.jetbrains:kotlin-react:17.0.2-pre.150-kotlin-1.4.31")
    implementation("org.jetbrains:kotlin-react-dom:17.0.2-pre.150-kotlin-1.4.31")
    implementation("org.jetbrains:kotlin-styled:5.2.1-pre.150-kotlin-1.4.31")
    implementation("org.jetbrains:kotlin-react-router-dom:5.2.0-pre.150-kotlin-1.4.31")

    testImplementation(kotlin("test-js"))
}

kotlin {
    js(IR) {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
    }
}
