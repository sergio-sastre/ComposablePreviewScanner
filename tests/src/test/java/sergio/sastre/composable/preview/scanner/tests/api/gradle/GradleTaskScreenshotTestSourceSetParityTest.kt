package sergio.sastre.composable.preview.scanner.tests.api.gradle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.Classpath
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.SourceSet.SCREENSHOT_TEST
import sergio.sastre.composable.preview.scanner.core.utils.assetsFilePath

/**
 * ScreenshotTest has also previews extra annotated with some custom Previews,
 * therefore it is interesting to test
 */
class GradleTaskScreenshotTestSourceSetParityTest {

    @Test
    fun `GIVEN gradle task screenshotTest source set dump WHEN comparing previews with direct source set scan THEN they find the exact same previews`() {
        val directSourceSetPreviews = AndroidComposablePreviewScanner()
            .setTargetSourceSet(
                sourceSetClasspath = Classpath(SCREENSHOT_TEST),
                packageTreesOfCrossModuleCustomPreviews = listOf("sergio.sastre.composable.preview.custompreviews")
            )
            .scanPackageTrees("sergio.sastre.composable.preview.scanner")
            .includePrivatePreviews()
            .getPreviews()

        val gradleTaskPreviews = AndroidComposablePreviewScanner()
            .setTargetSourceSet(Classpath(SCREENSHOT_TEST))
            .scanFile(
                targetInputStream = assetsFilePath("scan_result_gradle_screenshot_test.json").inputStream(),
                customPreviewsInfoInputStream = assetsFilePath("custom_previews_gradle.json").inputStream()
            )
            .includePrivatePreviews()
            .getPreviews()

        assertEquals(
            "Mismatch in preview count between direct SCREENSHOT_TEST scan and gradleTask dump",
            directSourceSetPreviews.size,
            gradleTaskPreviews.size
        )

        assertEquals(
            "Mismatch between direct SCREENSHOT_TEST scan and gradleTask dump previews",
            directSourceSetPreviews.map { "${it.previewInfo}|${it.declaringClass}|${it.methodName}|${it.methodParametersType}|${it.previewIndex}" }.sorted(),
            gradleTaskPreviews.map { "${it.previewInfo}|${it.declaringClass}|${it.methodName}|${it.methodParametersType}|${it.previewIndex}" }.sorted()
        )
    }
}
