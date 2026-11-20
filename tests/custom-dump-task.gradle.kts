import java.io.File
import org.gradle.api.attributes.Attribute
import org.gradle.process.CommandLineArgumentProvider

val scanResultDumperClasspath by configurations.creating

dependencies {
    scanResultDumperClasspath(project(":scan-result-dumper-cli"))
}

tasks.register<JavaExec>("dumpMainSourceSetScanResult") {
    description = "Dumps MAIN source set scan results into androidTest assets via JavaExec using ScanResultDumper"
    group = "Verification"

    classpath = scanResultDumperClasspath
    mainClass.set("sergio.sastre.composable.preview.scanner.dumper.cli.ScanResultDumperCliKt")
    workingDir = rootProject.projectDir
    maxHeapSize = "4g"

    // Source set classes must exist before dumping scan results
    tasks.findByName("compileDebugKotlin")?.let { dependsOn(it) }
    tasks.findByName("compileDebugScreenshotTestKotlin")?.let { dependsOn(it) }

    val debugCompileJars = configurations.getByName("debugCompileClasspath").incoming.artifactView {
        attributes {
            attribute(Attribute.of("artifactType", String::class.java), "android-classes-jar")
        }
    }.files
    inputs.dir(project.layout.buildDirectory.dir("tmp/kotlin-classes/debug"))
    inputs.dir(project.layout.buildDirectory.dir("intermediates/built_in_kotlinc/debugScreenshotTest/compileDebugScreenshotTestKotlin/classes"))
    inputs.files(debugCompileJars)

    outputs.files(
        project.file("src/androidTest/assets/scan_result_gradle_main_android.json"),
        project.file("src/androidTest/assets/scan_result_gradle_main_glance.json"),
        project.file("src/androidTest/assets/scan_result_gradle_main_wear.json"),
        project.file("src/androidTest/assets/scan_result_gradle_screenshot_test.json"),
        project.file("src/androidTest/assets/custom_previews_gradle.json")
    )
    outputs.upToDateWhen { false }

    args(
        "--spec",
        "MAIN|sergio.sastre.composable.preview.scanner.android|scan_result_gradle_main_android.json|androidx.compose.ui.tooling.preview,sergio.sastre.composable.preview.custompreviews|custom_previews_gradle.json",
        "--spec",
        "MAIN|sergio.sastre.composable.preview.scanner.glance|scan_result_gradle_main_glance.json||custom_previews_gradle.json",
        "--spec",
        "MAIN|sergio.sastre.composable.preview.scanner.wear|scan_result_gradle_main_wear.json||custom_previews_gradle.json",
        "--spec",
        "SCREENSHOT_TEST|sergio.sastre.composable.preview.scanner|scan_result_gradle_screenshot_test.json||custom_previews_gradle.json"
    )

    argumentProviders.add(
        CommandLineArgumentProvider {
            listOf(
                "--custom-previews-classpath",
                debugCompileJars.files.joinToString(File.pathSeparator) { it.absolutePath }
            )
        }
    )
}
