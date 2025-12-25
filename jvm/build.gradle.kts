plugins {
    kotlin("jvm") version "2.0.0"
    alias(libs.plugins.metalava)
    id("maven-publish")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    api(project(":core"))
    implementation(libs.classgraph)
    implementation(libs.kotlin.reflect)
}

//https://www.talentica.com/blogs/publish-your-android-library-on-jitpack-for-better-reachability/
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            afterEvaluate {
                from(components["kotlin"])
            }
            groupId = "sergio.sastre.composable.preview.scanner"
            artifactId = "jvm"
            version = "0.8.1"
        }
    }
}