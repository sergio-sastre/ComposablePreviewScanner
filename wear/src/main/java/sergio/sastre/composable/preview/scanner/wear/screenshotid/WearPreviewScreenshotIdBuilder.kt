package sergio.sastre.composable.preview.scanner.wear.screenshotid

import sergio.sastre.composable.preview.scanner.core.preview.screenshotid.DefaultPreviewInfoParameterToStringConverter
import sergio.sastre.composable.preview.scanner.core.preview.screenshotid.PreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.wear.WearPreviewInfo
import sergio.sastre.composable.preview.scanner.wear.preview.WearComposablePreview

class WearPreviewScreenshotIdBuilder<R>(
    private val wearComposablePreview: WearComposablePreview<WearPreviewInfo, R>
): PreviewScreenshotIdBuilder<WearPreviewInfo>(
    composablePreview = object : sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview<WearPreviewInfo> {
        override val previewInfo: WearPreviewInfo = wearComposablePreview.previewInfo
        override val previewIndex: Int? = wearComposablePreview.previewIndex
        override val previewIndexDisplayName: String? = wearComposablePreview.previewIndexDisplayName
        override val otherAnnotationsInfo = wearComposablePreview.otherAnnotationsInfo
        override val declaringClass: String = wearComposablePreview.declaringClass
        override val methodName: String = wearComposablePreview.methodName
        override val methodParametersType: String = wearComposablePreview.methodParametersType
        
        @androidx.compose.runtime.Composable
        override fun invoke() { /* no-op */ }
        
        override fun toString(): String = wearComposablePreview.toString()
    },
    defaultPreviewInfoIdProvider = {
        val genericPreviewInfoConverter = DefaultPreviewInfoParameterToStringConverter()
        val wearPreviewInfoConverter = WearPreviewInfoParameterToStringConverter()
        val wearPreviewInfo = wearComposablePreview.previewInfo
        linkedMapOf(
            "name" to genericPreviewInfoConverter.name(wearPreviewInfo.name),
            "group" to genericPreviewInfoConverter.group(wearPreviewInfo.group),
            "apiLevel" to genericPreviewInfoConverter.apiLevel(wearPreviewInfo.apiLevel),
            "widthDp" to genericPreviewInfoConverter.widthDp(wearPreviewInfo.widthDp),
            "heightDp" to genericPreviewInfoConverter.heightDp(wearPreviewInfo.heightDp),
            "locale" to if (wearPreviewInfo.locale.isNotEmpty()) wearPreviewInfo.locale else null,
            "fontScale" to genericPreviewInfoConverter.fontScale(wearPreviewInfo.fontScale),
            "showSystemUi" to genericPreviewInfoConverter.showSystemUi(wearPreviewInfo.showSystemUi),
            "showBackground" to genericPreviewInfoConverter.showBackground(wearPreviewInfo.showBackground),
            "backgroundColor" to genericPreviewInfoConverter.backgroundColor(wearPreviewInfo.backgroundColor),
            "uiMode" to wearPreviewInfoConverter.uiMode(wearPreviewInfo.uiMode),
            "device" to wearPreviewInfoConverter.device(wearPreviewInfo.device),
            "wallpaper" to wearPreviewInfoConverter.wallpaper(wearPreviewInfo.wallpaper)
        )
    }
)
