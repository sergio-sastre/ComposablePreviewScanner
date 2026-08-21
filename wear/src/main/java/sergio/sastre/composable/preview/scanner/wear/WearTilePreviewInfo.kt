package sergio.sastre.composable.preview.scanner.wear

/**
 * The values passed to the @androidx.wear.tiles.tooling.preview.Preview
 */
data class WearTilePreviewInfo(
    val name: String = "",
    val group: String = "",
    val device: String = "",
    val fontScale: Float = 1f,
    val locale: String = "",
)
