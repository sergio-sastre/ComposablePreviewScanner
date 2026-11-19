val scanResultDumperClasspath by configurations.creating

dependencies {
    scanResultDumperClasspath(project(":scan-result-dumper-cli"))
}

tasks.register<JavaExec>("dumpMainSourceSetScanResult") {
    description = "Dumps MAIN source set scan results into androidTest assets via JavaExec using ScanResultDumper"
    group = "Verification"

    classpath = scanResultDumperClasspath
    mainClass.set("sergio.sastre.composable.preview.scanner.dumper.cli.ScanResultDumperCliKt")
    workingDir = project.projectDir
    maxHeapSize = "4g"

    // Source set classes must exist before dumping scan results
    tasks.findByName("compileDebugKotlin")?.let { dependsOn(it) }

    inputs.dir(project.layout.buildDirectory.dir("tmp/kotlin-classes/debug"))

    outputs.files(
        project.file("src/androidTest/assets/scan_result_gradle_main_android.json"),
        project.file("src/androidTest/assets/scan_result_gradle_main_glance.json"),
        project.file("src/androidTest/assets/scan_result_gradle_main_wear.json"),
        project.file("src/androidTest/assets/custom_previews_gradle.json")
    )
    outputs.upToDateWhen { false }

    args(
        "--spec",
        "sergio.sastre.composable.preview.scanner.android|scan_result_gradle_main_android.json|androidx.compose.ui.tooling.preview,sergio.sastre.composable.preview.custompreviews|custom_previews_gradle.json",
        "--spec",
        "sergio.sastre.composable.preview.scanner.glance|scan_result_gradle_main_glance.json||custom_previews_gradle.json",
        "--spec",
        "sergio.sastre.composable.preview.scanner.wear|scan_result_gradle_main_wear.json||custom_previews_gradle.json"
    )
}
