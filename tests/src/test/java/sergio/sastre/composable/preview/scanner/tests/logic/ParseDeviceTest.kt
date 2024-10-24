package sergio.sastre.composable.preview.scanner.tests.logic

import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.google.testing.junit.testparameterinjector.TestParameter
import org.junit.runner.RunWith
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import junit.framework.TestCase.assertEquals
import org.junit.Test
import sergio.sastre.composable.preview.scanner.android.device.domain.Cutout
import sergio.sastre.composable.preview.scanner.android.device.domain.Dimensions
import sergio.sastre.composable.preview.scanner.android.device.domain.Navigation
import sergio.sastre.composable.preview.scanner.android.device.domain.Orientation
import sergio.sastre.composable.preview.scanner.android.device.domain.Orientation.LANDSCAPE
import sergio.sastre.composable.preview.scanner.android.device.domain.Orientation.PORTRAIT
import sergio.sastre.composable.preview.scanner.android.device.domain.Shape
import sergio.sastre.composable.preview.scanner.android.device.domain.Shape.NORMAL
import sergio.sastre.composable.preview.scanner.android.device.domain.Shape.ROUND
import sergio.sastre.composable.preview.scanner.android.device.ParseDevice
import sergio.sastre.composable.preview.scanner.android.device.domain.Type
import sergio.sastre.composable.preview.scanner.android.device.domain.Type.DESKTOP
import sergio.sastre.composable.preview.scanner.android.device.domain.Type.FOLDABLE
import sergio.sastre.composable.preview.scanner.android.device.domain.Type.PHONE
import sergio.sastre.composable.preview.scanner.android.device.domain.Type.TABLET
import sergio.sastre.composable.preview.scanner.android.device.domain.Unit.DP
import sergio.sastre.composable.preview.scanner.android.device.domain.Unit.PX
import sergio.sastre.composable.preview.scanner.android.device.types.Phone

@RunWith(TestParameterInjector::class)
class ParseDeviceTest {

    companion object {
        const val DIMENS = ",height=100dp,width=200dp"
    }

    enum class CustomDimensions(
        val deviceSpec: String,
        val expectedDimensions: Dimensions
    ) {
        SpacedSpec("spec: height=100, width=200, unit=dp", Dimensions(100f, 200f, DP)),
        DimensionsInDpImplicit("spec:height=100,width=200,unit=dp", Dimensions(100f, 200f, DP)),
        DimensionsInDpExplicit("spec:height=100dp,width=200dp", Dimensions(100f, 200f, DP)),
        DimensionsInPxImplicit("spec:height=100,width=200,unit=px", Dimensions(100f, 200f, PX)),
        DimensionsInPxExplicit("spec:height=100px,width=200px", Dimensions(100f, 200f, PX)),
        FloatDimensionsInDp("spec:height=100.5dp,width=200.1dp", Dimensions(100.5f, 200.1f, DP)),
    }
    @Test
    fun `GIVEN device height, extract its value`(
        @TestParameter customDimensions: CustomDimensions
    ) {
        val expectedDevice =
            ParseDevice.from(customDimensions.deviceSpec)!!

        assert(
            expectedDevice.dimensions.height == customDimensions.expectedDimensions.height
        )
        assert(
            expectedDevice.dimensions.width == customDimensions.expectedDimensions.width
        )
        assert(
            expectedDevice.dimensions.unit == customDimensions.expectedDimensions.unit
        )
    }

    enum class CustomDpi(
        val deviceSpec: String,
        val expectedDpi: Int
    ) {
        NoDpi("spec:height=100dp,width=200dp", 420),
        WithDpi("spec:dpi=320$DIMENS", 320),
    }
    @Test
    fun `GIVEN dpi, extract its value`(
        @TestParameter customDpi: CustomDpi
    ) {
        val expectedDevice =
            ParseDevice.from(customDpi.deviceSpec)!!

        assert(
            expectedDevice.densityDpi == customDpi.expectedDpi
        )
    }

