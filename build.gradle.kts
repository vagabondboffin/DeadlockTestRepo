import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.25"
}

group = "org.example"
version = "1.0-SNAPSHOT"

val kotlinFile: String? by project
if (kotlinFile != null) { 
    sourceSets {
        getByName("main") {
            kotlin.srcDir("src/main/kotlin")
            kotlin.include(kotlinFile)
        }
    }
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:lincheck:2.39")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
}


tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}

