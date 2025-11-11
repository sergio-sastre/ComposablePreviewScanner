package sergio.sastre.composable.preview.scanner.android.device.types

import sergio.sastre.composable.preview.scanner.android.device.domain.ChinSize
import sergio.sastre.composable.preview.scanner.android.device.domain.Device
import sergio.sastre.composable.preview.scanner.android.device.domain.Dimensions
import sergio.sastre.composable.preview.scanner.android.device.domain.GetDeviceByIdentifier
import sergio.sastre.composable.preview.scanner.android.device.domain.Identifier
import sergio.sastre.composable.preview.scanner.android.device.domain.Orientation.LANDSCAPE
import sergio.sastre.composable.preview.scanner.android.device.domain.Orientation.PORTRAIT
import sergio.sastre.composable.preview.scanner.android.device.domain.ScreenSize.NORMAL
import sergio.sastre.composable.preview.scanner.android.device.domain.Shape.NOTROUND
import sergio.sastre.composable.preview.scanner.android.device.domain.Type.CAR
import sergio.sastre.composable.preview.scanner.android.device.domain.Unit.PX

enum class Automotive(
    override val device: Device
): GetDeviceByIdentifier<Automotive> {

    AUTOMOTIVE_1024DP_LANDSCAPE(
        Device(
            identifier = Identifier.AUTOMOTIVE_1024DP_LANDSCAPE,
            dimensions = Dimensions(
                width = 1024f,
                height = 768f,
                unit = PX
            ),
            densityDpi = 160,
            orientation = LANDSCAPE,
            screenSize = NORMAL,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = CAR
        )
    ),

    AUTOMOTIVE_1080DP_LANDSCAPE(
        Device(
            identifier = Identifier.AUTOMOTIVE_1080DP_LANDSCAPE,
            dimensions = Dimensions(
                width = 1080f,
                height = 600f,
                unit = PX
            ),
            densityDpi = 120,
            orientation = LANDSCAPE,
            screenSize = NORMAL,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = CAR
        )
    ),

    AUTOMOTIVE_1408DP_LANDSCAPE(
        Device(
            identifier = Identifier.AUTOMOTIVE_1408DP_LANDSCAPE,
            dimensions = Dimensions(
                width = 1408f,
                height = 792f,
                unit = PX
            ),
            densityDpi = 160,
            orientation = LANDSCAPE,
            screenSize = NORMAL,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = CAR
        )
    ),

    AUTOMOTIVE_1408DP_LANDSCAPE_WITH_GOOGLE_PLAY(
        Device(
            identifier = Identifier.AUTOMOTIVE_1408DP_LANDSCAPE_WITH_GOOGLE_PLAY,
            dimensions = Dimensions(
                width = 1408f,
                height = 792f,
                unit = PX
            ),
            densityDpi = 160,
            orientation = LANDSCAPE,
            screenSize = NORMAL,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = CAR
        )
    ),

    AUTOMOTIVE_DISTANT_DISPLAY(
        Device(
            identifier = Identifier.AUTOMOTIVE_DISTANT_DISPLAY,
            dimensions = Dimensions(
                width = 1080f,
                height = 600f,
                unit = PX
            ),
            densityDpi = 120,
            orientation = LANDSCAPE,
            screenSize = NORMAL,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = CAR
        )
    ),

    AUTOMOTIVE_DISTANT_DISPLAY_WITH_GOOGLE_PLAY(
        Device(
            identifier = Identifier.AUTOMOTIVE_DISTANT_DISPLAY_WITH_GOOGLE_PLAY,
            dimensions = Dimensions(
                width = 1080f,
                height = 600f,
                unit = PX
            ),
            densityDpi = 120,
            orientation = LANDSCAPE,
            screenSize = NORMAL,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = CAR
        )
    ),

    AUTOMOTIVE_PORTRAIT(
        Device(
            identifier = Identifier.AUTOMOTIVE_PORTRAIT,
            dimensions = Dimensions(
                width = 800f,
                height = 1280f,
                unit = PX
            ),
            densityDpi = 120,
            orientation = PORTRAIT,
            screenSize = NORMAL,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = CAR
        )
    ),

    AUTOMOTIVE_LARGE_PORTRAIT(
        Device(
            identifier = Identifier.AUTOMOTIVE_LARGE_PORTRAIT,
            dimensions = Dimensions(
                width = 1280f,
                height = 1606f,
                unit = PX
            ),
            densityDpi = 160,
            orientation = PORTRAIT,
            screenSize = NORMAL,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = CAR
        )
    ),

    AUTOMOTIVE_ULTRAWIDE(
        Device(
            identifier = Identifier.AUTOMOTIVE_ULTRAWIDE,
            dimensions = Dimensions(
                width = 3904f,
                height = 1320f,
                unit = PX
            ),
            densityDpi = 240,
            orientation = LANDSCAPE,
            screenSize = NORMAL,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = CAR
        )
    ),
}