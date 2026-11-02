package sergio.sastre.composable.preview.scanner.android.device.domain

internal object ScreenRatioCalculator {

    fun calculateFor(dimensions: Dimensions): ScreenRatio {
        val aspectRatio = maxOf(dimensions.width, dimensions.height) / minOf(dimensions.width, dimensions.height)
        return when {
            aspectRatio >= 1.75 -> ScreenRatio.LONG
            else -> ScreenRatio.NOTLONG
        }
    }
}