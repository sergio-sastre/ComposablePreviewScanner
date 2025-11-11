package sergio.sastre.composable.preview.scanner.tests.api.main

import app.cash.paparazzi.DeviceConfig
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import sergio.sastre.composable.preview.scanner.android.device.DevicePreviewInfoParser
import sergio.sastre.composable.preview.scanner.android.device.domain.ChinSize
import sergio.sastre.composable.preview.scanner.android.device.domain.Cutout
import sergio.sastre.composable.preview.scanner.android.device.domain.Dimensions
import sergio.sastre.composable.preview.scanner.android.device.domain.Identifier
import sergio.sastre.composable.preview.scanner.android.device.domain.Navigation
import sergio.sastre.composable.preview.scanner.android.device.domain.Orientation
import sergio.sastre.composable.preview.scanner.android.device.domain.Orientation.LANDSCAPE
import sergio.sastre.composable.preview.scanner.android.device.domain.Orientation.PORTRAIT
import sergio.sastre.composable.preview.scanner.android.device.domain.Shape
import sergio.sastre.composable.preview.scanner.android.device.domain.Shape.NOTROUND
import sergio.sastre.composable.preview.scanner.android.device.domain.Shape.ROUND
import sergio.sastre.composable.preview.scanner.android.device.domain.Type
import sergio.sastre.composable.preview.scanner.android.device.domain.Type.DESKTOP
import sergio.sastre.composable.preview.scanner.android.device.domain.Type.FOLDABLE
import sergio.sastre.composable.preview.scanner.android.device.domain.Type.PHONE
import sergio.sastre.composable.preview.scanner.android.device.domain.Type.TABLET
import sergio.sastre.composable.preview.scanner.android.device.domain.Unit.DP
import sergio.sastre.composable.preview.scanner.android.device.domain.Unit.PX
import sergio.sastre.composable.preview.scanner.android.device.types.Phone
import sergio.sastre.composable.preview.scanner.android.device.types.XR

@RunWith(TestParameterInjector::class)
class DevicePreviewInfoParserTest {

    companion object {
        const val DIMENS_DP = ",height=100dp,width=200dp"
        const val DIMENS_PX = ",height=100px,width=200px"
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
            DevicePreviewInfoParser.parse(customDimensions.deviceSpec)!!

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
        WithDpi("spec:dpi=320$DIMENS_DP", 320),
    }

    @Test
    fun `GIVEN dpi, extract its value`(
        @TestParameter customDpi: CustomDpi
    ) {
        val expectedDevice =
            DevicePreviewInfoParser.parse(customDpi.deviceSpec)!!

        assert(
            expectedDevice.densityDpi == customDpi.expectedDpi
        )
    }

    enum class CustomShape(
        val deviceSpec: String,
        val expectedShapeValue: Shape
    ) {
        // Shape can only be defined one way
        NoShapeDefaultsNormal("spec:height=100dp,width=200dp", NOTROUND),
        IsRoundTrue("spec:isRound=true$DIMENS_DP", ROUND),
        IsRoundFalse("spec:isRound=false$DIMENS_DP", NOTROUND),
        ShapeNormal("spec:shape=Normal$DIMENS_DP", NOTROUND),
        ShapeRound("spec:shape=Round$DIMENS_DP", ROUND),
    }

    @Test
    fun `GIVEN shape, extract its value`(
        @TestParameter customShape: CustomShape
    ) {
        val expectedDevice =
            DevicePreviewInfoParser.parse(customShape.deviceSpec)!!

        assert(
            expectedDevice.shape == customShape.expectedShapeValue
        )
    }

    enum class CustomType(
        val deviceSpec: String,
        val expectedTypeValue: Type?
    ) {
        TypeDesktop("spec:id=reference_desktop$DIMENS_DP", DESKTOP),
        TypeFoldable("spec:id=reference_foldable$DIMENS_DP", FOLDABLE),
        TypePhone("spec:id=reference_phone$DIMENS_DP", PHONE),
        TypeTablet("spec:id=reference_tablet$DIMENS_DP", TABLET),
    }

    @Test
    fun `GIVEN type, extract its value`(
        @TestParameter customType: CustomType
    ) {
        val expectedDevice =
            DevicePreviewInfoParser.parse(customType.deviceSpec)!!

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
        Portrait("spec:orientation=portrait$DIMENS_DP", PORTRAIT),
        Landscape("spec:orientation=landscape$DIMENS_DP", LANDSCAPE),
    }

    @Test
    fun `GIVEN orientation, extract its value`(
        @TestParameter customOrientation: CustomOrientation
    ) {
        val expectedDevice =
            DevicePreviewInfoParser.parse(customOrientation.deviceSpec)!!

        assert(
            expectedDevice.orientation == customOrientation.expectedOrientationValue
        )
    }

