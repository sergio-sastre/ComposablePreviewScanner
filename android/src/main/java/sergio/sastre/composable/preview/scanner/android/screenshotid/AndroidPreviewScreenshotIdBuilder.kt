package sergio.sastre.composable.preview.scanner.android.screenshotid

import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.screenshotid.DefaultPreviewInfoParameterToStringConverter
import sergio.sastre.composable.preview.scanner.core.preview.screenshotid.PreviewScreenshotIdBuilder

/**
 * Helper to generate screenshot names with the non-default values of the Composable's Preview AndroidPreviewInfo
 */
class AndroidPreviewScreenshotIdBuilder(
    private val composablePreview: ComposablePreview<AndroidPreviewInfo>
) : PreviewScreenshotIdBuilder<AndroidPreviewInfo>(
    composablePreview = composablePreview,
    defaultPreviewInfoIdProvider = {
        val genericPreviewInfoConverter = DefaultPreviewInfoParameterToStringConverter()
        val androidPreviewInfoConverter = AndroidPreviewInfoParameterToStringConverter()
        val androidPreviewInfo = composablePreview.previewInfo
        linkedMapOf(
            "name" to genericPreviewInfoConverter.name(androidPreviewInfo.name),
            "group" to genericPreviewInfoConverter.group(androidPreviewInfo.group),
            "apiLevel" to genericPreviewInfoConverter.apiLevel(androidPreviewInfo.apiLevel),
            "widthDp" to genericPreviewInfoConverter.widthDp(androidPreviewInfo.widthDp),
            "heightDp" to genericPreviewInfoConverter.heightDp(androidPreviewInfo.heightDp),
            "locale" to androidPreviewInfo.locale,
            "fontScale" to genericPreviewInfoConverter.fontScale(androidPreviewInfo.fontScale),
            "showSystemUi" to genericPreviewInfoConverter.showSystemUi(androidPreviewInfo.showSystemUi),
            "showBackground" to genericPreviewInfoConverter.showBackground(androidPreviewInfo.showBackground),
            "backgroundColor" to genericPreviewInfoConverter.backgroundColor(androidPreviewInfo.backgroundColor),
            "uiMode" to androidPreviewInfoConverter.uiMode(androidPreviewInfo.uiMode),
            "device" to androidPreviewInfoConverter.device(androidPreviewInfo.device),
            "wallpaper" to androidPreviewInfoConverter.wallpaper(androidPreviewInfo.wallpaper)
        )
    }
)