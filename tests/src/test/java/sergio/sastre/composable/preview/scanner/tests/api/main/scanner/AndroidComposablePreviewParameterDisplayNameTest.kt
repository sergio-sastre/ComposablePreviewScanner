package sergio.sastre.composable.preview.scanner.tests.api.main.scanner

import org.junit.Assert.assertEquals
import org.junit.Test
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner

class AndroidComposablePreviewParameterDisplayNameTest {

    private fun previewIndexDisplayNamesInGroup(group: String): List<String?> =
        AndroidComposablePreviewScanner()
            .scanPackageTrees("sergio.sastre.composable.preview.scanner.android.previewparametersdisplayname")
            .filterPreviews { it.group == group }
            .getPreviews()
            .map { it.previewIndexDisplayName }

    @Test
    fun `GIVEN provider declares getDisplayName THEN previewIndexDisplayName returns its value for each parameter`() {
        assertEquals(
            listOf("Jim", "Jens", null),
            previewIndexDisplayNamesInGroup("no-preview-parameter-limit")
        )
    }

    @Test
    fun `GIVEN provider declares getDisplayName with non-nullable String return THEN previewIndexDisplayName returns its value for each parameter`() {
        assertEquals(
            listOf("Jim", "Jens"),
            previewIndexDisplayNamesInGroup("non-nullable-display-name")
        )
    }

    @Test
    fun `GIVEN provider inherits getDisplayName from an interface default THEN previewIndexDisplayName returns its value for each parameter`() {
        assertEquals(
            listOf("Jim", "Jens"),
            previewIndexDisplayNamesInGroup("interface-inherited-display-name")
        )
    }

    @Test
    fun `GIVEN provider inherits getDisplayName from an abstract superclass THEN previewIndexDisplayName returns its value for each parameter`() {
        assertEquals(
            listOf("Jim", "Jens"),
            previewIndexDisplayNamesInGroup("superclass-inherited-display-name")
        )
    }
}
