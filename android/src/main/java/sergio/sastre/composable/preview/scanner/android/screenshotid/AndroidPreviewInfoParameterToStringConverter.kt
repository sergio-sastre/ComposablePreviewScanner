package sergio.sastre.composable.preview.scanner.android.screenshotid

private const val NONE = -1
private const val RED_DOMINATED_EXAMPLE = 0
private const val GREEN_DOMINATED_EXAMPLE = 1
private const val BLUE_DOMINATED_EXAMPLE = 2
private const val YELLOW_DOMINATED_EXAMPLE = 3

private const val UI_MODE_NIGHT_YES = 32
private const val UI_MODE_NIGHT_MASK = 48

internal class AndroidPreviewInfoParameterToStringConverter {
    fun uiMode(uiMode: Int): String? =
        if (uiMode == 0) {
            null
        } else {
            when (uiMode and UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES) {
                true -> "NIGHT"
                else -> "DAY"
            }
        }

    fun device(device: String): String? = GetDeviceScreenshotId.getDeviceScreenshotId(device)

    fun wallpaper(wallpaper: Int): String? =
        when (wallpaper) {
            YELLOW_DOMINATED_EXAMPLE -> "WALLPAPER_YELLOW_DOMINATED"
            BLUE_DOMINATED_EXAMPLE -> "WALLPAPER_BLUE_DOMINATED"
            GREEN_DOMINATED_EXAMPLE -> "WALLPAPER_GREEN_DOMINATED"
            RED_DOMINATED_EXAMPLE -> "WALLPAPER_RED_DOMINATED"
            NONE -> null
            else -> null
        }
}