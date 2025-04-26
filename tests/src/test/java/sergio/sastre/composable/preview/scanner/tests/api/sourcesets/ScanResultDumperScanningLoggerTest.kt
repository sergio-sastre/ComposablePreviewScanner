package sergio.sastre.composable.preview.scanner.tests.api.sourcesets

import org.junit.Assert.assertTrue
import org.junit.Assume
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.Classpath
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.SourceSet.ANDROID_TEST
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.SourceSet.MAIN
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.SourceSet.SCREENSHOT_TEST
import sergio.sastre.composable.preview.scanner.core.annotations.RequiresShowStandardStreams
import sergio.sastre.composable.preview.scanner.core.scanresult.dump.ScanResultDumper
import sergio.sastre.composable.preview.scanner.core.utils.assetsFilePath
import sergio.sastre.composable.preview.scanner.utils.SystemOutputTestRule

@RunWith(Parameterized::class)
class ScanResultDumperScanningLoggerTest(
    val classpath: Classpath
) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun parameters(): Array<Any> {
            return arrayOf(
                Classpath(
                    sourceSet = MAIN,
                    variantName = "release"
                ),
                Classpath(
                    sourceSet = ANDROID_TEST,
                ),
                Classpath(
                    sourceSet = SCREENSHOT_TEST
                ),
            )
        }
    }

    @get:Rule
    val systemOutputTestRule = SystemOutputTestRule()

    @RequiresShowStandardStreams
    @Test
    fun `WHEN dumping scan result to file in Assets THEN outputs all scanning info except source set`() {
        // WHEN
        val scanResultFile = assetsFilePath(
            fileName = "scan_result.json"
        )
        val customPreviewsFile = assetsFilePath(
            fileName = "custom_previews.json"
        )
        Assume.assumeTrue(scanResultFile.exists().not())
        Assume.assumeTrue(customPreviewsFile.exists().not())

        try {
            ScanResultDumper()
                .enableScanningLogs()
                .setTargetSourceSet(classpath)
                .scanPackageTrees("sergio.sastre.composable.preview.scanner")
                .dumpScanResultToFileInAssets(
                    scanFileName = "scan_result.json",
                    customPreviewsFileName = "custom_previews.json"
                )

            // THEN
            assertTrue(
                "Output does contain source set",
                systemOutputTestRule.systemOutput.contains("Source set (compiled classes path): ${classpath.rootDir}/${classpath.packagePath}")
            )
        } finally {
            scanResultFile.delete()
            customPreviewsFile.delete()
        }
    }
}