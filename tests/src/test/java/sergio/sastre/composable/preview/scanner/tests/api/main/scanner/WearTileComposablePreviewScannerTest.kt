package sergio.sastre.composable.preview.scanner.tests.api.main.scanner

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.wear.tiles.RequestBuilders.ResourcesRequest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import sergio.sastre.composable.preview.scanner.MainActivity
import sergio.sastre.composable.preview.scanner.wear.WearTileComposablePreviewScanner

@RunWith(RobolectricTestRunner::class)
class WearTileComposablePreviewScannerTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `GIVEN Composable with wear tile @Preview, WHEN Scanning that package tree, THEN it returns the previews`() {
        val composablePreviews =
            WearTileComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.wear")
                .getPreviews()

        assertTrue(composablePreviews.any { it.methodName == "wearTilePreview" })

        val statefulTilePreviews = composablePreviews.filter { it.methodName == "statefulTilePreview" }
        assertTrue(statefulTilePreviews.size == 2)
    }

    @Test
    fun `GIVEN Composable with wear tile @Preview with resources and platform data, WHEN Scanning, THEN it returns the preview with expected data`() {
        val composablePreviews =
            WearTileComposablePreviewScanner()
                .scanPackageTrees("sergio.sastre.composable.preview.scanner.wear")
                .getPreviews()

        val preview = composablePreviews.find { it.methodName == "tilePreviewWithResourcesAndPlatformData" }
        assertNotNull(preview)

        composeTestRule.setContent {
            val tilePreviewData = preview!!.invoke()

            // Assert platformDataValues
            assertNotNull(tilePreviewData.platformDataValues)

            // Assert onTileResourceRequest
            val resources = tilePreviewData.onTileResourceRequest(ResourcesRequest.Builder().setVersion("1").build())
            assertTrue(resources.idToImageMapping.containsKey("image_id"))
        }
    }
}
