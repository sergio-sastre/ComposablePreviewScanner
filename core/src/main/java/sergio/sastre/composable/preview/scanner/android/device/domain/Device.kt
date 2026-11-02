package sergio.sastre.composable.preview.scanner.android.device.domain

import sergio.sastre.composable.preview.scanner.android.device.domain.Cutout.NONE
import sergio.sastre.composable.preview.scanner.android.device.domain.Navigation.GESTURE
import kotlin.math.ceil
import kotlin.math.floor

data class Device(
    val identifier: Identifier?,
    val dimensions: Dimensions,
    val densityDpi: Int,
    val orientation: Orientation,
    val shape: Shape,
    val chinSize: ChinSize = ChinSize(0F, dimensions.unit),
    val type: Type? = null,
    val screenRatio: ScreenRatio = ScreenRatioCalculator.calculateFor(dimensions),
    val screenSize: ScreenSize = ScreenSizeCalculator.calculateFor(dimensions, densityDpi),
    val cutout: Cutout = NONE,
    val navigation: Navigation = GESTURE,
) {

    fun inDp(): Device = this.copy(
        dimensions = dimensions.inDp(densityDpi),
        chinSize = chinSize.inDp(densityDpi)
    )

    fun inPx(): Device = this.copy(
        dimensions = dimensions.inPx(densityDpi),
        chinSize = chinSize.inPx(densityDpi)
    )
}

data class Dimensions(
    val height: Float,
    val width: Float,
    val unit: Unit,
) {
    fun inDp(densityDpi: Int): Dimensions {
        val conversionFactor: Float = densityDpi / 160f
        return when (unit) {
            Unit.DP -> this
            Unit.PX -> Dimensions(
                height = floor(height / conversionFactor),
                width = floor(width / conversionFactor),
                unit = Unit.DP
            )
        }
    }

    fun inPx(densityDpi: Int): Dimensions {
        val conversionFactor: Float = densityDpi / 160f
        return when (unit) {
            Unit.PX -> this
            Unit.DP ->
                Dimensions(
                    height = ceil(height * conversionFactor),
                    width = ceil(width * conversionFactor),
                    unit = Unit.PX
                )
        }
    }
}

enum class Unit(val value: String) {
    DP("dp"),
    PX("px")
}

data class ChinSize(
    val value: Float,
    val unit: Unit,
) {
    fun inDp(densityDpi: Int): ChinSize {
        val conversionFactor: Float = densityDpi / 160f
        return when (unit) {
            Unit.DP -> this
            Unit.PX -> ChinSize(
                value = floor(value / conversionFactor),
                unit = Unit.DP
            )
        }
    }

    fun inPx(densityDpi: Int): ChinSize {
        val conversionFactor: Float = densityDpi / 160f
        return when (unit) {
            Unit.PX -> this
            Unit.DP ->
                ChinSize(
                    value = ceil(value * conversionFactor),
                    unit = Unit.PX
                )
        }
    }
}

enum class Shape { ROUND, NOTROUND }

enum class ScreenRatio { LONG, NOTLONG }

enum class ScreenSize { SMALL, NORMAL, LARGE, XLARGE }

enum class Orientation(val value: String) {
    PORTRAIT("portrait"),
    LANDSCAPE("landscape")
}

enum class Cutout(val value: String) {
    NONE("none"),
    CORNER("corner"),
    DOUBLE("double"),
    PUNCH_HOLE("punch_hole"),
    TALL("tall")
}

enum class Navigation(val value: String) {
    GESTURE("gesture"),
    BUTTONS("buttons")
}

enum class Type { PHONE, TABLET, DESKTOP, FOLDABLE, WEAR, CAR, TV, XR }
