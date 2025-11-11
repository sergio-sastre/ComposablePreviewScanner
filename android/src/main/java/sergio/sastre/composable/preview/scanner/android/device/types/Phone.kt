package sergio.sastre.composable.preview.scanner.android.device.types

import sergio.sastre.composable.preview.scanner.android.device.domain.ChinSize
import sergio.sastre.composable.preview.scanner.android.device.domain.Cutout.PUNCH_HOLE
import sergio.sastre.composable.preview.scanner.android.device.domain.Device
import sergio.sastre.composable.preview.scanner.android.device.domain.Dimensions
import sergio.sastre.composable.preview.scanner.android.device.domain.GetDeviceByIdentifier
import sergio.sastre.composable.preview.scanner.android.device.domain.Identifier
import sergio.sastre.composable.preview.scanner.android.device.domain.Orientation.LANDSCAPE
import sergio.sastre.composable.preview.scanner.android.device.domain.Orientation.PORTRAIT
import sergio.sastre.composable.preview.scanner.android.device.domain.ScreenRatio.LONG
import sergio.sastre.composable.preview.scanner.android.device.domain.ScreenRatio.NOTLONG
import sergio.sastre.composable.preview.scanner.android.device.domain.ScreenSize.NORMAL
import sergio.sastre.composable.preview.scanner.android.device.domain.Shape.NOTROUND
import sergio.sastre.composable.preview.scanner.android.device.domain.Type.PHONE
import sergio.sastre.composable.preview.scanner.android.device.domain.Unit.PX

