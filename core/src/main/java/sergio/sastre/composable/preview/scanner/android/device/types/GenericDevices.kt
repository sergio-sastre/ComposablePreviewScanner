package sergio.sastre.composable.preview.scanner.android.device.types

import sergio.sastre.composable.preview.scanner.android.device.domain.ChinSize
import sergio.sastre.composable.preview.scanner.android.device.domain.Device
import sergio.sastre.composable.preview.scanner.android.device.domain.Dimensions
import sergio.sastre.composable.preview.scanner.android.device.domain.GetDeviceByIdentifier
import sergio.sastre.composable.preview.scanner.android.device.domain.Identifier
import sergio.sastre.composable.preview.scanner.android.device.domain.Orientation.LANDSCAPE
import sergio.sastre.composable.preview.scanner.android.device.domain.Orientation.PORTRAIT
import sergio.sastre.composable.preview.scanner.android.device.domain.ScreenRatio
import sergio.sastre.composable.preview.scanner.android.device.domain.ScreenRatio.LONG
import sergio.sastre.composable.preview.scanner.android.device.domain.Shape.NOTROUND
import sergio.sastre.composable.preview.scanner.android.device.domain.Type.FOLDABLE
import sergio.sastre.composable.preview.scanner.android.device.domain.Type.PHONE
import sergio.sastre.composable.preview.scanner.android.device.domain.Type.TABLET
import sergio.sastre.composable.preview.scanner.android.device.domain.Unit.PX

enum class GenericDevices(
    override val device: Device
) : GetDeviceByIdentifier<GenericDevices> {

    MEDIUM_TABLET(
        Device(
            identifier = Identifier.MEDIUM_TABLET,
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

    SMALL_PHONE(
        Device(
            identifier = Identifier.SMALL_PHONE,
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

    MEDIUM_PHONE(
        Device(
            identifier = Identifier.MEDIUM_PHONE,
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

    RESIZABLE_EXPERIMENTAL(
        Device(
            identifier = Identifier.RESIZABLE_EXPERIMENTAL,
            dimensions = Dimensions(
                width = 1084f,
                height = 2400f,
                unit = PX
            ),
            densityDpi = 420,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = FOLDABLE
        )
    ),

    FWVGA_3_7IN_SLIDER (
        Device(
            identifier = Identifier.FWVGA_3_7IN_SLIDER,
            dimensions = Dimensions(
                width = 480f,
                height = 854f,
                unit = PX
            ),
            densityDpi = 240,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),

    FWVGA_5_4IN (
        Device(
            identifier = Identifier.FWVGA_5_4IN,
            dimensions = Dimensions(
                width = 480f,
                height = 854f,
                unit = PX
            ),
            densityDpi = 160,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),

    QVGA_2_7IN (
        Device(
            identifier = Identifier.QVGA_2_7IN,
            dimensions = Dimensions(
                width = 240f,
                height = 320f,
                unit = PX
            ),
            densityDpi = 120,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),

    HVGA_3_2IN_SLIDER_ADP1 (
        Device(
            identifier = Identifier.HVGA_3_2IN_ADP1,
            dimensions = Dimensions(
                width = 320f,
                height = 480f,
                unit = PX
            ),
            densityDpi = 160,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),

    QVGA_2_7IN_SLIDER (
        Device(
            identifier = Identifier.QVGA_2_7IN_SLIDER,
            dimensions = Dimensions(
                width = 240f,
                height = 320f,
                unit = PX
            ),
            densityDpi = 120,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),

    QVGA_3_2IN_ADP2 (
        Device(
            identifier = Identifier.QVGA_3_2IN_ADP2,
            dimensions = Dimensions(
                width = 320f,
                height = 480f,
                unit = PX
            ),
            densityDpi = 160,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),

    WQVGA_3_3IN (
        Device(
            identifier = Identifier.WQVGA_3_3IN,
            dimensions = Dimensions(
                width = 240f,
                height = 400f,
                unit = PX
            ),
            densityDpi = 120,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),

    WQVGA_3_4IN (
        Device(
            identifier = Identifier.WQVGA_3_4IN,
            dimensions = Dimensions(
                width = 240f,
                height = 432f,
                unit = PX
            ),
            densityDpi = 120,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),

    WSVGA_TABLET_7IN (
        Device(
            identifier = Identifier.WSVGA_TABLET_7IN,
            dimensions = Dimensions(
                width = 1024f,
                height = 600f,
                unit = PX
            ),
            densityDpi = 160,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = TABLET
        )
    ),

    NEXUS_ONE_WVGA_3_7IN (
        Device(
            identifier = Identifier.NEXUS_ONE_WVGA_3_7IN,
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

    WVGA_5_1IN (
        Device(
            identifier = Identifier.WVGA_5_1IN,
            dimensions = Dimensions(
                width = 480f,
                height = 800f,
                unit = PX
            ),
            densityDpi = 160,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),

    WXGA_4_7IN (
        Device(
            identifier = Identifier.WXGA_4_7IN,
            dimensions = Dimensions(
                width = 1280f,
                height = 720f,
                unit = PX
            ),
            densityDpi = 320,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = PHONE
        )
    ),

    WXGA_TABLET_10_1IN (
        Device(
            identifier = Identifier.WXGA_TABLET_10_1IN,
            dimensions = Dimensions(
                width = 1280f,
                height = 800f,
                unit = PX
            ),
            densityDpi = 160,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = TABLET
        )
    ),

    NEXUS_S_WVGA_4IN (
        Device(
            identifier = Identifier.NEXUS_S_WVGA_4IN,
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

    GALAXY_NEXUS_4_65IN_720P (
        Device(
            identifier = Identifier.GALAXY_NEXUS_4_65IN_720P,
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

    FREEFORM_13_5IN (
        Device(
            identifier = Identifier.FREEFORM_13_5IN,
            dimensions = Dimensions(
                width = 2560f,
                height = 1440f,
                unit = PX
            ),
            densityDpi = 240,
            orientation = LANDSCAPE,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = FOLDABLE
        )
    ),

    FOLD_OUT_8IN (
        Device(
            identifier = Identifier.FOLD_OUT_8IN,
            dimensions = Dimensions(
                width = 2200f,
                height = 2480f,
                unit = PX
            ),
            densityDpi = 420,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = FOLDABLE
        )
    ),

    FOLD_IN_WITH_OUTER_DISPLAY_7_6IN (
        Device(
            identifier = Identifier.FOLD_IN_WITH_OUTER_DISPLAY_7_6IN,
            dimensions = Dimensions(
                width = 1768f,
                height = 2208f,
                unit = PX
            ),
            densityDpi = 420,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = FOLDABLE
        )
    ),

    HORIZONTAL_FOLD_IN_6_7IN (
        Device(
            identifier = Identifier.HORIZONTAL_FOLD_IN_6_7IN,
            dimensions = Dimensions(
                width = 1080f,
                height = 2636f,
                unit = PX
            ),
            densityDpi = 480,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = FOLDABLE
        )
    ),

    ROLLABLE_7_4IN (
        Device(
            identifier = Identifier.ROLLABLE_7_4IN,
            dimensions = Dimensions(
                width = 1600f,
                height = 2428f,
                unit = PX
            ),
            densityDpi = 420,
            orientation = PORTRAIT,
            shape = NOTROUND,
            chinSize = ChinSize(0F, PX),
            type = FOLDABLE
        )
    ),
}