package sergio.sastre.composable.preview.scanner.android.device.types

import sergio.sastre.composable.preview.scanner.android.device.domain.Device
import sergio.sastre.composable.preview.scanner.android.device.domain.Dimensions
import sergio.sastre.composable.preview.scanner.android.device.domain.GetDeviceByIdentifier
import sergio.sastre.composable.preview.scanner.android.device.domain.Identifier
import sergio.sastre.composable.preview.scanner.android.device.domain.Orientation.PORTRAIT
import sergio.sastre.composable.preview.scanner.android.device.domain.Shape.NORMAL
import sergio.sastre.composable.preview.scanner.android.device.domain.Shape.ROUND
import sergio.sastre.composable.preview.scanner.android.device.domain.Type.WEAR
import sergio.sastre.composable.preview.scanner.android.device.domain.Unit.PX

enum class Wear(
    override val device: Device
) : GetDeviceByIdentifier<Wear> {

    WEAR_OS_SQUARE(
        Device(
            id = Identifier.WEAR_OS_SQUARE,
            dimensions = Dimensions(
                width = 360f,
                height = 360f,
                unit = PX
            ),
            densityDpi = 320,
            orientation = PORTRAIT,
            shape = NORMAL,
            chinSize = 0,
            type = WEAR
        )
    ),

    WEAR_OS_SMALL_ROUND(
        Device(
            id = Identifier.WEAR_OS_SMALL_ROUND,
            dimensions = Dimensions(
                width = 384f,
                height = 384f,
                unit = PX
            ),
            densityDpi = 320,
            orientation = PORTRAIT,
            shape = ROUND,
            chinSize = 0,
            type = WEAR
        )
    ),

    WEAR_OS_LARGE_ROUND(
        Device(
            id = Identifier.WEAR_OS_LARGE_ROUND,
            dimensions = Dimensions(
                width = 454f,
                height = 454f,
                unit = PX
            ),
            densityDpi = 320,
            orientation = PORTRAIT,
            shape = ROUND,
            chinSize = 0,
            type = WEAR
        )
    ),

    WEAR_OS_RECTANGULAR(
        Device(
            id = Identifier.WEAR_OS_RECTANGULAR,
            dimensions = Dimensions(
                width = 402f,
                height = 476f,
                unit = PX
            ),
            densityDpi = 320,
            orientation = PORTRAIT,
            shape = NORMAL,
            chinSize = 0,
            type = WEAR
        )
    ),
}