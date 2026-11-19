plugins {
    kotlin("jvm") version "2.0.0"
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":core"))
}
