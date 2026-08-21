package sergio.sastre.composable.preview.scanner.wear.screenshotid

import sergio.sastre.composable.preview.scanner.core.preview.screenshotid.DefaultPreviewInfoParameterToStringConverter
import sergio.sastre.composable.preview.scanner.core.preview.screenshotid.PreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.wear.WearTilePreviewInfo
import sergio.sastre.composable.preview.scanner.wear.preview.WearComposablePreview

class WearTilePreviewScreenshotIdBuilder<R>(
    private val wearComposablePreview: WearComposablePreview<WearTilePreviewInfo, R>
): PreviewScreenshotIdBuilder<WearTilePreviewInfo>(
    composablePreview = object : sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview<WearTilePreviewInfo> {
        override val previewInfo: WearTilePreviewInfo = wearComposablePreview.previewInfo
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
        val wearTilePreviewInfo = wearComposablePreview.previewInfo
        linkedMapOf(
            "name" to genericPreviewInfoConverter.name(wearTilePreviewInfo.name),
            "group" to genericPreviewInfoConverter.group(wearTilePreviewInfo.group),
            "device" to if (wearTilePreviewInfo.device.isNotEmpty()) wearTilePreviewInfo.device else null,
            "fontScale" to genericPreviewInfoConverter.fontScale(wearTilePreviewInfo.fontScale),
            "locale" to if (wearTilePreviewInfo.locale.isNotEmpty()) wearTilePreviewInfo.locale else null,
        )
    }
)
