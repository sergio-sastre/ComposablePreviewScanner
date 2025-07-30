package sergio.sastre.composable.preview.scanner.tests.api.main.screenshotid

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Wallpapers
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider
import io.github.classgraph.AnnotationInfoList
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.android.device.domain.Identifier
import sergio.sastre.composable.preview.scanner.android.device.types.Automotive
import sergio.sastre.composable.preview.scanner.android.device.types.Desktop
import sergio.sastre.composable.preview.scanner.android.device.types.GenericDevices
import sergio.sastre.composable.preview.scanner.android.device.types.Phone
import sergio.sastre.composable.preview.scanner.android.device.types.Tablet
import sergio.sastre.composable.preview.scanner.android.device.types.Television
import sergio.sastre.composable.preview.scanner.android.device.types.Wear
import sergio.sastre.composable.preview.scanner.android.device.types.XR
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
    fun `GIVEN preview with only group, THEN show group with underscores`() {
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
        assertEquals(
            AndroidPreviewScreenshotIdBuilder(preview).build(), ""
        )
    }

    @Test
    fun `GIVEN preview with predefined device id, THEN only show its name as expectedId`(
        @TestParameter(valuesProvider = IdentifierWithIdValueProvider::class) identifier: Identifier
    ) {
        val validPattern = "^[A-Z0-9_]+$".toRegex()
        val preview = previewBuilder(
            previewInfo = AndroidPreviewInfo(
                device = "id:" + identifier.id
            )
        )
        val screenshotId = AndroidPreviewScreenshotIdBuilder(preview).build()
        assertTrue(
            "screenshot id must only contain uppercase alphanumeric symbols or underscore '_'",
            validPattern.matches(screenshotId)
        )
        assertTrue(
            "screenshot id must not contain underscore '_' as first character",
            screenshotId.first().toString() != "_"
        )
        assertTrue(
            "screenshot id must not contain underscore '_' as last character",
            screenshotId.last().toString() != "_"
        )
    }

    @Test
    fun `GIVEN preview with predefined device name, THEN only show its name as expectedId`(
        @TestParameter(valuesProvider = IdentifierWithNameValueProvider::class) identifier: Identifier
    ) {
        val validPattern = "^[A-Z0-9_]+$".toRegex()
        val preview = previewBuilder(
            previewInfo = AndroidPreviewInfo(
                device = "name:" + identifier.name
            )
        )
        val screenshotId = AndroidPreviewScreenshotIdBuilder(preview).build()
        assertTrue(
            "screenshot id must only contains uppercase alphanumeric symbols or underscore '_'",
            validPattern.matches(screenshotId)
        )
        assertTrue(
            "screenshot id must not contain underscore '_' as first character",
            screenshotId.first().toString() != "_"
        )
        assertTrue(
            "screenshot id must not contain underscore '_' as last character",
            screenshotId.last().toString() != "_"
        )
    }

    enum class CustomDeviceTestParam(
        val deviceId: String,
        val expectedId: String
    ) {
        CUSTOM_TV(
            "spec:shape=Normal,width=1280,height=720,unit=dp,dpi=421",
            "SHAPE_NORMAL_WIDTH_1280_HEIGHT_720_UNIT_DP_DPI_421"
        ),
        CUSTOM_DESKTOP(
            "spec:id=reference_desktop,shape=Normal,width=1920,height=1080,unit=dp,dpi=161",
            "DESKTOP_SHAPE_NORMAL_WIDTH_1920_HEIGHT_1080_UNIT_DP_DPI_161"
        ),
        CUSTOM_TABLET(
            "spec:id=reference_tablet,shape=Normal,width=1280,height=800,unit=dp,dpi=241",
            "TABLET_SHAPE_NORMAL_WIDTH_1280_HEIGHT_800_UNIT_DP_DPI_241"
        ),
        CUSTOM_FOLDABLE(
            "spec:id=reference_foldable,shape=Normal,width=1280,height=800,unit=dp,dpi=241",
            "FOLDABLE_SHAPE_NORMAL_WIDTH_1280_HEIGHT_800_UNIT_DP_DPI_241"
        ),
        CUSTOM_PHONE(
            "spec:id=reference_phone,shape=Normal,width=1280,height=800,unit=dp,dpi=241",
            "PHONE_SHAPE_NORMAL_WIDTH_1280_HEIGHT_800_UNIT_DP_DPI_241"
        ),
        CUSTOM_PARENT(
            "spec:parent=Nexus 7, orientation=landscape",
            "PARENT_NEXUS_7_ORIENTATION_LANDSCAPE"
        ),
        CUSTOM_PARENT_REVERSED(
            "spec: orientation=landscape, cutout= none,parent=Nexus 7",
            "ORIENTATION_LANDSCAPE_CUTOUT_NONE_PARENT_NEXUS_7"
        ),
        CUSTOM_WITH_DOUBLE_SPACES(
            "spec:width = 411dp, height = 891dp, orientation = landscape, dpi = 420",
            "WIDTH_411DP_HEIGHT_891DP_ORIENTATION_LANDSCAPE_DPI_420"
        ),
        CUSTOM_WITH_TABS(
            "spec:width=411dp,  height  =891dp",
            "WIDTH_411DP_HEIGHT_891DP"
        )
    }
    @Test
    fun `GIVEN preview with custom device, THEN only show its properties as expectedId separated by underscores`(
        @TestParameter device: CustomDeviceTestParam,
    ) {
        val preview = previewBuilder(
            previewInfo = AndroidPreviewInfo(
                device = device.deviceId
            )
        )
        assertEquals(
            AndroidPreviewScreenshotIdBuilder(preview).build(), device.expectedId
        )
    }

    enum class WallpaperColorDominated(
        val value: Int,
        val screenshotId: String
    ) {
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

        assertEquals(
            AndroidPreviewScreenshotIdBuilder(preview).build(), wallpaperColorDominated.screenshotId
        )
    }

    @Test
    fun `GIVEN preview with only wallpaper = NONE, THEN show nothing`() {
        val preview = previewBuilder(
            previewInfo = AndroidPreviewInfo(
                wallpaper = Wallpapers.NONE
            )
        )

        assertEquals(
            AndroidPreviewScreenshotIdBuilder(preview).build(), ""
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
    fun `GIVEN methodParameters not ignored, THEN declaringClass is included`() {
        val preview = previewBuilder(
            methodParameters = "name_String",
        )

        assert(
            AndroidPreviewScreenshotIdBuilder(preview)
                .doNotIgnoreMethodParametersType()
                .build()
                .contains("name_String")
        )
    }

    @Test
    fun `GIVEN 2 previews differ only in the methodParametersType WHEN these are not ignored, THEN the screenshotIds differ`() {
        val preview1 = previewBuilder(
            methodParameters = "name_String",
        )
        val preview2 = previewBuilder(
            methodParameters = "name_Int",
        )

        val screenshotIdPreview1 = AndroidPreviewScreenshotIdBuilder(preview1)
            .doNotIgnoreMethodParametersType()
            .build()

        val screenshotIdPreview2 = AndroidPreviewScreenshotIdBuilder(preview2)
            .doNotIgnoreMethodParametersType()
            .build()

        assert(screenshotIdPreview1 != screenshotIdPreview2)
    }

    @Test
    fun `GIVEN 2 previews differ only in the methodParametersType WHEN these are ignored, THEN the screenshotIds are the same`() {
        val preview1 = previewBuilder(
            methodParameters = "name_String",
        )
        val preview2 = previewBuilder(
            methodParameters = "name_Int",
        )

        val screenshotIdPreview1 = AndroidPreviewScreenshotIdBuilder(preview1).build()

        val screenshotIdPreview2 = AndroidPreviewScreenshotIdBuilder(preview2).build()

        assert(screenshotIdPreview1 == screenshotIdPreview2)
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
        methodName: String = "",
        methodParameters: String = "",
    ): ComposablePreview<AndroidPreviewInfo> = object : ComposablePreview<AndroidPreviewInfo> {
        override val previewInfo: AndroidPreviewInfo = previewInfo
        override val previewIndex: Int? = previewIndex
        override val otherAnnotationsInfo: AnnotationInfoList? = otherAnnotationsInfo
        override val declaringClass: String = declaringClass
        override val methodName: String = methodName
        override val methodParametersType: String = methodParameters

        @Composable
        override fun invoke() {
        }
    }

    private class IdentifierValueProvider : TestParameterValuesProvider() {
        public override fun provideValues(context: Context?): List<Identifier> {
            return listOf(
                Phone.entries,
                Tablet.entries,
                Desktop.entries,
                Automotive.entries,
                Television.entries,
                Wear.entries,
                GenericDevices.entries,
                XR.entries,
            ).flatMap { entry -> entry.mapNotNull { it.device.identifier } }
        }
    }

    private class IdentifierWithIdValueProvider : TestParameterValuesProvider() {
        public override fun provideValues(context: Context?): List<Identifier> {
            return IdentifierValueProvider().provideValues(context).filter { it.id != null }
        }
    }

    private class IdentifierWithNameValueProvider : TestParameterValuesProvider() {
        public override fun provideValues(context: Context?): List<Identifier> {
            return IdentifierValueProvider().provideValues(context).filter { it.name != null }
        }
    }
}