    enum class CustomCutouts(
        val deviceSpec: String,
        val expectedCutoutValue: Cutout
    ) {
        NoCutout("spec:height=200dp,width=100dp", Cutout.NONE),
        None("spec:cutout=none$DIMENS_DP", Cutout.NONE),
        Corner("spec:cutout=corner$DIMENS_DP", Cutout.CORNER),
        Double("spec:cutout=double$DIMENS_DP", Cutout.DOUBLE),
        PunchHole("spec:cutout=punch_hole$DIMENS_DP", Cutout.PUNCH_HOLE),
        Tall("spec:cutout=tall$DIMENS_DP", Cutout.TALL),
    }

    @Test
    fun `GIVEN cutout, extract its value`(
        @TestParameter customCutout: CustomCutouts
    ) {
        val expectedDevice =
            DevicePreviewInfoParser.parse(customCutout.deviceSpec)!!

        assert(
            expectedDevice.cutout == customCutout.expectedCutoutValue
        )
    }

    enum class CustomChinSize(
        val deviceSpec: String,
        val expectedChinSize: ChinSize
    ) {
        DpNoChinSizeDefaults0("spec:height=200dp,width=100dp", ChinSize(0F, DP)),
        PxNoChinSizeDefaults0("spec:width=200px,height=2341px", ChinSize(0F, PX)),
        ChinSizeDp("spec:chinSize=8dp$DIMENS_DP", ChinSize(8F, DP)),
        ChinSizeFloat("spec:chinSize=8.1dp$DIMENS_DP", ChinSize(8.1F, DP)),
        ChinSizePx("spec:chinSize=8px$DIMENS_PX", ChinSize(8F, PX))
    }

    @Test
    fun `GIVEN chinSize, extract its value`(
        @TestParameter customChinSize: CustomChinSize
    ) {
        val expectedDevice =
            DevicePreviewInfoParser.parse(customChinSize.deviceSpec)!!

        assert(
            expectedDevice.chinSize.value == customChinSize.expectedChinSize.value
        )
        assert(
            expectedDevice.chinSize.unit == customChinSize.expectedChinSize.unit
        )
    }

    enum class CustomNavigation(
        val deviceSpec: String,
        val expectedNavigationValue: Navigation
    ) {
        NoNavigationDefaultsGesture("spec:height=200dp,width=100dp", Navigation.GESTURE),
        NavigationGesture("spec:navigation=gesture$DIMENS_DP", Navigation.GESTURE),
        NavigationButtons("spec:navigation=buttons$DIMENS_DP", Navigation.BUTTONS),
    }

    @Test
    fun `GIVEN navigation, extract its value`(
        @TestParameter customNavigation: CustomNavigation
    ) {
        val expectedDevice =
            DevicePreviewInfoParser.parse(customNavigation.deviceSpec)!!

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
        ParentAndOrientationReversed(
            "spec:orientation=landscape,parent=Nexus One",
            LANDSCAPE,
            Navigation.GESTURE
        ),
        ParentAndOrientation(
            "spec:parent=Nexus One,orientation=landscape",
            LANDSCAPE,
            Navigation.GESTURE
        ),
        ParentAndNavigation(
            "spec:parent=Nexus One,navigation=buttons",
            PORTRAIT,
            Navigation.BUTTONS
        ),
        ParentAndOrientationAndNavigation(
            "spec:parent=Nexus One,orientation = landscape,navigation=buttons",
            LANDSCAPE,
            Navigation.BUTTONS
        ),
    }

    @Test
    fun `GIVEN spec parent, has expected device with given orientation and navigation`(
        @TestParameter deviceParent: DeviceParent
    ) {
        val nexusOne = Phone.NEXUS_ONE.device
        val expectedDevice =
            DevicePreviewInfoParser.parse(deviceParent.deviceSpec)

        assertEquals(expectedDevice!!.identifier, nexusOne.identifier)
        assertEquals(expectedDevice.navigation, deviceParent.expectedNavigation)
        assertEquals(expectedDevice.orientation, deviceParent.expectedOrientation)
    }

