package sergio.sastre.composable.preview.scanner.common.screenshotid

import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.screenshotid.DefaultPreviewInfoParameterToStringConverter
import sergio.sastre.composable.preview.scanner.core.preview.screenshotid.PreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.common.CommonPreviewInfo

class CommonPreviewScreenshotIdBuilder(
    private val composablePreview: ComposablePreview<CommonPreviewInfo>
): PreviewScreenshotIdBuilder<CommonPreviewInfo>(
    composablePreview = composablePreview,
    defaultPreviewInfoIdProvider = {
        val genericPreviewInfoConverter = DefaultPreviewInfoParameterToStringConverter()
        val commonPreviewInfo = composablePreview.previewInfo
        linkedMapOf(
            "name" to genericPreviewInfoConverter.name(commonPreviewInfo.name),
            "group" to genericPreviewInfoConverter.group(commonPreviewInfo.group),
            "widthDp" to genericPreviewInfoConverter.widthDp(commonPreviewInfo.widthDp),
            "heightDp" to genericPreviewInfoConverter.heightDp(commonPreviewInfo.heightDp),
            "locale" to commonPreviewInfo.locale,
            "showBackground" to genericPreviewInfoConverter.showBackground(commonPreviewInfo.showBackground),
            "backgroundColor" to genericPreviewInfoConverter.backgroundColor(commonPreviewInfo.backgroundColor),
        )
    }
)