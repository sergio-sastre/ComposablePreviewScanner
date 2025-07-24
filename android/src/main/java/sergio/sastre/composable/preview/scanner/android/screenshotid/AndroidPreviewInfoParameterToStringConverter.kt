package sergio.sastre.composable.preview.scanner.android.screenshotid

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Wallpapers

internal class AndroidPreviewInfoParameterToStringConverter() {
    fun uiMode(uiMode: Int): String? =
        if (uiMode == 0) {
            null
        } else {
            when (uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
                true -> "NIGHT"
                else -> "DAY"
            }
        }

    fun device(device: String): String? = GetDeviceScreenshotId.getDeviceScreenshotId(device)

    fun wallpaper(wallpaper: Int): String? =
        when (wallpaper) {
            Wallpapers.YELLOW_DOMINATED_EXAMPLE -> "WALLPAPER_YELLOW_DOMINATED"
            Wallpapers.BLUE_DOMINATED_EXAMPLE -> "WALLPAPER_BLUE_DOMINATED"
            Wallpapers.GREEN_DOMINATED_EXAMPLE -> "WALLPAPER_GREEN_DOMINATED"
            Wallpapers.RED_DOMINATED_EXAMPLE -> "WALLPAPER_RED_DOMINATED"
            Wallpapers.NONE -> null
            else -> null
        }
}