enum class Phone(
    override val device: Device
): GetDeviceByIdentifier<Phone> {

    GALAXY_NEXUS(
        Device(
            identifier = Identifier.GALAXY_NEXUS,
            dimensions = Dimensions(
                width = 720f,
                height = 1280f,
                unit = PX
            ),
            densityDpi = 320,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),

            type = PHONE
        )
    ),

    NEXUS_ONE(
        Device(
            identifier = Identifier.NEXUS_ONE,
            dimensions = Dimensions(
                width = 480f,
                height = 800f,
                unit = PX
            ),
            densityDpi = 240,
            orientation = PORTRAIT,
            screenRatio = LONG,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),

    NEXUS_S(
        Device(
            identifier = Identifier.NEXUS_S,
            dimensions = Dimensions(
                width = 480f,
                height = 800f,
                unit = PX
            ),
            densityDpi = 240,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),

    NEXUS_4(
        Device(
            identifier = Identifier.NEXUS_4,
            dimensions = Dimensions(
                width = 768f,
                height = 1280f,
                unit = PX
            ),
            densityDpi = 480,
            orientation = PORTRAIT,
            screenSize = NORMAL,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),

    NEXUS_5(
        Device(
            identifier = Identifier.NEXUS_5,
            dimensions = Dimensions(
                width = 1080f,
                height = 2340f,
                unit = PX
            ),
            densityDpi = 480,
            orientation = PORTRAIT,
            screenRatio = NOTLONG,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),
    NEXUS_5X(
        Device(
            identifier = Identifier.NEXUS_5X,
            dimensions = Dimensions(
                width = 1080f,
                height = 1920f,
                unit = PX
            ),
            densityDpi = 420,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),
    NEXUS_6(
        Device(
            identifier = Identifier.NEXUS_6,
            dimensions = Dimensions(
                width = 1440f,
                height = 2560f,
                unit = PX
            ),
            densityDpi = 560,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),
    NEXUS_6P(
        Device(
            identifier = Identifier.NEXUS_6P,
            dimensions = Dimensions(
                width = 1440f,
                height = 2560f,
                unit = PX
            ),
            densityDpi = 560,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),
    PIXEL(
        Device(
            identifier = Identifier.PIXEL,
            dimensions = Dimensions(
                width = 1080f,
                height = 1920f,
                unit = PX
            ),
            densityDpi = 420,
            orientation = PORTRAIT,
            screenRatio = NOTLONG,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),
    PIXEL_XL(
        Device(
            identifier = Identifier.PIXEL_XL,
            dimensions = Dimensions(
                width = 1440f,
                height = 2560f,
                unit = PX
            ),
            densityDpi = 560,
            orientation = PORTRAIT,
            screenRatio = NOTLONG,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),
    PIXEL_2(
        Device(
            identifier = Identifier.PIXEL_2,
            dimensions = Dimensions(
                width = 1080f,
                height = 1920f,
                unit = PX
            ),
            densityDpi = 420,
            orientation = PORTRAIT,
            screenRatio = NOTLONG,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),
    PIXEL_2_XL(
        Device(
            identifier = Identifier.PIXEL_2_XL,
            dimensions = Dimensions(
                width = 1440f,
                height = 2880f,
                unit = PX
            ),
            densityDpi = 560,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),
    PIXEL_3(
        Device(
            identifier = Identifier.PIXEL_3,
            dimensions = Dimensions(
                width = 1080f,
                height = 2160f,
                unit = PX
            ),
            densityDpi = 440,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),

    PIXEL_3A(
        Device(
            identifier = Identifier.PIXEL_3A,
            dimensions = Dimensions(
                width = 1080f,
                height = 2220f,
                unit = PX
            ),
            densityDpi = 440,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),

    // NOTE: Has a cutout up similar to Cutout.DOUBLE, but only up. Undefined yet
    PIXEL_3_XL(
        Device(
            identifier = Identifier.PIXEL_3_XL,
            dimensions = Dimensions(
                width = 1440f,
                height = 2960f,
                unit = PX
            ),
            densityDpi = 560,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),
    PIXEL_3A_XL(
        Device(
            identifier = Identifier.PIXEL_3A_XL,
            dimensions = Dimensions(
                width = 1080f,
                height = 2160f,
                unit = PX
            ),
            densityDpi = 420,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),
    PIXEL_4(
        Device(
            identifier = Identifier.PIXEL_4,
            dimensions = Dimensions(
                width = 1080f,
                height = 2280f,
                unit = PX
            ),
            densityDpi = 440,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),
    PIXEL_4A(
        Device(
            identifier = Identifier.PIXEL_4A,
            dimensions = Dimensions(
                width = 1080f,
                height = 2340f,
                unit = PX
            ),
            densityDpi = 440,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            cutout = PUNCH_HOLE,
            type = PHONE
        )
    ),
    PIXEL_4_XL(
        Device(
            identifier = Identifier.PIXEL_4_XL,
            dimensions = Dimensions(
                width = 1440f,
                height = 3040f,
                unit = PX
            ),
            densityDpi = 560,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),
    PIXEL_5(
        Device(
            identifier = Identifier.PIXEL_5,
            dimensions = Dimensions(
                width = 1080f,
                height = 2340f,
                unit = PX
            ),
            densityDpi = 440,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            cutout = PUNCH_HOLE,
            type = PHONE
        )
    ),

    // NOTE: Has a cutout up in the middle. Still undefined
    PIXEL_6(
        Device(
            identifier = Identifier.PIXEL_6,
            dimensions = Dimensions(
                width = 1080f,
                height = 2400f,
                unit = PX
            ),
            densityDpi = 420,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),

    // NOTE: Has a cutout up in the middle. Still undefined
    PIXEL_6A(
        Device(
            identifier = Identifier.PIXEL_6A,
            dimensions = Dimensions(
                width = 1080f,
                height = 2400f,
                unit = PX
            ),
            densityDpi = 420,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),

    // NOTE: Has a cutout up in the middle. Still undefined
    PIXEL_6_PRO(
        Device(
            identifier = Identifier.PIXEL_6_PRO,
            dimensions = Dimensions(
                width = 1440f,
                height = 3120f,
                unit = PX
            ),
            densityDpi = 560,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),

    // NOTE: Has a cutout up in the middle. Still undefined
    PIXEL_7(
        Device(
            identifier = Identifier.PIXEL_7,
            dimensions = Dimensions(
                width = 1080f,
                height = 2400f,
                unit = PX
            ),
            densityDpi = 420,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),

    // NOTE: Has a cutout up in the middle. Still undefined
    PIXEL_7A(
        Device(
            identifier = Identifier.PIXEL_7A,
            dimensions = Dimensions(
                width = 1080f,
                height = 2400f,
                unit = PX
            ),
            densityDpi = 420,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),

    // NOTE: Has a cutout up in the middle. Still undefined
    PIXEL_7_PRO(
        Device(
            identifier = Identifier.PIXEL_7_PRO,
            dimensions = Dimensions(
                width = 1440f,
                height = 3120f,
                unit = PX
            ),
            densityDpi = 560,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),

    // NOTE: Has a cutout up in the middle. Still undefined
    PIXEL_8(
        Device(
            identifier = Identifier.PIXEL_8,
            dimensions = Dimensions(
                width = 1080f,
                height = 2400f,
                unit = PX
            ),
            densityDpi = 420,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),
    PIXEL_8A(
        Device(
            identifier = Identifier.PIXEL_8A,
            dimensions = Dimensions(
                width = 1080f,
                height = 2400f,
                unit = PX
            ),
            densityDpi = 420,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),

    // NOTE: Has a cutout up in the middle. Still undefined
    PIXEL_8_PRO(
        Device(
            identifier = Identifier.PIXEL_8_PRO,
            dimensions = Dimensions(
                width = 1344f,
                height = 2992f,
                unit = PX
            ),
            densityDpi = 480,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),
    PIXEL_9A(
        Device(
            identifier = Identifier.PIXEL_9A,
            dimensions = Dimensions(
                width = 1080f,
                height = 2424f,
                unit = PX
            ),
            densityDpi = 420,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),
    PIXEL_9(
        Device(
            identifier = Identifier.PIXEL_9,
            dimensions = Dimensions(
                width = 1080f,
                height = 2424f,
                unit = PX
            ),
            densityDpi = 420,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),
    PIXEL_9_PRO(
        Device(
            identifier = Identifier.PIXEL_9_PRO,
            dimensions = Dimensions(
                width = 1280f,
                height = 2856f,
                unit = PX
            ),
            densityDpi = 480,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),
    PIXEL_9_PRO_XL(
        Device(
            identifier = Identifier.PIXEL_9_PRO_XL,
            dimensions = Dimensions(
                width = 1314f,
                height = 2992f,
                unit = PX
            ),
            densityDpi = 480,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),
    PIXEL_FOLD(
        Device(
            identifier = Identifier.PIXEL_FOLD,
            dimensions = Dimensions(
                width = 2208f,
                height = 1840f,
                unit = PX
            ),
            densityDpi = 420,
            orientation = LANDSCAPE,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    );
}