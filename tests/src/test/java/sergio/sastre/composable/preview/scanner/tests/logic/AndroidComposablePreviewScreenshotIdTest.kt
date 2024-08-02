package sergio.sastre.composable.preview.scanner.tests.logic

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Wallpapers
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import io.github.classgraph.AnnotationInfoList
import org.junit.Test
import org.junit.runner.RunWith
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.android.screenshotid.AndroidPreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview


@RunWith(TestParameterInjector::class)
class AndroidComposablePreviewScreenshotIdTest {

    @Test
    fun `GIVEN preview className and methodName, THEN show them only but separated by a dot`() {
        val preview = previewBuilder(
            declaringClass = "MyClass",
            methodName = "PreviewName",
        )

        assert(
            AndroidPreviewScreenshotIdBuilder(preview).build() == "MyClass.PreviewName"
        )
    }

    @Test
    fun `GIVEN preview with only previewIndex, THEN show only index`() {
        val preview = previewBuilder(
            previewIndex = 1,
        )

        assert(
            AndroidPreviewScreenshotIdBuilder(preview).build() == "1"
        )
    }

    @Test
    fun `GIVEN preview with only name, THEN show only name with underscores`() {
        val preview = previewBuilder(
            previewInfo = AndroidPreviewInfo(
                name = "My preview name"
            )
        )

        assert(
            AndroidPreviewScreenshotIdBuilder(preview).build() == "My_preview_name"
        )
    }

    @Test
    fun `GIVEN preview with only , THEN show group with underscores`() {
        val preview = previewBuilder(
            previewInfo = AndroidPreviewInfo(
                group = "My preview group"
            )
        )

        assert(
            AndroidPreviewScreenshotIdBuilder(preview).build() == "My_preview_group"
        )
    }

    @Test
    fun `GIVEN preview with only apiLevel greater than -1, THEN show only API_LEVEL_apiLevel`() {
        val preview = previewBuilder(
            previewInfo = AndroidPreviewInfo(
                apiLevel = 33
            )
        )

        assert(
            AndroidPreviewScreenshotIdBuilder(preview).build() == "API_LEVEL_33"
        )
    }

    @Test
    fun `GIVEN preview with only width greater than -1, THEN show only W$value$dp`() {
        val preview = previewBuilder(
            previewInfo = AndroidPreviewInfo(
                widthDp = 33
            )
        )

        assert(
            AndroidPreviewScreenshotIdBuilder(preview).build() == "W33dp"
        )
    }

    @Test
    fun `GIVEN preview with only height greater than -1, THEN show only H$value$dp`() {
        val preview = previewBuilder(
            previewInfo = AndroidPreviewInfo(
                heightDp = 33
            )
        )

        assert(
            AndroidPreviewScreenshotIdBuilder(preview).build() == "H33dp"
        )
    }

    @Test
    fun `GIVEN preview with only locale af_ZA, THEN show only locale af_ZA`() {
        val preview = previewBuilder(
            previewInfo = AndroidPreviewInfo(
                locale = "af_ZA"
            )
        )

        assert(
            AndroidPreviewScreenshotIdBuilder(preview).build() == "af_ZA"
        )
    }

    @Test
    fun `GIVEN preview with only fontScale 1,555f , THEN show only fontScale 1_555F`() {
        val preview = previewBuilder(
            previewInfo = AndroidPreviewInfo(
                fontScale = 1.55F
            )
        )

        assert(
            AndroidPreviewScreenshotIdBuilder(preview).build() == "FONT_1_55f"
        )
    }

    @Test
    fun `GIVEN preview with only showBackground = true, THEN show only WITH_BACKGROUND`() {
        val preview = previewBuilder(
            previewInfo = AndroidPreviewInfo(
                showBackground = true
            )
        )

        assert(
            AndroidPreviewScreenshotIdBuilder(preview).build() == "WITH_BACKGROUND"
        )
    }

    @Test
    fun `GIVEN preview with only showBackground = false, THEN show nothing`() {
        val preview = previewBuilder(
            previewInfo = AndroidPreviewInfo(
                showBackground = false
            )
        )

        assert(
            AndroidPreviewScreenshotIdBuilder(preview).build() == ""
        )
    }

    @Test
    fun `GIVEN preview with only backgroundColor = 16L, THEN show only its backgroundColor long value`() {
        val preview = previewBuilder(
            previewInfo = AndroidPreviewInfo(
                backgroundColor = 16
            )
        )

        assert(
            AndroidPreviewScreenshotIdBuilder(preview).build() == "BG_COLOR_16"
        )
    }

