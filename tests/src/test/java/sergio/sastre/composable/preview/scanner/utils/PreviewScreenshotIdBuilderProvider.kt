@file:Suppress("DEPRECATION")
package sergio.sastre.composable.preview.scanner.utils

import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.android.screenshotid.AndroidPreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.core.preview.screenshotid.PreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.glance.GlancePreviewInfo
import sergio.sastre.composable.preview.scanner.glance.screenshotid.GlancePreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.wear.WearTilePreviewInfo
import sergio.sastre.composable.preview.scanner.wear.screenshotid.WearTilePreviewScreenshotIdBuilder

sealed interface PreviewScreenshotIdBuilderProvider<out T : PreviewScreenshotIdBuilder<*>> {
    fun passPreviewWithInfo(
        previewIndex: Int? = null,
        previewParameterDisplayName: String? = null,
        declaringClass: String = "",
        methodName: String = "",
        methodParameters: String = "",
    ): T
}

sealed interface SizedPreviewScreenshotIdBuilderProvider<out T : PreviewScreenshotIdBuilder<*>> :
    PreviewScreenshotIdBuilderProvider<T> {

    fun passPreviewWithInfo(
        previewIndex: Int? = null,
        previewParameterDisplayName: String? = null,
        widthDp: Int = -1,
        heightDp: Int = -1,
        declaringClass: String = "",
        methodName: String = "",
        methodParameters: String = "",
    ): T

    override fun passPreviewWithInfo(
        previewIndex: Int?,
        previewParameterDisplayName: String?,
        declaringClass: String,
        methodName: String,
        methodParameters: String,
    ): T = passPreviewWithInfo(
        previewIndex = previewIndex,
        previewParameterDisplayName = previewParameterDisplayName,
        widthDp = -1,
        heightDp = -1,
        declaringClass = declaringClass,
        methodName = methodName,
        methodParameters = methodParameters,
    )
}

data object AndroidScreenshotIdBuilderProvider :
    SizedPreviewScreenshotIdBuilderProvider<PreviewScreenshotIdBuilder<AndroidPreviewInfo>> {
    override fun passPreviewWithInfo(
        previewIndex: Int?,
        previewParameterDisplayName: String?,
        widthDp: Int,
        heightDp: Int,
        declaringClass: String,
        methodName: String,
        methodParameters: String,
    ): PreviewScreenshotIdBuilder<AndroidPreviewInfo> {
        return AndroidPreviewScreenshotIdBuilder(
            androidPreviewBuilder(
                widthDp = widthDp,
                heightDp = heightDp,
                previewIndex = previewIndex,
                previewParameterDisplayName = previewParameterDisplayName,
                declaringClass = declaringClass,
                methodName = methodName,
                methodParameters = methodParameters
            )
        )
    }
}

data object GlanceScreenshotIdBuilderProvider :
    SizedPreviewScreenshotIdBuilderProvider<PreviewScreenshotIdBuilder<GlancePreviewInfo>> {
    override fun passPreviewWithInfo(
        previewIndex: Int?,
        previewParameterDisplayName: String?,
        widthDp: Int,
        heightDp: Int,
        declaringClass: String,
        methodName: String,
        methodParameters: String,
    ): PreviewScreenshotIdBuilder<GlancePreviewInfo> {
        return GlancePreviewScreenshotIdBuilder(
            glancePreviewBuilder(
                widthDp = widthDp,
                heightDp = heightDp,
                previewIndex = previewIndex,
                previewParameterDisplayName = previewParameterDisplayName,
                declaringClass = declaringClass,
                methodName = methodName,
                methodParameters = methodParameters
            )
        )
    }
}

data object WearTileScreenshotIdBuilderProvider :
    PreviewScreenshotIdBuilderProvider<PreviewScreenshotIdBuilder<WearTilePreviewInfo>> {
    override fun passPreviewWithInfo(
        previewIndex: Int?,
        previewParameterDisplayName: String?,
        declaringClass: String,
        methodName: String,
        methodParameters: String,
    ): PreviewScreenshotIdBuilder<WearTilePreviewInfo> {
        return WearTilePreviewScreenshotIdBuilder(
            wearTilePreviewBuilder<Unit>(
                previewIndex = previewIndex,
                previewParameterDisplayName = previewParameterDisplayName,
                declaringClass = declaringClass,
                methodName = methodName,
                methodParameters = methodParameters
            )
        )
    }
}
