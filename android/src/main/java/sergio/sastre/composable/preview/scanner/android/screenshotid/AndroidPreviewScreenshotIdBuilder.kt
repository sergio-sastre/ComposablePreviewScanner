package sergio.sastre.composable.preview.scanner.android.screenshotid

import android.annotation.SuppressLint
import android.content.res.Configuration
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

    private var ignoreClassName: Boolean = false
    private var ignoreMethodName: Boolean = false
    private var ignoreMethodParametersType: Boolean = true

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

    fun ignoreClassName() = apply {
        ignoreClassName = true
    }

    fun ignoreMethodName() = apply {
        ignoreMethodName = true
    }

    /**
     * TODO -> explain why
     */
    fun doNotIgnoreMethodParametersType() = apply {
        ignoreMethodParametersType = false
    }

    @SuppressLint("NewApi")
    fun build(): String =
        buildList {
            val previewInfoId =
                defaultPreviewInfoId.values.filterNot { it.isNullOrBlank() }.joinToString("_")
            val declaringClass = when (ignoreClassName) {
                true -> null
                false -> composablePreview.declaringClass
            }
            val methodName = when (ignoreMethodName) {
                true -> null
                false -> composablePreview.methodName
            }
            add(
                listOfNotNull(
                    declaringClass,
                    methodName,
                    previewInfoId
                )
                    .filter { it.isNotBlank() }.joinToString(".")
            )
            if (!ignoreMethodParametersType && composablePreview.methodParameters.isNotBlank()) {
                add("_${composablePreview.methodParameters}")
            }
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
                when (uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
                    true -> "NIGHT"
                    else -> "DAY"
                }
            }

    private val AndroidPreviewInfo.deviceName: String?
        get() = GetDeviceScreenshotId.getDeviceScreenshotId(device)

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