    @Test
    fun `GIVEN preview with only uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL, THEN show only NIGHT`() {
        val preview = previewBuilder(
            previewInfo = AndroidPreviewInfo(
                uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
            )
        )

        assert(
            AndroidPreviewScreenshotIdBuilder(preview).build() == "NIGHT"
        )
    }

    @Test
    fun `GIVEN preview with only uiMode = UI_MODE_NIGHT_YES, THEN show only NIGHT`() {
        val preview = previewBuilder(
            previewInfo = AndroidPreviewInfo(
                uiMode = Configuration.UI_MODE_NIGHT_YES
            )
        )

        assert(
            AndroidPreviewScreenshotIdBuilder(preview).build() == "NIGHT"
        )
    }

    @Test
    fun `GIVEN preview with only uiMode = UI_MODE_NIGHT_NO, THEN show only DAY`() {
        val preview = previewBuilder(
            previewInfo = AndroidPreviewInfo(
                uiMode = Configuration.UI_MODE_NIGHT_NO
            )
        )

        assert(
            AndroidPreviewScreenshotIdBuilder(preview).build() == "DAY"
        )
    }

    @Test
    fun `GIVEN preview with only uiMode = UI_MODE_NIGHT_UNDEFINED, THEN show nothing`() {
        val preview = previewBuilder(
            previewInfo = AndroidPreviewInfo(
                uiMode = Configuration.UI_MODE_NIGHT_UNDEFINED
            )
        )

        assert(
            AndroidPreviewScreenshotIdBuilder(preview).build() == ""
        )
    }

    @Test
    fun `GIVEN preview with only showBackground = false but overrides it, THEN show overriden value`() {
        val preview = previewBuilder(
            previewInfo = AndroidPreviewInfo(
                showBackground = false
            )
        )

        assert(
            AndroidPreviewScreenshotIdBuilder(preview)
                .overrideDefaultIdFor(
                    previewInfoName = "showBackground",
                    applyInfoValue = {
                        when (it.showBackground) {
                            true -> "WITH_BACKGROUND"
                            false -> "WITHOUT_BACKGROUND"
                        }
                    }
                )
                .build() == "WITHOUT_BACKGROUND" // instead of "" as the default
        )
    }

    @Test
    fun `GIVEN preview with default device, THEN show nothing`() {
        val preview = previewBuilder(
            previewInfo = AndroidPreviewInfo(
                device = Devices.DEFAULT
            )
        )
        assert(
            AndroidPreviewScreenshotIdBuilder(preview).build() == ""
        )
    }

    enum class DeviceTestParam(
        val deviceId: String,
        val expectedId: String
    ) {
        NEXUS_7(Devices.NEXUS_7, "NEXUS_7"),
        NEXUS_7_2013(Devices.NEXUS_7_2013,"NEXUS_7_2013"),
        NEXUS_5(Devices.NEXUS_5, "NEXUS_5"),
        NEXUS_6(Devices.NEXUS_6, "NEXUS_6"),
        NEXUS_9(Devices.NEXUS_9, "NEXUS_9"),
        NEXUS_10(Devices.NEXUS_10, "NEXUS_10"),
        NEXUS_5X(Devices.NEXUS_5X, "NEXUS_5"),
        NEXUS_6P(Devices.NEXUS_6P, "NEXUS_6P"),
        PIXEL_C(Devices.PIXEL_C, "PIXEL_C"),
        PIXEL(Devices.PIXEL, "PIXEL"),
        PIXEL_XL(Devices.PIXEL_XL, "PIXEL_XL"),
        PIXEL_2(Devices.PIXEL_2, "PIXEL_2"),
        PIXEL_2_XL(Devices.PIXEL_2_XL, "PIXEL_2_XL"),
        PIXEL_3(Devices.PIXEL_3, "PIXEL_3"),
        PIXEL_3_XL(Devices.PIXEL_3_XL, "PIXEL_3"),
        PIXEL_3A(Devices.PIXEL_3A, "PIXEL_3A"),
        PIXEL_3A_XL(Devices.PIXEL_3A_XL, "PIXEL_3A_XL"),
        PIXEL_4(Devices.PIXEL_4, "PIXEL_4"),
        PIXEL_4_XL(Devices.PIXEL_4_XL, "PIXEL_4_XL"),
        PIXEL_4A(Devices.PIXEL_4A, "PIXEL_4A"),
        PIXEL_5(Devices.PIXEL_5, "PIXEL_5A"),
        PIXEL_6(Devices.PIXEL_6, "PIXEL_6A"),
        PIXEL_6_PRO(Devices.PIXEL_6_PRO, "PIXEL_6_PRO"),
        PIXEL_6A(Devices.PIXEL_6A, "PIXEL_6A"),
        PIXEL_7(Devices.PIXEL_7, "PIXEL_7"),
        PIXEL_7_PRO(Devices.PIXEL_7_PRO, "PIXEL_7_PRO"),
        PIXEL_7A(Devices.PIXEL_7A, "PIXEL_7A"),
        PIXEL_FOLD(Devices.PIXEL_FOLD, "PIXEL_FOLD"),
        AUTOMOTIVE_1024p(Devices.AUTOMOTIVE_1024p,"AUTOMOTIVE_1024p"),
        WEAR_OS_LARGE_ROUND(Devices.WEAR_OS_LARGE_ROUND, "WEAR_OS_LARGE_ROUND"),
        WEAR_OS_SMALL_ROUND(Devices.WEAR_OS_SMALL_ROUND, "WEAR_OS_SMALL_ROUND"),
        WEAR_OS_SQUARE(Devices.WEAR_OS_SQUARE, "WEAR_OS_SQUARE"),
        WEAR_OS_RECT(Devices.WEAR_OS_RECT, "WEAR_OS_RECT"),
        PHONE(Devices.PHONE, "PHONE"),
        FOLDABLE(Devices.FOLDABLE, "FOLDABLE"),
        TABLET(Devices.TABLET, "TABLET"),
        DESKTOP(Devices.DESKTOP, "DESKTOP"),
        TV_720p(Devices.TV_720p, "TV_720p"),
        TV_1080p(Devices.TV_1080p, "TV_1080p"),
    }
    @Test
    fun `GIVEN preview with predefined device, THEN only show its name as expectedId`(
        @TestParameter device: DeviceTestParam,
    ) {
        val preview = previewBuilder(
            previewInfo = AndroidPreviewInfo(
                device = device.deviceId
            )
        )
        assert(
            AndroidPreviewScreenshotIdBuilder(preview).build() == device.expectedId
        )
    }

