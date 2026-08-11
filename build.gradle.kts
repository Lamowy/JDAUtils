plugins {
    kotlin("jvm") version "2.4.0"
}

group = "io.github.lamowy"
version = "1.1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("net.dv8tion:JDA:6.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")

    implementation("org.yaml:snakeyaml:2.6")

    implementation("com.squareup.okio:okio:3.18.1")

    implementation(files("libs/LangUtils-1.0.jar"))
    implementation(files("libs/FileUtils-1.0.1.jar"))
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}