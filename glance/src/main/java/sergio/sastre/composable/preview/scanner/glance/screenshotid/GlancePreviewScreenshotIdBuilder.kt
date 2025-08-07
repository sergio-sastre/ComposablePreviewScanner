package sergio.sastre.composable.preview.scanner.glance.screenshotid

import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.screenshotid.DefaultPreviewInfoParameterToStringConverter
import sergio.sastre.composable.preview.scanner.core.preview.screenshotid.PreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.glance.GlancePreviewInfo

class GlancePreviewScreenshotIdBuilder(
    private val composablePreview: ComposablePreview<GlancePreviewInfo>
): PreviewScreenshotIdBuilder<GlancePreviewInfo>(
    composablePreview = composablePreview,
    defaultPreviewInfoIdProvider = {
        val genericPreviewInfoConverter = DefaultPreviewInfoParameterToStringConverter()
        val glancePreviewInfo = composablePreview.previewInfo
        linkedMapOf(
            "widthDp" to genericPreviewInfoConverter.widthDp(glancePreviewInfo.widthDp),
            "heightDp" to genericPreviewInfoConverter.heightDp(glancePreviewInfo.heightDp),
        )
    }
)