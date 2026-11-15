package sergio.sastre.composable.preview.scanner.android.device.domain

import sergio.sastre.composable.preview.scanner.android.device.domain.Cutout.NONE
import sergio.sastre.composable.preview.scanner.android.device.domain.Navigation.GESTURE
import kotlin.math.round

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
    private var convertedFrom: Dimensions? = null

    fun inDp(densityDpi: Int): Dimensions {
        val conversionFactor: Float = densityDpi / 160f
        return when (unit) {
            Unit.DP -> this
            Unit.PX -> convertedFrom ?: Dimensions(
                height = round(height / conversionFactor),
                width = round(width / conversionFactor),
                unit = Unit.DP
            ).also { it.convertedFrom = this }
        }
    }

    fun inPx(densityDpi: Int): Dimensions {
        val conversionFactor: Float = densityDpi / 160f
        return when (unit) {
            Unit.PX -> this
            Unit.DP -> convertedFrom ?: Dimensions(
                height = round(height * conversionFactor),
                width = round(width * conversionFactor),
                unit = Unit.PX
            ).also { it.convertedFrom = this }
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
    private var convertedFrom: ChinSize? = null

    fun inDp(densityDpi: Int): ChinSize {
        val conversionFactor: Float = densityDpi / 160f
        return when (unit) {
            Unit.DP -> this
            Unit.PX -> convertedFrom ?: ChinSize(
                value = round(value / conversionFactor),
                unit = Unit.DP
            ).also { it.convertedFrom = this }
        }
    }

    fun inPx(densityDpi: Int): ChinSize {
        val conversionFactor: Float = densityDpi / 160f
        return when (unit) {
            Unit.PX -> this
            Unit.DP -> convertedFrom ?: ChinSize(
                value = round(value * conversionFactor),
                unit = Unit.PX
            ).also { it.convertedFrom = this }
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
