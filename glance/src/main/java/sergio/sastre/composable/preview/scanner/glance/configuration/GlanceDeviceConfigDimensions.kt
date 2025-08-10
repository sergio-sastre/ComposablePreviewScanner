package sergio.sastre.composable.preview.scanner.glance.configuration

import kotlin.math.roundToInt

/**
 * Helper to calculate Paparazzi's DeviceConfig dimensions to account for
 * Screenshots generated from Glance Composables.
 */
class GlanceDeviceConfigDimensions(
    private val densityDpi: Float,
    private val previewWidthDp: Int,
    private val previewHeightDp: Int,
) {
    fun width(originalDeviceConfigWidthPx: Int): Int {
        val widthPx = (previewWidthDp * densityDpi / 160).roundToInt()
        return when (widthPx > 0) {
            true -> widthPx
            false -> originalDeviceConfigWidthPx
        }
    }

    fun height(originalDeviceConfigHeightPx: Int): Int {
        val heightPx = (previewHeightDp * densityDpi / 160).roundToInt()
        return when (heightPx > 0) {
            true -> heightPx
            false -> originalDeviceConfigHeightPx
        }
    }
}