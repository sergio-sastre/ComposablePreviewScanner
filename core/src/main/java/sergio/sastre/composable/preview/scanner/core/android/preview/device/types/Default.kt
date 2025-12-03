package sergio.sastre.composable.preview.scanner.core.android.preview.device.types

import sergio.sastre.composable.preview.scanner.core.android.preview.device.domain.ChinSize
import sergio.sastre.composable.preview.scanner.core.android.preview.device.domain.Device
import sergio.sastre.composable.preview.scanner.core.android.preview.device.domain.Dimensions
import sergio.sastre.composable.preview.scanner.core.android.preview.device.domain.Orientation.PORTRAIT
import sergio.sastre.composable.preview.scanner.core.android.preview.device.domain.Shape.NOTROUND
import sergio.sastre.composable.preview.scanner.core.android.preview.device.domain.Type.PHONE
import sergio.sastre.composable.preview.scanner.core.android.preview.device.domain.Unit.PX

val DEFAULT: Device
    get() = Device(
        identifier = null,
        dimensions = Dimensions(
            width = 1080f,
            height = 2340f,
            unit = PX
        ),
        densityDpi = 440,
        orientation = PORTRAIT,
        shape = NOTROUND,
        chinSize = ChinSize(0F, PX),
        type = PHONE
    )