    enum class CustomShape(
        val deviceSpec: String,
        val expectedShapeValue: Shape
    ) {
        // Shape can only be defined one way
        NoShapeDefaultsNormal("spec:height=100dp,width=200dp", NORMAL),
        IsRoundTrue("spec:isRound=true$DIMENS", ROUND),
        IsRoundFalse("spec:isRound=false$DIMENS", NORMAL),
        ShapeNormal("spec:shape=Normal$DIMENS", NORMAL),
        ShapeRound("spec:shape=Round$DIMENS", ROUND),
    }
    @Test
    fun `GIVEN shape, extract its value`(
        @TestParameter customShape: CustomShape
    ) {
        val expectedDevice =
            ParseDevice.from(customShape.deviceSpec)!!

        assert(
            expectedDevice.shape == customShape.expectedShapeValue
        )
    }

    enum class CustomType(
        val deviceSpec: String,
        val expectedTypeValue: Type?
    ) {
        TypeDesktop("spec:id=reference_desktop$DIMENS", DESKTOP),
        TypeFoldable("spec:id=reference_foldable$DIMENS", FOLDABLE),
        TypePhone("spec:id=reference_phone$DIMENS", PHONE),
        TypeTablet("spec:id=reference_tablet$DIMENS", TABLET),
    }
    @Test
    fun `GIVEN type, extract its value`(
        @TestParameter customType: CustomType
    ) {
        val expectedDevice =
            ParseDevice.from(customType.deviceSpec)!!

        assert(
            expectedDevice.type == customType.expectedTypeValue
        )
    }

    enum class CustomOrientation(
        val deviceSpec: String,
        val expectedOrientationValue: Orientation
    ) {
        NoOrientationDefaultsPortrait("spec:height=200dp,width=100dp", PORTRAIT),
        NoOrientationDefaultsLandscape("spec:height=100dp,width=200dp", LANDSCAPE),
        Portrait("spec:orientation=portrait$DIMENS", PORTRAIT),
        Landscape("spec:orientation=landscape$DIMENS", LANDSCAPE),
    }
    @Test
    fun `GIVEN orientation, extract its value`(
        @TestParameter customOrientation: CustomOrientation
    ) {
        val expectedDevice =
            ParseDevice.from(customOrientation.deviceSpec)!!

        assert(
            expectedDevice.orientation == customOrientation.expectedOrientationValue
        )
    }

    enum class CustomCutouts(
        val deviceSpec: String,
        val expectedCutoutValue: Cutout
    ) {
        NoCutout("spec:height=200dp,width=100dp", Cutout.NONE),
        None("spec:cutout=none$DIMENS", Cutout.NONE),
        Corner("spec:cutout=corner$DIMENS", Cutout.CORNER),
        Double("spec:cutout=double$DIMENS", Cutout.DOUBLE),
        PunchHole("spec:cutout=punch_hole$DIMENS", Cutout.PUNCH_HOLE),
        Tall("spec:cutout=tall$DIMENS", Cutout.TALL),
    }
    @Test
    fun `GIVEN cutout, extract its value`(
        @TestParameter customCutout: CustomCutouts
    ) {
        val expectedDevice =
            ParseDevice.from(customCutout.deviceSpec)!!

        assert(
            expectedDevice.cutout == customCutout.expectedCutoutValue
        )
    }

    enum class CustomChinSize(
        val deviceSpec: String,
        val expectedChinSizeValue: Int
    ) {
        NoChinSizeDefaults0("spec:height=200dp,width=100dp", 0),
        ChinSize("spec:chinSize=8dp$DIMENS", 8),
    }
    @Test
    fun `GIVEN chinSize, extract its value`(
        @TestParameter customChinSize: CustomChinSize
    ) {
        val expectedDevice =
            ParseDevice.from(customChinSize.deviceSpec)!!

        assert(
            expectedDevice.chinSize == customChinSize.expectedChinSizeValue
        )
    }

    enum class CustomNavigation(
        val deviceSpec: String,
        val expectedNavigationValue: Navigation
    ) {
        NoNavigationDefaultsGesture("spec:height=200dp,width=100dp", Navigation.GESTURE),
        NavigationGesture("spec:navigation=gesture$DIMENS", Navigation.GESTURE),
        NavigationButtons("spec:navigation=buttons$DIMENS", Navigation.BUTTONS),
    }
    @Test
    fun `GIVEN navigation, extract its value`(
        @TestParameter customNavigation: CustomNavigation
    ) {
        val expectedDevice =
            ParseDevice.from(customNavigation.deviceSpec)!!

        assert(
            expectedDevice.navigation == customNavigation.expectedNavigationValue
        )
    }

