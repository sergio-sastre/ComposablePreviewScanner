package sergio.sastre.composable.preview.scanner.run_before_instrumentation_tests

import org.junit.Assert.assertEquals
import org.junit.Test
import sergio.sastre.composable.preview.scanner.core.annotations.RequiresShowStandardStreams
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.Classpath
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.SourceSet.ANDROID_TEST
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.SourceSet.MAIN
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.SourceSet.SCREENSHOT_TEST
import sergio.sastre.composable.preview.scanner.core.scanresult.dump.ScanResultDumper
import sergio.sastre.composable.preview.scanner.core.utils.assetsFilePath
import sergio.sastre.composable.preview.scanner.tests.api.sourcesets.MainSourceSetAndBuildTimeParityTest.AnyProvider

class SaveScanResultInAssets {
    @OptIn(RequiresShowStandardStreams::class)
    @Test
    fun `task -- save scan result in assets`() {
        val scanResultScreenshotTestFileName = "scan_result_screenshot_test.json"
        val scanResultAndroidTestFileName = "scan_result_android_test.json"
        val scanResultMainAndroidFileName = "scan_result_main_android.json"
        val scanResultMainGlanceFileName = "scan_result_main_glance.json"
        val scanResultMainWearFileName = "scan_result_main_wear.json"
        val customPreviewsFileName = "custom_previews.json"

        data class DumpSpec(
            val sourceSet: Classpath,
            val packageTree: String,
            val fileName: String,
            val customPreviewPackages: List<String> = emptyList(),
        )

        val dumpSpecs = listOf(
            DumpSpec(
                sourceSet = Classpath(SCREENSHOT_TEST),
                packageTree = "sergio.sastre.composable.preview.scanner",
                fileName = scanResultScreenshotTestFileName,
                customPreviewPackages = listOf(
                    "androidx.compose.ui.tooling.preview",
                    "sergio.sastre.composable.preview.custompreviews"
                )
            ),
            DumpSpec(
                sourceSet = Classpath(ANDROID_TEST),
                packageTree = "sergio.sastre.composable.preview.scanner",
                fileName = scanResultAndroidTestFileName,
            ),
            DumpSpec(
                sourceSet = Classpath(MAIN),
                packageTree = "sergio.sastre.composable.preview.scanner.android",
                fileName = scanResultMainAndroidFileName,
                customPreviewPackages = listOf(
                    "androidx.compose.ui.tooling.preview",
                    "sergio.sastre.composable.preview.custompreviews"
                )
            ),
            DumpSpec(
                sourceSet = Classpath(MAIN),
                packageTree = "sergio.sastre.composable.preview.scanner.glance",
                fileName = scanResultMainGlanceFileName,
            ),
            DumpSpec(
                sourceSet = Classpath(MAIN),
                packageTree = "sergio.sastre.composable.preview.scanner.wear",
                fileName = scanResultMainWearFileName,
            ),
        )

        dumpSpecs.forEach { spec ->
            ScanResultDumper()
                .enableScanningLogs()
                .setTargetSourceSet(spec.sourceSet)
                .scanPackageTrees(spec.packageTree)
                .dumpScanResultToFileInAssets(
                    scanFileName = spec.fileName,
                    packageTreesOfCustomPreviews = spec.customPreviewPackages
                )
        }

        val expectedFiles = dumpSpecs.map { it.fileName } + customPreviewsFileName
        expectedFiles.forEach { fileName ->
            assert(assetsFilePath(fileName).exists()) {
                "Expected generated asset file to exist: $fileName"
            }
        }

        validateParityWithBuildTime()
    }

    private fun validateParityWithBuildTime() {
        AnyProvider.values().forEach { anyProvider ->
            val buildTimePreviews = anyProvider.provider.scanBuildTime()
            val mainSourceSetPreviews = anyProvider.provider.scanMainSourceSet()

            assertEquals(
                "Mismatch in preview count between buildTime and mainSourceSet for ${anyProvider.name}",
                buildTimePreviews.size,
                mainSourceSetPreviews.size
            )
            assertEquals(
                "Mismatch between buildTime and mainSourceSet previews for ${anyProvider.name}",
                buildTimePreviews,
                mainSourceSetPreviews
            )
        }
    }
}