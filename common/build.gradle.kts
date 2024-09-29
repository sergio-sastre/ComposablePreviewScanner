import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("jvm") version "2.0.0"
    id("com.vanniktech.maven.publish") version "0.29.0"
    id("com.gradleup.nmcp") version "0.0.8"
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

//https://medium.com/@iRYO400/how-to-upload-your-android-library-to-maven-central-central-portal-in-2024-af7348742247
mavenPublishing {
    coordinates("io.github.sergio-sastre.ComposablePreviewScanner", "common", "0.7.2")
    pom {
        name = "ComposablePreviewScanner"
        description = "A library to help auto-generate screenshot tests from Composable Previews with any screenshot testing library"
        inceptionYear = "2024"
        url = "https://github.com/sergio-sastre/ComposablePreviewScanner"
        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/license/mit"
                distribution = "https://github.com/sergio-sastre/ComposablePreviewScanner/blob/master/LICENSE"
            }
        }
        developers {
            developer {
                id = "sergio-sastre"
                name = "Sergio Sastre Flórez"
                url = "https://github.com/sergio-sastre/ComposablePreviewScanner"
            }
        }
        scm {
            url = "https://github.com/sergio-sastre/ComposablePreviewScanner"
            connection = "scm:git:git://github.com/sergio-sastre/ComposablePreviewScanner.git"
            developerConnection = "scm:git:ssh://git@github.com/sergio-sastre/ComposablePreviewScanner.git"
        }
    }
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
}


//https://www.talentica.com/blogs/publish-your-android-library-on-jitpack-for-better-reachability/
/*
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            afterEvaluate {
                from(components["kotlin"])
            }
            groupId = "sergio.sastre.composable.preview.scanner"
            artifactId = "common"
            version = "0.7.2"
        }
    }
}

 */