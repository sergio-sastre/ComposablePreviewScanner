package sergio.sastre.composable.preview.scanner.scanning

import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import org.junit.Assert.assertEquals
import org.junit.Test
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreviewWithResult
import sergio.sastre.composable.preview.scanner.core.scanner.config.SourceScannerWithResult
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.Classpath
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.SourceSet.MAIN
import sergio.sastre.composable.preview.scanner.glance.GlanceComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.wear.WearTilePreviewInfo
import sergio.sastre.composable.preview.scanner.wear.WearTilePreviewScanner

/**
 * Verifies on-device (ART) that scanning from `androidTest/assets` via `scanFile(InputStream)`
 * yields the exact same previews as the host `buildTime` scan.
 *
 * Because `scanPackageTrees` throws `ScanSourceNotSupported` in instrumentation tests,
 * `SaveScanResultInAssets` writes `expected_build_time_previews_*.txt` into `androidTest/assets`
 * during the host build-time validation step (also checked on JVM by
 * `MainSourceSetAndBuildTimeParityTest`). Running this on-device guarantees that:
 * 1. `ScanResultDumper` wrote the JSONs into the module's `androidTest/assets` and they were
 *    bundled into the test APK.
 * 2. `ReflectionClassLoader` on ART resolves every preview identically to the JVM build-time scan.
 */
class AssetsBuildTimeParityInstrumentationTest {

    @Test
    fun GIVEN_android_assets_dump_WHEN_scanning_on_device_THEN_matches_build_time_previews() {
        assertAssetsParityWithBuildTime(
            scanner = AndroidComposablePreviewScanner().setTargetSourceSet(Classpath(MAIN)),
            scanResultAssetName = "scan_result_main_android.json",
            expectedPreviewsAssetName = "expected_build_time_previews_android.txt",
        )
    }

    @Test
    fun GIVEN_glance_assets_dump_WHEN_scanning_on_device_THEN_matches_build_time_previews() {
        assertAssetsParityWithBuildTime(
            scanner = GlanceComposablePreviewScanner().setTargetSourceSet(Classpath(MAIN)),
            scanResultAssetName = "scan_result_main_glance.json",
            expectedPreviewsAssetName = "expected_build_time_previews_glance.txt",
        )
    }

    @Test
    fun GIVEN_wear_tile_assets_dump_WHEN_scanning_on_device_THEN_matches_build_time_previews() {
        assertAssetsParityWithBuildTime(
            scanner = WearTilePreviewScanner().setTargetSourceSet(Classpath(MAIN)),
            scanResultAssetName = "scan_result_main_wear.json",
            expectedPreviewsAssetName = "expected_build_time_previews_wear_tile.txt",
            mapPreview = { preview ->
                val info = preview.previewInfo as WearTilePreviewInfo
                info.copy(device = info.device.ifEmpty { "id:wearos_small_round" }).toString() +
                    preview.identity()
            }
        )
    }

    private fun <T, R> assertAssetsParityWithBuildTime(
        scanner: SourceScannerWithResult<T, R>,
        scanResultAssetName: String,
        expectedPreviewsAssetName: String,
        mapPreview: (ComposablePreviewWithResult<T, R>) -> String = {
            it.previewInfo.toString() + it.identity()
        },
    ) {
        val assets = getInstrumentation().context.assets

        val expectedPreviews = assets.open(expectedPreviewsAssetName)
            .bufferedReader()
            .useLines { lines -> lines.filter { it.isNotEmpty() }.toList() }

        val actualPreviews = scanner
            .scanFile(
                targetInputStream = assets.open(scanResultAssetName),
                customPreviewsInfoInputStream = assets.open("custom_previews.json"),
            )
            .includePrivatePreviews()
            .getPreviews()
            .map(mapPreview)
            .sorted()

        assertEquals(
            "Mismatch in preview count between host buildTime and on-device assets for $scanResultAssetName",
            expectedPreviews.size,
            actualPreviews.size,
        )
        assertEquals(
            "Mismatch between host buildTime and on-device assets previews for $scanResultAssetName",
            expectedPreviews,
            actualPreviews,
        )
    }

    private fun ComposablePreviewWithResult<*, *>.identity(): String =
        "|$declaringClass|$methodName|$methodParametersType|$previewIndex"
}
