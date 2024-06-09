package sergio.sastre.composable.preview.scanner.android.screenshotid

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Wallpapers
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

/**
 * Helper to generate screenshot names with the non-default values of the Composable's Preview AndroidPreviewInfo
 */
class AndroidPreviewScreenshotIdBuilder(
    private val composablePreview: ComposablePreview<AndroidPreviewInfo>
) {

    private val androidPreviewInfo = composablePreview.previewInfo

    private val defaultPreviewInfoId = linkedMapOf(
        "name" to androidPreviewInfo.nameName,
        "group" to androidPreviewInfo.groupName,
        "apiLevel" to androidPreviewInfo.apiLevelName,
        "widthDp" to androidPreviewInfo.widthName,
        "heightDp" to androidPreviewInfo.heightName,
        "locale" to androidPreviewInfo.locale,
        "fontScale" to androidPreviewInfo.fontScaleName,
        "showSystemUi" to androidPreviewInfo.showSystemUiName,
        "showBackground" to androidPreviewInfo.showBackgroundName,
        "backgroundColor" to androidPreviewInfo.backgroundColorName,
        "uiMode" to androidPreviewInfo.uiModeName,
        "device" to androidPreviewInfo.deviceName,
        "wallpaper" to androidPreviewInfo.wallpaperName
    )

    fun overrideDefaultIdFor(
        previewInfoName: String,
        applyInfoValue: (AndroidPreviewInfo) -> String?
    ) = apply {
        defaultPreviewInfoId[previewInfoName] = applyInfoValue(androidPreviewInfo)
    }

    fun ignoreIdFor(
        previewInfoName: String,
    ) = apply {
        defaultPreviewInfoId[previewInfoName] = null
    }

    fun build(): String =
        buildList {
            val previewInfoId =
                defaultPreviewInfoId.values.filterNot { it.isNullOrBlank() }.joinToString("_")
            add(
                listOf(composablePreview.declaringClass, composablePreview.methodName, previewInfoId)
                    .filter { it.isNotBlank() }.joinToString(".")
            )
            if (composablePreview.previewIndex != null) {
                add(composablePreview.previewIndex)
            }
        }.joinToString("")

    private val AndroidPreviewInfo.nameName: String?
        get() =
            if (name == "") {
                null
            } else {
                name.replace(" ", "_")
            }

    private val AndroidPreviewInfo.groupName: String?
        get() =
            if (group == "") {
                null
            } else {
                group.replace(" ", "_")
            }

    private val AndroidPreviewInfo.apiLevelName: String?
        get() =
            if (apiLevel == -1) {
                null
            } else {
                "API_LEVEL_$apiLevel"
            }

    private val AndroidPreviewInfo.widthName: String?
        get() =
            if (widthDp == -1) {
                null
            } else {
                "W${widthDp}dp"
            }

    private val AndroidPreviewInfo.heightName: String?
        get() =
            if (heightDp == -1) {
                null
            } else {
                "H${heightDp}dp"
            }

    private val AndroidPreviewInfo.fontScaleName: String?
        get() =
            if (fontScale == 1F) {
                null
            } else {
                "FONT_${fontScale}f".replace(".", "_")
            }

    private val AndroidPreviewInfo.showSystemUiName: String?
        get() =
            if (!showSystemUi) {
                null
            } else {
                "WITH_SYSTEM_UI"
            }

    private val AndroidPreviewInfo.showBackgroundName: String?
        get() =
            if (!showBackground) {
                null
            } else {
                "WITH_BACKGROUND"
            }

    private val AndroidPreviewInfo.backgroundColorName: String?
        get() =
            if (backgroundColor == 0L) {
                null
            } else {
                "BG_COLOR_$backgroundColor"
            }

    private val AndroidPreviewInfo.uiModeName: String?
        get() =
            if (uiMode == 0) {
                null
            } else {
                when (uiMode) {
                    Configuration.UI_MODE_NIGHT_YES -> "NIGHT"
                    Configuration.UI_MODE_NIGHT_NO -> "DAY"
                    else -> null
                }
            }

    private val AndroidPreviewInfo.deviceName: String?
        get() =
            if (device == "") {
                null
            } else {
                when (device) {
                    Devices.NEXUS_7 -> "NEXUS_7"
                    Devices.NEXUS_7_2013 -> "NEXUS_7_2013"
                    Devices.NEXUS_5 -> "NEXUS_5"
                    Devices.NEXUS_6 -> "NEXUS_6"
                    Devices.NEXUS_9 -> "NEXUS_9"
                    Devices.NEXUS_10 -> "NEXUS_10"
                    Devices.NEXUS_5X -> "NEXUS_5"
                    Devices.NEXUS_6P -> "NEXUS_6P"
                    Devices.PIXEL_C -> "PIXEL_C"
                    Devices.PIXEL -> "PIXEL"
                    Devices.PIXEL_XL -> "PIXEL_XL"
                    Devices.PIXEL_2 -> "PIXEL_2"
                    Devices.PIXEL_2_XL -> "PIXEL_2_XL"
                    Devices.PIXEL_3 -> "PIXEL_3"
                    Devices.PIXEL_3_XL -> "PIXEL_3"
                    Devices.PIXEL_3A -> "PIXEL_3A"
                    Devices.PIXEL_3A_XL -> "PIXEL_3A_XL"
                    Devices.PIXEL_4 -> "PIXEL_4"
                    Devices.PIXEL_4_XL -> "PIXEL_4_XL"
                    Devices.PIXEL_4A -> "PIXEL_4A"
                    Devices.PIXEL_5 -> "PIXEL_5A"
                    Devices.PIXEL_6 -> "PIXEL_6A"
                    Devices.PIXEL_6_PRO -> "PIXEL_6_PRO"
                    Devices.PIXEL_6A -> "PIXEL_6A"
                    Devices.PIXEL_7 -> "PIXEL_7"
                    Devices.PIXEL_7_PRO -> "PIXEL_7_PRO"
                    Devices.PIXEL_7A -> "PIXEL_7A"
                    Devices.PIXEL_FOLD -> "PIXEL_FOLD"
                    Devices.AUTOMOTIVE_1024p -> "AUTOMOTIVE_1024p"
                    Devices.WEAR_OS_LARGE_ROUND -> "WEAR_OS_LARGE_ROUND"
                    Devices.WEAR_OS_SMALL_ROUND -> "WEAR_OS_SMALL_ROUND"
                    Devices.WEAR_OS_SQUARE -> "WEAR_OS_SQUARE"
                    Devices.WEAR_OS_RECT -> "WEAR_OS_RECT"
                    Devices.PHONE -> "PHONE"
                    Devices.FOLDABLE -> "FOLDABLE"
                    Devices.TABLET -> "TABLET"
                    Devices.DESKTOP -> "DESKTOP"
                    Devices.TV_720p -> "TV_720p"
                    Devices.TV_1080p -> "TV_1080p"
                    else -> device
                        .replace("spec", "")
                        .replace(":id", "") // avoid replacing "id" in WIDTH
                        .replace("id:", "") // avoid replacing "id" in WIDTH
                        .replace("=reference_", "")
                        .replace("=", "_")
                        .replace(",", "_")
                        .replace(" ", "_")
                        .replace(":", "")
                        .uppercase()
                }
            }

    private val AndroidPreviewInfo.wallpaperName: String?
        get() =
            when (wallpaper) {
                Wallpapers.YELLOW_DOMINATED_EXAMPLE -> "WALLPAPER_YELLOW_DOMINATED"
                Wallpapers.BLUE_DOMINATED_EXAMPLE -> "WALLPAPER_BLUE_DOMINATED"
                Wallpapers.GREEN_DOMINATED_EXAMPLE -> "WALLPAPER_GREEN_DOMINATED"
                Wallpapers.RED_DOMINATED_EXAMPLE -> "WALLPAPER_RED_DOMINATED"
                Wallpapers.NONE -> null
                else -> null
            }
}