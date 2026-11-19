package sergio.sastre.composable.preview.scanner.tests.api.gradle

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreviewWithResult
import sergio.sastre.composable.preview.scanner.core.scanner.config.SourceScannerWithResult
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.Classpath
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.SourceSet.MAIN
import sergio.sastre.composable.preview.scanner.core.utils.assetsFilePath
import sergio.sastre.composable.preview.scanner.glance.GlanceComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.wear.WearTilePreviewInfo
import sergio.sastre.composable.preview.scanner.wear.WearTilePreviewScanner

@RunWith(TestParameterInjector::class)
class GradleTaskMainSourceSetAndBuildTimeParityTest {

    enum class Provider(
        private val buildTimeScanner: () -> SourceScannerWithResult<*, *>,
        private val gradleTaskScanner: () -> SourceScannerWithResult<*, *>,
        private val packageTree: String,
        private val scanFileName: String,
    ) {
        ANDROID(
            buildTimeScanner = { AndroidComposablePreviewScanner() },
            gradleTaskScanner = { AndroidComposablePreviewScanner().setTargetSourceSet(Classpath(MAIN)) },
            packageTree = "sergio.sastre.composable.preview.scanner",
            scanFileName = "scan_result_gradle_main_android.json",
        ),
        GLANCE(
            buildTimeScanner = { GlanceComposablePreviewScanner() },
            gradleTaskScanner = { GlanceComposablePreviewScanner().setTargetSourceSet(Classpath(MAIN)) },
            packageTree = "sergio.sastre.composable.preview.scanner",
            scanFileName = "scan_result_gradle_main_glance.json",
        ),
        WEAR_TILE(
            buildTimeScanner = { WearTilePreviewScanner() },
            gradleTaskScanner = { WearTilePreviewScanner().setTargetSourceSet(Classpath(MAIN)) },
            packageTree = "sergio.sastre.composable.preview.scanner.wear",
            scanFileName = "scan_result_gradle_main_wear.json",
        );

        fun scanBuildTime(): List<String> =
            buildTimeScanner()
                .scanPackageTrees(packageTree)
                .includePrivatePreviews()
                .getPreviews()
                .map { it.toComparableString() }
                .sorted()

        fun scanFromGradleTaskOutput(): List<String> =
            gradleTaskScanner()
                .scanFile(
                    targetInputStream = assetsFilePath(scanFileName).inputStream(),
                    customPreviewsInfoInputStream = assetsFilePath("custom_previews_gradle.json").inputStream()
                )
                .includePrivatePreviews()
                .getPreviews()
                .map { it.toComparableString() }
                .sorted()

        private fun ComposablePreviewWithResult<*, *>.toComparableString(): String {
            val info = when (val previewInfo = previewInfo) {
                is WearTilePreviewInfo ->
                    previewInfo.copy(device = previewInfo.device.ifEmpty { "id:wearos_small_round" })
                else -> previewInfo
            }
            return "$info|$declaringClass|$methodName|$methodParametersType|$previewIndex"
        }
    }

    @Test
    fun `GIVEN gradle task main source set dump WHEN comparing previews with build time THEN they find the exact same previews`(
        @TestParameter provider: Provider
    ) {
        val buildTimePreviews = provider.scanBuildTime()
        val gradleTaskPreviews = provider.scanFromGradleTaskOutput()

        assertEquals(
            "Mismatch in preview count between buildTime and gradleTask mainSourceSet for ${provider.name}",
            buildTimePreviews.size,
            gradleTaskPreviews.size
        )

        assertEquals(
            "Mismatch between buildTime and gradleTask mainSourceSet previews for ${provider.name}",
            buildTimePreviews,
            gradleTaskPreviews
        )
    }
}
