package sergio.sastre.composable.preview.scanner.android.device.types

import sergio.sastre.composable.preview.scanner.android.device.domain.Device
import sergio.sastre.composable.preview.scanner.android.device.domain.Dimensions
import sergio.sastre.composable.preview.scanner.android.device.domain.GetDeviceByIdentifier
import sergio.sastre.composable.preview.scanner.android.device.domain.Identifier
import sergio.sastre.composable.preview.scanner.android.device.domain.Orientation.LANDSCAPE
import sergio.sastre.composable.preview.scanner.android.device.domain.Shape.NORMAL
import sergio.sastre.composable.preview.scanner.android.device.domain.Type.DESKTOP
import sergio.sastre.composable.preview.scanner.android.device.domain.Unit.PX

enum class Desktop(
    override val device: Device
) : GetDeviceByIdentifier<Desktop> {

    SMALL_DESKTOP(
        Device(
            id = Identifier.SMALL_DESKTOP,
            dimensions = Dimensions(
                width = 1366f,
                height = 768f,
                unit = PX
            ),
            densityDpi = 160,
            orientation = LANDSCAPE,
            shape = NORMAL,
            chinSize = 0,
            type = DESKTOP
        )
    ),

    MEDIUM_DESKTOP(
        Device(
            id = Identifier.MEDIUM_DESKTOP,
            dimensions = Dimensions(
                width = 3840f,
                height = 2160f,
                unit = PX
            ),
            densityDpi = 320,
            orientation = LANDSCAPE,
            shape = NORMAL,
            chinSize = 0,
            type = DESKTOP
        )
    ),

    LARGE_DESKTOP(
        Device(
            id = Identifier.LARGE_DESKTOP,
            dimensions = Dimensions(
                width = 1920f,
                height = 1080f,
                unit = PX
            ),
            densityDpi = 160,
            orientation = LANDSCAPE,
            shape = NORMAL,
            chinSize = 0,
            type = DESKTOP
        ),
    )
}