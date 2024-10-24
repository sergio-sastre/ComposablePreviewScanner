package sergio.sastre.composable.preview.scanner.android.device.types

import sergio.sastre.composable.preview.scanner.android.device.domain.Device
import sergio.sastre.composable.preview.scanner.android.device.domain.Dimensions
import sergio.sastre.composable.preview.scanner.android.device.domain.Orientation.PORTRAIT
import sergio.sastre.composable.preview.scanner.android.device.domain.Shape.NORMAL
import sergio.sastre.composable.preview.scanner.android.device.domain.Type.PHONE
import sergio.sastre.composable.preview.scanner.android.device.domain.Unit.PX

val DEFAULT: Device
    get() = Device(
        dimensions = Dimensions(
            width = 1080f,
            height = 2340f,
            unit = PX
        ),
        densityDpi = 440,
        orientation = PORTRAIT,
        shape = NORMAL,
        chinSize = 0,
        type = PHONE
    )