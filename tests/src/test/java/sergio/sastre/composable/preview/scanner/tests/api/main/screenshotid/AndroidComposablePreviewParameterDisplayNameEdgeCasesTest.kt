package sergio.sastre.composable.preview.scanner.tests.api.main.screenshotid

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.android.screenshotid.AndroidPreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

@RunWith(TestParameterInjector::class)
class AndroidComposablePreviewParameterDisplayNameEdgeCasesTest {

    enum class DisplayNameTestCase(
        val group: String,
        val expectedNames: List<String>
    ) {
        DECLARES_GET_DISPLAY_NAME(
            group = "no-preview-parameter-limit",
            expectedNames = listOf("Jim", "Jens", "2" /*index for null value*/)
        ),
        NON_NULLABLE_RETURN(
            group = "non-nullable-display-name",
            expectedNames = listOf("Jim", "Jens")
        ),
        INTERFACE_INHERITED(
            group = "interface-inherited-display-name",
            expectedNames = listOf("Jim", "Jens")
        ),
        SUPERCLASS_INHERITED(
            group = "superclass-inherited-display-name",
            expectedNames = listOf("Jim", "Jens")
        )
    }

    private fun previewIndexDisplayNamesInGroup(group: String): List<ComposablePreview<AndroidPreviewInfo>> =
        AndroidComposablePreviewScanner()
            .scanPackageTrees("sergio.sastre.composable.preview.scanner.android.previewparametersdisplayname")
            .filterPreviews { it.group == group }
            .getPreviews()

    @Test
    fun `GIVEN provider with display name edge cases, THEN previewIndexDisplayName returns its value for each parameter`(
        @TestParameter testCase: DisplayNameTestCase
    ) {
        val previews = previewIndexDisplayNamesInGroup(testCase.group)
        val displayedNames = previews.map {
            AndroidPreviewScreenshotIdBuilder(it).build().substringAfterLast("_")
        }
        assertEquals(
            testCase.expectedNames,
            displayedNames
        )
    }
}
