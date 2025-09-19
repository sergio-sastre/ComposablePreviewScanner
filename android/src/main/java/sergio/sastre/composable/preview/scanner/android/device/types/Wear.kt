package sergio.sastre.composable.preview.scanner.android.device.types

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
            chinSize = 0,
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
            chinSize = 0,
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
            chinSize = 0,
            type = WEAR
        )
    ),

    WEAR_OS_RECT(
        Device(
            identifier = Identifier.WEAR_OS_RECT,
            dimensions = Dimensions(
                width = 402f,
                height = 476f,
                unit = PX
            ),
            densityDpi = 320,
            orientation = PORTRAIT,
            screenRatio = LONG,
            shape = NOTROUND,
            chinSize = 0,
            type = WEAR
        )
    ),

    // The Id was changed in more recent versions. New one reflects WEAR_OS_RECT
    // This is kept for compatibility with older versions of Android studio
    WEAR_OS_RECTANGULAR(
        Device(
            identifier = Identifier.WEAR_OS_RECTANGULAR,
            dimensions = Dimensions(
                width = 402f,
                height = 476f,
                unit = PX
            ),
            densityDpi = 320,
            orientation = PORTRAIT,
            screenRatio = LONG,
            shape = NOTROUND,
            chinSize = 0,
            type = WEAR
        )
    ),
}