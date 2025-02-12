package sergio.sastre.composable.preview.scanner.android.device.types

import sergio.sastre.composable.preview.scanner.android.device.domain.Device
import sergio.sastre.composable.preview.scanner.android.device.domain.Dimensions
import sergio.sastre.composable.preview.scanner.android.device.domain.GetDeviceByIdentifier
import sergio.sastre.composable.preview.scanner.android.device.domain.Identifier
import sergio.sastre.composable.preview.scanner.android.device.domain.Orientation.LANDSCAPE
import sergio.sastre.composable.preview.scanner.android.device.domain.Orientation.PORTRAIT
import sergio.sastre.composable.preview.scanner.android.device.domain.Shape.NOTROUND
import sergio.sastre.composable.preview.scanner.android.device.domain.Type
import sergio.sastre.composable.preview.scanner.android.device.domain.Type.*
import sergio.sastre.composable.preview.scanner.android.device.domain.Unit.PX

enum class XR(
    override val device: Device
) : GetDeviceByIdentifier<XR> {

    XR_DEVICE(
        Device(
            identifier = Identifier.XR_DEVICE,
            dimensions = Dimensions(
                width = 2560f,
                height = 2558f,
                unit = PX
            ),
            densityDpi = 320,
            orientation = LANDSCAPE,
            shape = NOTROUND,
            chinSize = 0,
            type = XR
        )
    )
}