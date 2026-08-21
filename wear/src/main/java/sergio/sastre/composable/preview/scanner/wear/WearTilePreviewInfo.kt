package sergio.sastre.composable.preview.scanner.wear

import sergio.sastre.composable.preview.scanner.android.device.types.Wear

/**
 * The values passed to the @androidx.wear.tiles.tooling.preview.Preview
 */
data class WearTilePreviewInfo(
    val name: String = "",
    val group: String = "",
    val device: String = "id:wearos_small_round",
    val fontScale: Float = 1f,
    val locale: String = "",
)