    enum class DeviceParent(
        val deviceSpec: String,
        val expectedOrientation: Orientation,
        val expectedNavigation: Navigation
    ) {
        JustParent("spec:parent=Nexus One", PORTRAIT, Navigation.GESTURE),
        ParentAndOrientationReversed("spec:orientation=landscape,parent=Nexus One", LANDSCAPE, Navigation.GESTURE),
        ParentAndOrientation("spec:parent=Nexus One,orientation=landscape", LANDSCAPE, Navigation.GESTURE),
        ParentAndNavigation("spec:parent=Nexus One,navigation=buttons", PORTRAIT, Navigation.BUTTONS),
        ParentAndOrientationAndNavigation("spec:parent=Nexus One,orientation=landscape,navigation=buttons", LANDSCAPE, Navigation.BUTTONS),
    }
    @Test
    fun `GIVEN spec parent, has expected device with given orientation and navigation`(
        @TestParameter deviceParent: DeviceParent
    ) {
        val nexusOne = Phone.NEXUS_ONE.device
        val expectedDevice =
            ParseDevice.from(deviceParent.deviceSpec)

        assertEquals(expectedDevice!!.id, nexusOne.id)
        assertEquals(expectedDevice.navigation, deviceParent.expectedNavigation)
        assertEquals(expectedDevice.orientation, deviceParent.expectedOrientation)
    }

    enum class DeviceIdMapping(
        val deviceId: String,
        val roborazziDeviceQualifier: String
    ) {
        NexusOne("id:Nexus One", RobolectricDeviceQualifiers.NexusOne),
        Nexus7("id:Nexus 7", RobolectricDeviceQualifiers.Nexus7),
        Nexus9("id:Nexus 9", RobolectricDeviceQualifiers.Nexus9),
        PixelC("id:pixel_c", RobolectricDeviceQualifiers.PixelC),
        PixelXL("id:pixel_xl", RobolectricDeviceQualifiers.PixelXL),
        Pixel4("id:pixel_4", RobolectricDeviceQualifiers.Pixel4),
        Pixel4A("id:pixel_4a", RobolectricDeviceQualifiers.Pixel4a),
        Pixel4XL("id:pixel_4_xl", RobolectricDeviceQualifiers.Pixel4XL),
        Pixel5("id:pixel_5", RobolectricDeviceQualifiers.Pixel5),
        Pixel6("id:pixel_6", RobolectricDeviceQualifiers.Pixel6),
        Pixel6Pro("id:pixel_6_pro", RobolectricDeviceQualifiers.Pixel6Pro),
        Pixel6a("id:pixel_6a", RobolectricDeviceQualifiers.Pixel6a),
        Pixel7("id:pixel_7", RobolectricDeviceQualifiers.Pixel7),
        Pixel7Pro("id:pixel_7_pro", RobolectricDeviceQualifiers.Pixel7Pro),

        WearOSLargeRound("id:wearos_large_round", RobolectricDeviceQualifiers.WearOSLargeRound),
        WearOSSmallRound("id:wearos_small_round", RobolectricDeviceQualifiers.WearOSSmallRound),
        WearOSSquare("id:wearos_square", RobolectricDeviceQualifiers.WearOSSquare),
        WearOSRectangular("id:wearos_rectangular", RobolectricDeviceQualifiers.WearOSRectangular),

        SmallDesktop("id:desktop_small", RobolectricDeviceQualifiers.SmallDesktop),
        MediumDesktop("id:desktop_medium", RobolectricDeviceQualifiers.MediumDesktop),
        LargeDesktop("id:desktop_large", RobolectricDeviceQualifiers.LargeDesktop),
        Automotive1024pLandscape("id:automotive_1024p_landscape", RobolectricDeviceQualifiers.Automotive1024plandscape)
    }
    @Test
    fun `GIVEN device id, WHEN converted to Dp, has expected Roborazzi height and width with error margin up to 1f`(
        @TestParameter deviceMapping: DeviceIdMapping
    ) {
        val expectedDeviceInDp =
            ParseDevice.from(deviceMapping.deviceId)!!.inDp()

        assertEquals(
            expectedDeviceInDp.dimensions.height, deviceMapping.roborazziDeviceQualifier.extractHeight(), 1f
        )
        assertEquals(
            expectedDeviceInDp.dimensions.width, deviceMapping.roborazziDeviceQualifier.extractWidth(), 1f
        )
    }

