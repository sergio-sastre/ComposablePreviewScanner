package sergio.sastre.composable.preview.scanner.android.device.domain

import sergio.sastre.composable.preview.scanner.android.device.domain.Cutout.NONE
import sergio.sastre.composable.preview.scanner.android.device.domain.Navigation.GESTURE
import kotlin.math.abs
import kotlin.math.round

private const val ROUNDING_EPSILON = 1e-9

private fun pxToDp(rawPx: Double, densityDpi: Int): Double =
    rawPx * 160.0 / densityDpi

private fun dpToPx(rawDp: Double, densityDpi: Int): Double =
    rawDp * densityDpi / 160.0

private fun roundOrOriginal(value: Double, original: Float?): Float {
    if (original != null && abs(value - original.toDouble()) < ROUNDING_EPSILON) {
        return original
    }
    return round(value).toFloat()
}

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
    // Preserves unrounded Double values and initial float values across chained conversions
    // without changing the public API.
    private var rawHeight: Double = height.toDouble()
    private var rawWidth: Double = width.toDouble()
    private var initialHeight: Float = height
    private var initialWidth: Float = width
    private var initialUnit: Unit = unit

    fun inDp(densityDpi: Int): Dimensions = when (unit) {
        Unit.DP -> this
        Unit.PX -> {
            val dpHeight = pxToDp(rawHeight, densityDpi)
            val dpWidth = pxToDp(rawWidth, densityDpi)
            val origHeight = initialHeight.takeIf { initialUnit == Unit.DP }
            val origWidth = initialWidth.takeIf { initialUnit == Unit.DP }
            Dimensions(
                height = roundOrOriginal(dpHeight, origHeight),
                width = roundOrOriginal(dpWidth, origWidth),
                unit = Unit.DP
            ).also {
                it.rawHeight = dpHeight
                it.rawWidth = dpWidth
                it.initialHeight = initialHeight
                it.initialWidth = initialWidth
                it.initialUnit = initialUnit
            }
        }
    }

    fun inPx(densityDpi: Int): Dimensions = when (unit) {
        Unit.PX -> this
        Unit.DP -> {
            val pxHeight = dpToPx(rawHeight, densityDpi)
            val pxWidth = dpToPx(rawWidth, densityDpi)
            val origHeight = initialHeight.takeIf { initialUnit == Unit.PX }
            val origWidth = initialWidth.takeIf { initialUnit == Unit.PX }
            Dimensions(
                height = roundOrOriginal(pxHeight, origHeight),
                width = roundOrOriginal(pxWidth, origWidth),
                unit = Unit.PX
            ).also {
                it.rawHeight = pxHeight
                it.rawWidth = pxWidth
                it.initialHeight = initialHeight
                it.initialWidth = initialWidth
                it.initialUnit = initialUnit
            }
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
    // Preserves unrounded Double value and initial float value across chained conversions
    // without changing the public API.
    private var rawValue: Double = value.toDouble()
    private var initialValue: Float = value
    private var initialUnit: Unit = unit

    fun inDp(densityDpi: Int): ChinSize = when (unit) {
        Unit.DP -> this
        Unit.PX -> {
            val dpValue = pxToDp(rawValue, densityDpi)
            val origValue = initialValue.takeIf { initialUnit == Unit.DP }
            ChinSize(
                value = roundOrOriginal(dpValue, origValue),
                unit = Unit.DP
            ).also {
                it.rawValue = dpValue
                it.initialValue = initialValue
                it.initialUnit = initialUnit
            }
        }
    }

    fun inPx(densityDpi: Int): ChinSize = when (unit) {
        Unit.PX -> this
        Unit.DP -> {
            val pxValue = dpToPx(rawValue, densityDpi)
            val origValue = initialValue.takeIf { initialUnit == Unit.PX }
            ChinSize(
                value = roundOrOriginal(pxValue, origValue),
                unit = Unit.PX
            ).also {
                it.rawValue = pxValue
                it.initialValue = initialValue
                it.initialUnit = initialUnit
            }
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
