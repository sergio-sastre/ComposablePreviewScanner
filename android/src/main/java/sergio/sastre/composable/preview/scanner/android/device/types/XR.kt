package sergio.sastre.composable.preview.scanner.android.device.types

import sergio.sastre.composable.preview.scanner.android.device.domain.ChinSize
import sergio.sastre.composable.preview.scanner.android.device.domain.Device
import sergio.sastre.composable.preview.scanner.android.device.domain.Dimensions
import sergio.sastre.composable.preview.scanner.android.device.domain.GetDeviceByIdentifier
import sergio.sastre.composable.preview.scanner.android.device.domain.Identifier
import sergio.sastre.composable.preview.scanner.android.device.domain.Orientation.LANDSCAPE
import sergio.sastre.composable.preview.scanner.android.device.domain.Shape.NOTROUND
import sergio.sastre.composable.preview.scanner.android.device.domain.Type.*
import sergio.sastre.composable.preview.scanner.android.device.domain.Unit.PX

private val XR_DEFAULT = Device(
    identifier = null,
    dimensions = Dimensions(
        width = 2560f,
        height = 2558f,
        unit = PX
    ),
    densityDpi = 320,
    orientation = LANDSCAPE,
    shape = NOTROUND,
    chinSize = ChinSize(0F, PX),
    type = XR
)

enum class XR(
    override val device: Device
) : GetDeviceByIdentifier<XR> {

    @Deprecated(
        message = "Replaced by XR_HEADSET in newer Android Studio versions",
        replaceWith = ReplaceWith("XR.XR_HEADSET")
    )
    XR_DEVICE(
        XR_DEFAULT.copy(identifier = Identifier.XR_DEVICE)
    ),

    XR_HEADSET(
        XR_DEFAULT.copy(identifier = Identifier.XR_HEADSET)
    );
}