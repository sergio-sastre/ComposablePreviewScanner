package sergio.sastre.composable.preview.scanner.android.device.types

import sergio.sastre.composable.preview.scanner.android.device.domain.ChinSize
import sergio.sastre.composable.preview.scanner.android.device.domain.Device
import sergio.sastre.composable.preview.scanner.android.device.domain.Dimensions
import sergio.sastre.composable.preview.scanner.android.device.domain.GetDeviceByIdentifier
import sergio.sastre.composable.preview.scanner.android.device.domain.Identifier
import sergio.sastre.composable.preview.scanner.android.device.domain.Orientation.LANDSCAPE
import sergio.sastre.composable.preview.scanner.android.device.domain.Shape.NOTROUND
import sergio.sastre.composable.preview.scanner.android.device.domain.Type.TV
import sergio.sastre.composable.preview.scanner.android.device.domain.Unit.PX

enum class Television(
    override val device: Device
) : GetDeviceByIdentifier<Television> {

    TV_4K(
        Device(
            identifier = Identifier.TV_4K,
            dimensions = Dimensions(
                width = 3840f,
                height = 2160f,
                unit = PX,
            ),
            densityDpi = 640,
            orientation = LANDSCAPE,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = TV
        )
    ),

    TV_720p(
        Device(
            identifier = Identifier.TV_720p,
            dimensions = Dimensions(
                width = 1280f,
                height = 720f,
                unit = PX,
            ),
            densityDpi = 220,
            orientation = LANDSCAPE,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = TV
        )
    ),

    TV_1080p(
        Device(
            identifier = Identifier.TV_1080p,
            dimensions = Dimensions(
                width = 1920f,
                height = 1080f,
                unit = PX,
            ),
            densityDpi = 320,
            orientation = LANDSCAPE,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = TV
        )
    )
}