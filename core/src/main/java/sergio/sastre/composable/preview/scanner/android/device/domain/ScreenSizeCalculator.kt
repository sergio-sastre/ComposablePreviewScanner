package sergio.sastre.composable.preview.scanner.android.device.domain

import sergio.sastre.composable.preview.scanner.android.device.domain.Identifier.Companion.NEXUS_4
import sergio.sastre.composable.preview.scanner.android.device.types.Automotive
import kotlin.math.min

internal object ScreenSizeCalculator {
    private val SCREEN_SIZE_EXCEPTIONS: Map<Identifier?, ScreenSize> = mapOf(
        NEXUS_4 to ScreenSize.NORMAL
    ) + Automotive.entries.map { auto -> auto.device.identifier to ScreenSize.NORMAL }

    fun calculateFor(dimensions: Dimensions, densityDpi: Int): ScreenSize {
        val dimensionsInDp = dimensions.inDp(densityDpi)
        val smallestWidth = min(dimensionsInDp.height, dimensionsInDp.width)
        return when {
            smallestWidth < 320f -> ScreenSize.SMALL
            smallestWidth < 480f -> ScreenSize.NORMAL
            smallestWidth < 720f -> ScreenSize.LARGE
            else -> ScreenSize.XLARGE
        }
    }
}