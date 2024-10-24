package sergio.sastre.composable.preview.scanner.android.device.domain

import sergio.sastre.composable.preview.scanner.android.device.domain.Cutout.NONE
import sergio.sastre.composable.preview.scanner.android.device.domain.Navigation.GESTURE
import kotlin.math.ceil
import kotlin.math.floor

data class Device(
    val id: Identifier? = null,
    val dimensions: Dimensions,
    val densityDpi: Int,
    val orientation: Orientation,
    val shape: Shape,
    val chinSize: Int,
    val type: Type?,
    val cutout: Cutout = NONE,
    val navigation: Navigation = GESTURE,
) {
    private val conversionFactor: Float = densityDpi / 160f

    fun inDp(): Device =
        when (dimensions.unit) {
            Unit.DP -> this
            Unit.PX -> this.copy(
                dimensions = Dimensions(
                    height = floor(dimensions.height / conversionFactor),
                    width = floor(dimensions.width / conversionFactor),
                    unit = Unit.DP
                )
            )
        }

    fun inPx(): Device = when (dimensions.unit) {
        Unit.PX -> this
        Unit.DP -> this.copy(
            dimensions = Dimensions(
                height = ceil(dimensions.height * conversionFactor),
                width = ceil(dimensions.width * conversionFactor),
                unit = Unit.DP
            )
        )
    }
}

class Dimensions(
    val height: Float,
    val width: Float,
    val unit: Unit,
)

enum class Unit(val value: String) {
    DP("dp"),
    PX("px")
}

enum class Shape { ROUND, NORMAL }

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

enum class Type { PHONE, TABLET, DESKTOP, FOLDABLE, WEAR, CAR, TV }
