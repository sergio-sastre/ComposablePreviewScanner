plugins {
    kotlin("jvm") version "2.0.0"
    alias(libs.plugins.metalava)
    id("maven-publish")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    api(project(":core"))
    implementation(libs.kotlin.reflect)
    implementation (libs.classgraph)
}

//https://www.talentica.com/blogs/publish-your-android-library-on-jitpack-for-better-reachability/
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            afterEvaluate {
                from(components["kotlin"])
            }
            groupId = "sergio.sastre.composable.preview.scanner"
            artifactId = "android"
            version = "0.9.3"
        }
    }
}
