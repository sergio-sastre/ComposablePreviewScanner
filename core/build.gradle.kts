plugins {
    kotlin("jvm") version "2.0.0"
    id("org.jetbrains.compose") // required to resolve compose runtime
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" // needed for Kotlin 2.0 Compose Multiplatform
    id("maven-publish")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(libs.androidx.compose.runtime)
    implementation(libs.kotlin.reflect)
    implementation(libs.classgraph)
}

//https://www.talentica.com/blogs/publish-your-android-library-on-jitpack-for-better-reachability/
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            afterEvaluate {
                from(components["kotlin"])
            }
            groupId = "sergio.sastre.composable.preview.scanner"
            artifactId = "core"
            version = "0.6.1"
        }
    }
}
