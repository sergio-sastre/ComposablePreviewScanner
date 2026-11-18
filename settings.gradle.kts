pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        maven { url = uri("https://maven-central.storage-download.googleapis.com/maven2") }
        mavenCentral()
        gradlePluginPortal()
    }
}

includeBuild("paparazzi-plugin")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        maven { url = uri("https://maven-central.storage-download.googleapis.com/maven2") }
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "ComposablePreviewScanner"
include(":tests")
include(":core")
include(":android")
include(":custompreviews")
include(":paparazzi-plugin-tests")
include(":glance")
include(":wear")
include(":paparazzi-plugin-annotations")
include(":benchmark")
