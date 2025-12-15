package sergio.sastre.composable.preview.scanner.utils

import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.android.screenshotid.AndroidPreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.common.CommonPreviewInfo
import sergio.sastre.composable.preview.scanner.common.screenshotid.CommonPreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.core.preview.screenshotid.PreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.glance.GlancePreviewInfo
import sergio.sastre.composable.preview.scanner.glance.screenshotid.GlancePreviewScreenshotIdBuilder

sealed interface PreviewScreenshotIdBuilderProvider<out T : PreviewScreenshotIdBuilder<*>> {
    fun passPreviewWithInfo(
        previewIndex: Int? = null,
        widthDp: Int = -1,
        heightDp: Int = -1,
        declaringClass: String = "",
        methodName: String = "",
        methodParameters: String = "",
    ): T
}

class CommonScreenshotIdBuilderProvider :
    PreviewScreenshotIdBuilderProvider<PreviewScreenshotIdBuilder<CommonPreviewInfo>> {
    override fun passPreviewWithInfo(
        previewIndex: Int?,
        widthDp: Int,
        heightDp: Int,
        declaringClass: String,
        methodName: String,
        methodParameters: String,
    ): PreviewScreenshotIdBuilder<CommonPreviewInfo> {
        return CommonPreviewScreenshotIdBuilder(
            previewBuilder(
                previewIndex = previewIndex,
                previewInfo = CommonPreviewInfo(widthDp = widthDp, heightDp = heightDp),
                declaringClass = declaringClass,
                methodName = methodName,
                methodParameters = methodParameters
            )
        )
    }
}

class AndroidScreenshotIdBuilderProvider :
    PreviewScreenshotIdBuilderProvider<PreviewScreenshotIdBuilder<AndroidPreviewInfo>> {
    override fun passPreviewWithInfo(
        previewIndex: Int?,
        widthDp: Int,
        heightDp: Int,
        declaringClass: String,
        methodName: String,
        methodParameters: String,
    ): PreviewScreenshotIdBuilder<AndroidPreviewInfo> {
        return AndroidPreviewScreenshotIdBuilder(
            previewBuilder(
                previewIndex = previewIndex,
                previewInfo = AndroidPreviewInfo(widthDp = widthDp, heightDp = heightDp),
                declaringClass = declaringClass,
                methodName = methodName,
                methodParameters = methodParameters
            )
        )
    }
}

class GlanceScreenshotIdBuilderProvider :
    PreviewScreenshotIdBuilderProvider<PreviewScreenshotIdBuilder<GlancePreviewInfo>> {
    override fun passPreviewWithInfo(
        previewIndex: Int?,
        widthDp: Int,
        heightDp: Int,
        declaringClass: String,
        methodName: String,
        methodParameters: String,
    ): PreviewScreenshotIdBuilder<GlancePreviewInfo> {
        return GlancePreviewScreenshotIdBuilder(
            previewBuilder(
                previewIndex = previewIndex,
                previewInfo = GlancePreviewInfo(widthDp = widthDp, heightDp = heightDp),
                declaringClass = declaringClass,
                methodName = methodName,
                methodParameters = methodParameters
            )
        )
    }
}