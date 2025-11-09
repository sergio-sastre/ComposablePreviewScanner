package sergio.sastre.composable.preview.scanner.android.device.types

import sergio.sastre.composable.preview.scanner.android.device.domain.ChinSize
import sergio.sastre.composable.preview.scanner.android.device.domain.Device
import sergio.sastre.composable.preview.scanner.android.device.domain.Dimensions
import sergio.sastre.composable.preview.scanner.android.device.domain.GetDeviceByIdentifier
import sergio.sastre.composable.preview.scanner.android.device.domain.Identifier
import sergio.sastre.composable.preview.scanner.android.device.domain.Orientation.PORTRAIT
import sergio.sastre.composable.preview.scanner.android.device.domain.ScreenRatio.LONG
import sergio.sastre.composable.preview.scanner.android.device.domain.Shape.NOTROUND
import sergio.sastre.composable.preview.scanner.android.device.domain.Shape.ROUND
import sergio.sastre.composable.preview.scanner.android.device.domain.Type.WEAR
import sergio.sastre.composable.preview.scanner.android.device.domain.Unit.PX

private val WEAR_OS_RECT_DEFAULT =
    Device(
        identifier = null,
        dimensions = Dimensions(
            width = 402f,
            height = 476f,
            unit = PX
        ),
        densityDpi = 320,
        orientation = PORTRAIT,
        screenRatio = LONG,
        shape = NOTROUND,
        chinSize = ChinSize(0F, PX),
        type = WEAR
    )

enum class Wear(
    override val device: Device
) : GetDeviceByIdentifier<Wear> {

    WEAR_OS_SQUARE(
        Device(
            identifier = Identifier.WEAR_OS_SQUARE,
            dimensions = Dimensions(
                width = 360f,
                height = 360f,
                unit = PX
            ),
            densityDpi = 320,
            orientation = PORTRAIT,
            screenRatio = LONG,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = WEAR
        )
    ),

    WEAR_OS_SMALL_ROUND(
        Device(
            identifier = Identifier.WEAR_OS_SMALL_ROUND,
            dimensions = Dimensions(
                width = 384f,
                height = 384f,
                unit = PX
            ),
            densityDpi = 320,
            orientation = PORTRAIT,
            screenRatio = LONG,
            shape = ROUND,
            chinSize = ChinSize(0F, PX),
            type = WEAR
        )
    ),

    WEAR_OS_LARGE_ROUND(
        Device(
            identifier = Identifier.WEAR_OS_LARGE_ROUND,
            dimensions = Dimensions(
                width = 454f,
                height = 454f,
                unit = PX
            ),
            densityDpi = 320,
            orientation = PORTRAIT,
            screenRatio = LONG,
            shape = ROUND,
            chinSize = ChinSize(0F, PX),
            type = WEAR
        )
    ),

    WEAR_OS_RECT(
        WEAR_OS_RECT_DEFAULT.copy(identifier = Identifier.WEAR_OS_RECT)
    ),

    @Deprecated(
        message = "Replaced by WEAR_OS_RECT in newer Android Studio versions",
        replaceWith = ReplaceWith("Wear.WEAR_OS_RECT")
    )
    WEAR_OS_RECTANGULAR(
        WEAR_OS_RECT_DEFAULT.copy(identifier = Identifier.WEAR_OS_RECTANGULAR)
    ),
}