    @Test
    fun `GIVEN device WHEN converted to Dp AND back to Px THEN returns original device with error margin up to 2f`(
        @TestParameter deviceMapping: DeviceIdMapping
    ) {
        val originalDeviceInPx =
            ParseDevice.from(deviceMapping.deviceId)

        val deviceInDpAndBackInPx =
            originalDeviceInPx!!.inDp().inPx()

        assertEquals(
            originalDeviceInPx.dimensions.height, deviceInDpAndBackInPx.dimensions.height, 2f
        )
        assertEquals(
            originalDeviceInPx.dimensions.width, deviceInDpAndBackInPx.dimensions.width, 2f
        )
    }

    enum class DeviceNameMapping(
        val deviceName: String,
        val roborazziDeviceQualifier: String
    ) {
        NexusOne("name:Nexus One", RobolectricDeviceQualifiers.NexusOne),
        Nexus7("name:Nexus 7", RobolectricDeviceQualifiers.Nexus7),
        Nexus9("name:Nexus 9", RobolectricDeviceQualifiers.Nexus9),
        PixelC("name:Pixel C", RobolectricDeviceQualifiers.PixelC),
        PixelXL("name:Pixel XL", RobolectricDeviceQualifiers.PixelXL),
        Pixel4("name:Pixel 4", RobolectricDeviceQualifiers.Pixel4),
        Pixel4A("name:Pixel 4a", RobolectricDeviceQualifiers.Pixel4a),
        Pixel4XL("name:Pixel 4 XL", RobolectricDeviceQualifiers.Pixel4XL),
        Pixel5("name:Pixel 5", RobolectricDeviceQualifiers.Pixel5),
        Pixel6("name:Pixel 6", RobolectricDeviceQualifiers.Pixel6),
        Pixel6Pro("name:Pixel 6 Pro", RobolectricDeviceQualifiers.Pixel6Pro),
        Pixel6a("name:Pixel 6a", RobolectricDeviceQualifiers.Pixel6a),
        Pixel7("name:Pixel 7", RobolectricDeviceQualifiers.Pixel7),
        Pixel7Pro("name:Pixel 7 Pro", RobolectricDeviceQualifiers.Pixel7Pro),

        WearOSLargeRound("name:Wear OS Large Round", RobolectricDeviceQualifiers.WearOSLargeRound),
        WearOSSmallRound("name:Wear OS Small Round", RobolectricDeviceQualifiers.WearOSSmallRound),
        WearOSSquare("name:Wear OS Square", RobolectricDeviceQualifiers.WearOSSquare),
        WearOSRectangular("name:Wear OS Rectangular", RobolectricDeviceQualifiers.WearOSRectangular),

        SmallDesktop("name:Small Desktop", RobolectricDeviceQualifiers.SmallDesktop),
        MediumDesktop("name:Medium Desktop", RobolectricDeviceQualifiers.MediumDesktop),
        LargeDesktop("name:Large Desktop", RobolectricDeviceQualifiers.LargeDesktop),
        Automotive1024pLandscape("name:Automotive (1024p landscape)", RobolectricDeviceQualifiers.Automotive1024plandscape)
    }
    @Test
    fun `GIVEN device name, WHEN converted to Dp, has expected Roborazzi height and width with error margin up to 1f`(
        @TestParameter deviceMapping: DeviceNameMapping
    ) {
        val expectedDeviceInDp =
            ParseDevice.from(deviceMapping.deviceName)!!.inDp()

        assertEquals(
            expectedDeviceInDp.dimensions.height, deviceMapping.roborazziDeviceQualifier.extractHeight(), 1f
        )
        assertEquals(
            expectedDeviceInDp.dimensions.width, deviceMapping.roborazziDeviceQualifier.extractWidth(), 1f
        )
    }

    private fun String.extractWidth(): Float {
        val regex = "w(\\d+)dp".toRegex()
        return regex.find(this)!!.groupValues[1].toFloat()
    }

    private fun String.extractHeight(): Float {
        val regex = "h(\\d+)dp".toRegex()
        return regex.find(this)!!.groupValues[1].toFloat()
    }
}