    enum class CustomDeviceTestParam(
        val deviceId: String,
        val expectedId: String
    ) {
        CUSTOM_TV("spec:shape=Normal,width=1280,height=720,unit=dp,dpi=421", "SHAPE_NORMAL_WIDTH_1280_HEIGHT_720_UNIT_DP_DPI_421"),
        CUSTOM_DESKTOP("spec:id=reference_desktop,shape=Normal,width=1920,height=1080,unit=dp,dpi=161", "DESKTOP_SHAPE_NORMAL_WIDTH_1920_HEIGHT_1080_UNIT_DP_DPI_161"),
        CUSTOM_TABLET("spec:id=reference_tablet,shape=Normal,width=1280,height=800,unit=dp,dpi=241", "TABLET_SHAPE_NORMAL_WIDTH_1280_HEIGHT_800_UNIT_DP_DPI_241"),
        CUSTOM_FOLDABLE("spec:id=reference_foldable,shape=Normal,width=1280,height=800,unit=dp,dpi=241", "FOLDABLE_SHAPE_NORMAL_WIDTH_1280_HEIGHT_800_UNIT_DP_DPI_241"),
        CUSTOM_PHONE("spec:id=reference_phone,shape=Normal,width=1280,height=800,unit=dp,dpi=241", "PHONE_SHAPE_NORMAL_WIDTH_1280_HEIGHT_800_UNIT_DP_DPI_241"),
        CUSTOM_WITH_DOUBLE_SPACES("spec:width = 411dp, height = 891dp, orientation = landscape, dpi = 420", "WIDTH_411DP_HEIGHT_891DP_ORIENTATION_LANDSCAPE_DPI_420")
    }
    @Test
    fun `GIVEN preview with custom device, THEN only show its name as expectedId`(
        @TestParameter device: CustomDeviceTestParam,
    ) {
        val preview = previewBuilder(
            previewInfo = AndroidPreviewInfo(
                device = device.deviceId
            )
        )
        assert(
            AndroidPreviewScreenshotIdBuilder(preview).build() == device.expectedId
        )
    }

    enum class WallpaperColorDominated(
        val value: Int,
        val screenshotId: String
    ){
        RED(Wallpapers.RED_DOMINATED_EXAMPLE, "WALLPAPER_RED_DOMINATED"),
        YELLOW(Wallpapers.YELLOW_DOMINATED_EXAMPLE, "WALLPAPER_YELLOW_DOMINATED"),
        GREEN(Wallpapers.GREEN_DOMINATED_EXAMPLE, "WALLPAPER_GREEN_DOMINATED"),
        BLUE(Wallpapers.BLUE_DOMINATED_EXAMPLE, "WALLPAPER_BLUE_DOMINATED"),
    }
    @Test
    fun `GIVEN preview with only wallpaper color dominated, THEN only show its name as screenshotId`(
        @TestParameter wallpaperColorDominated: WallpaperColorDominated
    ) {
        val preview = previewBuilder(
            previewInfo = AndroidPreviewInfo(
                wallpaper = wallpaperColorDominated.value
            )
        )

        assert(
            AndroidPreviewScreenshotIdBuilder(preview).build() == wallpaperColorDominated.screenshotId
        )
    }

