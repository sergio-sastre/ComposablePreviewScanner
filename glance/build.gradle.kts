import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.plugin.compose)
    id("com.vanniktech.maven.publish") version "0.29.0"
    id("com.gradleup.nmcp") version "0.0.8"
    id("maven-publish")
}

android {
    namespace = "sergio.sastre.composable.preview.scanner.glance"
    compileSdk = 35

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        // Enables Jetpack Compose for this module
        compose = true
    }

    /*
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
    
     */
}

dependencies {
    api(project(":core"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.junit)
    implementation(libs.androidx.appcompat)
    compileOnly(libs.androidx.glance)
    compileOnly(libs.androidx.glance.appwidget)
    compileOnly(libs.androidx.glance.preview)
    compileOnly(libs.androidx.glance.appwidget.preview)
    implementation(libs.classgraph)
    implementation(libs.kotlin.reflect)
}

//https://medium.com/@iRYO400/how-to-upload-your-android-library-to-maven-central-central-portal-in-2024-af7348742247
mavenPublishing {
    coordinates("io.github.sergio-sastre.ComposablePreviewScanner", "glance", "0.7.0")
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
        create<MavenPublication>("release") {
            afterEvaluate {
                from(components["release"])
            }
            groupId = "sergio.sastre.composable.preview.scanner"
            artifactId = "glance"
            version = "0.7.0"
        }
    }
}

 */