    enum class DeviceIdMapping(
        val deviceId: String,
        val roborazziDeviceQualifier: String,
    ) {
        // Android previews have a very weird matching for this device; id:Nexus 7 and name:Nexus 7 map to different devices
        Nexus7Name("name:Nexus 7", RobolectricDeviceQualifiers.Nexus7),
        Nexus7Id("id:Nexus 7 2013", RobolectricDeviceQualifiers.Nexus7),

        NexusOne("id:Nexus One", RobolectricDeviceQualifiers.NexusOne),
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
        WearOSRect("id:wearos_rect", RobolectricDeviceQualifiers.WearOSRectangular),

        SmallDesktop("id:desktop_small", RobolectricDeviceQualifiers.SmallDesktop),
        MediumDesktop("id:desktop_medium", RobolectricDeviceQualifiers.MediumDesktop),
        LargeDesktop("id:desktop_large", RobolectricDeviceQualifiers.LargeDesktop),
        Automotive1024pLandscape(
            "id:automotive_1024p_landscape",
            RobolectricDeviceQualifiers.Automotive1024plandscape
        )
    }

    @Test
    fun `GIVEN device id, WHEN converted to Dp, has expected Roborazzi height and width with error margin up to 1f`(
        @TestParameter deviceMapping: DeviceIdMapping
    ) {
        val expectedDevice = DevicePreviewInfoParser.parse(deviceMapping.deviceId)!!
        val expectedDeviceInDp = expectedDevice.inDp()

        assertEquals(
            expectedDeviceInDp.dimensions.height,
            deviceMapping.roborazziDeviceQualifier.extractHeight(),
            1f
        )
        assertEquals(
            expectedDeviceInDp.dimensions.width,
            deviceMapping.roborazziDeviceQualifier.extractWidth(),
            1f
        )
        assertEquals(
            expectedDevice.screenRatio.name.lowercase(),
            deviceMapping.roborazziDeviceQualifier.extractScreenRatio()
        )
        assertEquals(
            expectedDevice.screenSize.name.lowercase(),
            deviceMapping.roborazziDeviceQualifier.extractScreenSize()
        )
    }

    enum class DeviceIdPaparazziMapping(
        val deviceId: String,
        val expectedDeviceConfig: DeviceConfig,
    ) {
        Nexus4("id:Nexus 4", DeviceConfig.NEXUS_4),
        Nexus5("id:Nexus 5", DeviceConfig.NEXUS_5),
        Nexus7("id:Nexus 7", DeviceConfig.NEXUS_7),
        Nexus7_2012("name:Nexus 7 (2012)", DeviceConfig.NEXUS_7_2012),
        Nexus10("id:Nexus 10", DeviceConfig.NEXUS_10),
        Pixel("id:pixel", DeviceConfig.PIXEL),
        PixelXL("id:pixel_xl", DeviceConfig.PIXEL_XL),
        Pixel2("id:pixel_2", DeviceConfig.PIXEL_2),
        Pixel2XL("id:pixel_2_xl", DeviceConfig.PIXEL_2_XL),
        PixelC("id:pixel_c", DeviceConfig.PIXEL_C),
        Pixel4("id:pixel_4", DeviceConfig.PIXEL_4),
        Pixel4A("id:pixel_4a", DeviceConfig.PIXEL_4A),
        Pixel4XL("id:pixel_4_xl", DeviceConfig.PIXEL_4_XL),
        Pixel5("id:pixel_5", DeviceConfig.PIXEL_5),
        Pixel6("id:pixel_6", DeviceConfig.PIXEL_6),
        Pixel6Pro("id:pixel_6_pro", DeviceConfig.PIXEL_6_PRO),
        WearOSSmallRound("id:wearos_small_round", DeviceConfig.WEAR_OS_SMALL_ROUND),
        WearOSSquare("id:wearos_square", DeviceConfig.WEAR_OS_SQUARE),
    }

    @Test
    fun `GIVEN device id, WHEN converted to Dp, has expected Paparazzi screen ratio`(
        @TestParameter deviceMapping: DeviceIdPaparazziMapping
    ) {
        val expectedDevice = DevicePreviewInfoParser.parse(deviceMapping.deviceId)!!

        assertEquals(
            expectedDevice.screenRatio.name.lowercase(),
            deviceMapping.expectedDeviceConfig.ratio.name.lowercase()
        )

        assertEquals(
            expectedDevice.screenSize.name.lowercase(),
            deviceMapping.expectedDeviceConfig.size.name.lowercase()
        )
    }

