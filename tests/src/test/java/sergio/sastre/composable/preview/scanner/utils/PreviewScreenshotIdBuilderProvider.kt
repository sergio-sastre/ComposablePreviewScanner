package sergio.sastre.composable.preview.scanner.utils

import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.android.screenshotid.AndroidPreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.common.CommonPreviewInfo
import sergio.sastre.composable.preview.scanner.common.screenshotid.CommonPreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.core.preview.screenshotid.PreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.glance.GlancePreviewInfo
import sergio.sastre.composable.preview.scanner.glance.screenshotid.GlancePreviewScreenshotIdBuilder

sealed interface PreviewScreenshotIdBuilderProvider<T> {
    fun passPreviewWithInfo(
        widthDp: Int = -1,
        declaringClass: String = "",
        methodName: String = "",
        methodParameters: String = "",
    ): T
}

class CommonScreenshotIdBuilderProvider :
    PreviewScreenshotIdBuilderProvider<PreviewScreenshotIdBuilder<CommonPreviewInfo>> {
    override fun passPreviewWithInfo(
        widthDp: Int,
        declaringClass: String,
        methodName: String,
        methodParameters: String,
    ): PreviewScreenshotIdBuilder<CommonPreviewInfo> {
        return CommonPreviewScreenshotIdBuilder(
            previewBuilder(
                previewInfo = CommonPreviewInfo(widthDp = widthDp),
                declaringClass = declaringClass,
                methodName = methodName,
                methodParameters = methodParameters
            )
        )
    }
}

class AndroidScreenshotIdBuilderProvider() :
    PreviewScreenshotIdBuilderProvider<PreviewScreenshotIdBuilder<AndroidPreviewInfo>> {
    override fun passPreviewWithInfo(
        widthDp: Int,
        declaringClass: String,
        methodName: String,
        methodParameters: String,
    ): PreviewScreenshotIdBuilder<AndroidPreviewInfo> {
        return AndroidPreviewScreenshotIdBuilder(
            previewBuilder(
                previewInfo = AndroidPreviewInfo(widthDp = widthDp),
                declaringClass = declaringClass,
                methodName = methodName,
                methodParameters = methodParameters
            )
        )
    }
}

class GlanceScreenshotIdBuilderProvider() :
    PreviewScreenshotIdBuilderProvider<PreviewScreenshotIdBuilder<GlancePreviewInfo>> {
    override fun passPreviewWithInfo(
        widthDp: Int,
        declaringClass: String,
        methodName: String,
        methodParameters: String,
    ): PreviewScreenshotIdBuilder<GlancePreviewInfo> {
        return GlancePreviewScreenshotIdBuilder(
            previewBuilder(
                previewInfo = GlancePreviewInfo(widthDp = widthDp),
                declaringClass = declaringClass,
                methodName = methodName,
                methodParameters = methodParameters
            )
        )
    }
}