    @Test
    fun `GIVEN preview with only wallpaper = NONE, THEN show nothing`() {
        val preview = previewBuilder(
            previewInfo = AndroidPreviewInfo(
                wallpaper = Wallpapers.NONE
            )
        )

        assert(
            AndroidPreviewScreenshotIdBuilder(preview).build() == ""
        )
    }

    @Test
    fun `GIVEN className ignored, THEN declaringClass is not included`() {
        val preview = previewBuilder(
            declaringClass = "MyClass",
        )

        assert(
            !AndroidPreviewScreenshotIdBuilder(preview)
                .ignoreClassName()
                .build()
                .contains("MyClass")
        )
    }

    @Test
    fun `GIVEN methodName ignored, THEN methodName is not included`() {
        val preview = previewBuilder(
            methodName = "PreviewName",
        )

        assert(
            !AndroidPreviewScreenshotIdBuilder(preview)
                .ignoreMethodName()
                .build()
                .contains("PreviewName")
        )
    }

    enum class PreviewKeyAndInfo(
        val key: String,
        val previewInfo: AndroidPreviewInfo
    ) {
        NAME("name", AndroidPreviewInfo(name = "name")),
        GROUP("group", AndroidPreviewInfo(group = "group")),
        API_LEVEL("apiLevel", AndroidPreviewInfo(apiLevel = 33)),
        WIDTH_DP("widthDp", AndroidPreviewInfo(widthDp = 33)),
        HEIGHT_DP("heightDp", AndroidPreviewInfo(heightDp = 33)),
        LOCALE("locale", AndroidPreviewInfo(locale = "de")),
        FONT_SCALE("fontScale", AndroidPreviewInfo(fontScale = 1.3f)),
        SHOW_SYSTEM_UI("showSystemUi", AndroidPreviewInfo(showSystemUi = true)),
        SHOW_BACKGROUND("showBackground", AndroidPreviewInfo(showBackground = true)),
        BACKGROUND_COLOR("backgroundColor", AndroidPreviewInfo(backgroundColor = 100L)),
        UI_MODE("uiMode", AndroidPreviewInfo(uiMode = Configuration.UI_MODE_NIGHT_YES)),
        DEVICE("device", AndroidPreviewInfo(device = Devices.PHONE)),
        WALLPAPER("wallpaper", AndroidPreviewInfo(wallpaper = Wallpapers.GREEN_DOMINATED_EXAMPLE))
    }
    @Test
    fun `GIVEN preview info key ignored, THEN show nothing`(
        @TestParameter previewKeyAndInfo: PreviewKeyAndInfo
    ) {
        val preview = previewBuilder(
            previewInfo = previewKeyAndInfo.previewInfo
        )

        assert(
            AndroidPreviewScreenshotIdBuilder(preview)
                .ignoreIdFor(previewKeyAndInfo.key)
                .build() == ""
        )
    }

    @Test
    fun `GIVEN preview with only widthDp and heightDp but both ignored, THEN show nothing`() {
        val preview = previewBuilder(
            previewInfo = AndroidPreviewInfo(
                widthDp = 33,
                heightDp = 32,
            )
        )

        assert(
            AndroidPreviewScreenshotIdBuilder(preview)
                .ignoreIdFor("widthDp")
                .ignoreIdFor("heightDp")
                .build() == "" // instead of "W33dp_H32dp" as the default
        )
    }

    private fun previewBuilder(
        previewInfo: AndroidPreviewInfo = AndroidPreviewInfo(),
        previewIndex: Int? = null,
        otherAnnotationsInfo: AnnotationInfoList? = null,
        declaringClass: String = "",
        methodName: String = ""
    ): ComposablePreview<AndroidPreviewInfo> = object : ComposablePreview<AndroidPreviewInfo> {
        override val previewInfo: AndroidPreviewInfo = previewInfo
        override val previewIndex: Int? = previewIndex
        override val otherAnnotationsInfo: AnnotationInfoList? = otherAnnotationsInfo
        override val declaringClass: String = declaringClass
        override val methodName: String = methodName

        @Composable
        override fun invoke() {
        }
    }
}