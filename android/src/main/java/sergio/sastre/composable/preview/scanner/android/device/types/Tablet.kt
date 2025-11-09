package sergio.sastre.composable.preview.scanner.android.device.types

import sergio.sastre.composable.preview.scanner.android.device.domain.ChinSize
import sergio.sastre.composable.preview.scanner.android.device.domain.Device
import sergio.sastre.composable.preview.scanner.android.device.domain.Dimensions
import sergio.sastre.composable.preview.scanner.android.device.domain.GetDeviceByIdentifier
import sergio.sastre.composable.preview.scanner.android.device.domain.Identifier
import sergio.sastre.composable.preview.scanner.android.device.domain.Orientation.LANDSCAPE
import sergio.sastre.composable.preview.scanner.android.device.domain.Orientation.PORTRAIT
import sergio.sastre.composable.preview.scanner.android.device.domain.Shape.NOTROUND
import sergio.sastre.composable.preview.scanner.android.device.domain.Type.TABLET
import sergio.sastre.composable.preview.scanner.android.device.domain.Unit.PX

enum class Tablet(
    override val device: Device
): GetDeviceByIdentifier<Tablet> {

    PIXEL_TABLET(
        Device(
            identifier = Identifier.PIXEL_TABLET,
            dimensions = Dimensions(
                width = 2560f,
                height = 1600f,
                unit = PX
            ),
            densityDpi = 320,
            orientation = LANDSCAPE,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = TABLET
        )
    ),

    PIXEL_C(
        Device(
            identifier = Identifier.PIXEL_C,
            dimensions = Dimensions(
                width = 2560f,
                height = 1800f,
                unit = PX
            ),
            densityDpi = 320,
            orientation = LANDSCAPE,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = TABLET
        )
    ),

    PIXEL_9_PRO_FOLD(
        Device(
            identifier = Identifier.PIXEL_9_PRO_FOLD,
            dimensions = Dimensions(
                width = 2076f,
                height = 2152f,
                unit = PX
            ),
            densityDpi = 420,
            orientation = LANDSCAPE,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = TABLET
        )
    ),

    NEXUS_7_2012(
        Device(
            identifier = Identifier.NEXUS_7_2012,
            dimensions = Dimensions(
                width = 800f,
                height = 1280f,
                unit = PX
            ),
            densityDpi = 220,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = TABLET
        )
    ),

    NEXUS_7_2013(
        Device(
            identifier = Identifier.NEXUS_7_2013,
            dimensions = Dimensions(
                width = 1200f,
                height = 1920f,
                unit = PX
            ),
            densityDpi = 320,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = TABLET
        )
    ),

    NEXUS_9(
        Device(
            identifier = Identifier.NEXUS_9,
            dimensions = Dimensions(
                width = 2048f,
                height = 1536f,
                unit = PX
            ),
            densityDpi = 320,
            orientation = LANDSCAPE,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = TABLET
        )
    ),

    NEXUS_10(
        Device(
            identifier = Identifier.NEXUS_10,
            dimensions = Dimensions(
                width = 2560f,
                height = 1600f,
                unit = PX
            ),
            densityDpi = 320,
            orientation = LANDSCAPE,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = TABLET
        )
    );
}