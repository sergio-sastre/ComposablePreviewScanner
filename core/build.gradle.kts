plugins {
    kotlin("jvm") version "2.0.0"
    alias(libs.plugins.jetbrains.kotlin.plugin.compose)
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
            version = "0.7.1"
        }
    }
}