    @Test
    fun `GIVEN device in Px WHEN converted to Dp AND back to Px twice THEN returns original device with error margin up to 2f`() {
        val marginError = 2f
        val deviceString = "spec:width=300px,height=300px,chinSize=25.5px"
        val originalDeviceInPx =
            DevicePreviewInfoParser.parse(deviceString)

        val deviceInDp =
            originalDeviceInPx!!.inDp()

        val deviceBackInPx = deviceInDp.inPx()

        val deviceBackInDp = deviceBackInPx.inDp()

        // original
        assertEquals(
            originalDeviceInPx.dimensions.height, deviceBackInPx.dimensions.height, marginError
        )
        assertEquals(
            originalDeviceInPx.dimensions.width, deviceBackInPx.dimensions.width, marginError
        )
        assertEquals(
            originalDeviceInPx.chinSize.value, deviceBackInPx.chinSize.value, marginError
        )

        // in dp
        assertEquals(
            deviceInDp.dimensions.height, deviceBackInDp.dimensions.height, marginError
        )
        assertEquals(
            deviceInDp.dimensions.width, deviceBackInDp.dimensions.width, marginError
        )
        assertEquals(
            deviceInDp.chinSize.value, deviceBackInDp.chinSize.value, marginError
        )
    }

    @Test
    fun `GIVEN device in Dp WHEN converted to Px AND back to Dp twice THEN returns original device with error margin up to 2f`() {
        val marginError = 2f
        val deviceString = "spec:width=114.3dp,height=114.3dp,isRound=true,chinSize=9.7dp"
        val originalDeviceInDp =
            DevicePreviewInfoParser.parse(deviceString)

        val deviceInPx =
            originalDeviceInDp!!.inPx()

        val deviceBackInDp = deviceInPx.inDp()

        val deviceBackInPx = deviceBackInDp.inPx()

        // original
        assertEquals(
            originalDeviceInDp.dimensions.height, deviceBackInDp.dimensions.height, marginError
        )
        assertEquals(
            originalDeviceInDp.dimensions.width, deviceBackInDp.dimensions.width, marginError
        )
        assertEquals(
            originalDeviceInDp.chinSize.value, deviceBackInDp.chinSize.value, marginError
        )

        // in px
        assertEquals(
            deviceInPx.dimensions.height, deviceBackInPx.dimensions.height, marginError
        )
        assertEquals(
            deviceInPx.dimensions.width, deviceBackInPx.dimensions.width, marginError
        )
        assertEquals(
            deviceInPx.chinSize.value, deviceBackInPx.chinSize.value, marginError
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
        WearOSRectangular(
            "name:Wear OS Rectangular",
            RobolectricDeviceQualifiers.WearOSRectangular
        ),

        SmallDesktop("name:Small Desktop", RobolectricDeviceQualifiers.SmallDesktop),
        MediumDesktop("name:Medium Desktop", RobolectricDeviceQualifiers.MediumDesktop),
        LargeDesktop("name:Large Desktop", RobolectricDeviceQualifiers.LargeDesktop),
        Automotive1024pLandscape(
            "name:Automotive (1024p landscape)",
            RobolectricDeviceQualifiers.Automotive1024plandscape
        )
    }

    @Test
    fun `GIVEN device name, WHEN converted to Dp, has expected Roborazzi height and width with error margin up to 1f`(
        @TestParameter deviceMapping: DeviceNameMapping
    ) {
        val expectedDeviceInDp =
            DevicePreviewInfoParser.parse(deviceMapping.deviceName)!!.inDp()

        assertEquals(
            expectedDeviceInDp.dimensions.height,
            deviceMapping.roborazziDeviceQualifier.extractHeight(),
            1f
        )
        assertEquals(
            expectedDeviceInDp.dimensions.width,
            deviceMapping.roborazziDeviceQualifier.extractWidth(),
            1f
        )
    }

    enum class XR_DEVICE(val identifier: Identifier) {
        XR_DEVICE(requireNotNull(XR.XR_DEVICE.device.identifier)),
        XR_HEADSET(requireNotNull(XR.XR_HEADSET.device.identifier)),
    }

    @Test
    fun `GIVEN XR Device or XR Headset, has expected dimensions, density and orientation`(
        @TestParameter xrDevice: XR_DEVICE
    ) {
        val expectedDeviceFromId =
            DevicePreviewInfoParser.parse("id:${xrDevice.identifier.id}")!!

        val expectedDeviceFromName =
            DevicePreviewInfoParser.parse("name:${xrDevice.identifier.name}")!!

        assertEquals(
            expectedDeviceFromId, expectedDeviceFromName
        )

        assertEquals(
            expectedDeviceFromId.dimensions.width, 2560f
        )
        assertEquals(
            expectedDeviceFromId.dimensions.height, 2558f
        )
        assertEquals(
            expectedDeviceFromId.densityDpi, 320
        )
        assertEquals(
            expectedDeviceFromId.orientation, LANDSCAPE
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

    private fun String.extractScreenRatio(): String {
        val regex = "(long|notlong)".toRegex()
        return regex.find(this)?.value ?: ""
    }

    private fun String.extractScreenSize(): String {
        val regex = "(small|normal|large|xlarge)".toRegex()
        return regex.find(this)?.value ?: ""
    }
}