@file:Suppress("DEPRECATION")
package sergio.sastre.composable.preview.scanner.utils

import androidx.compose.runtime.Composable
import io.github.classgraph.AnnotationInfoList
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreviewWithResult
import sergio.sastre.composable.preview.scanner.glance.GlancePreviewInfo
import sergio.sastre.composable.preview.scanner.wear.WearTilePreviewInfo

private fun <T, R> createComposablePreviewWithResult(
    previewInfo: T,
    previewIndex: Int?,
    previewParameterDisplayName: String?,
    otherAnnotationsInfo: AnnotationInfoList?,
    declaringClass: String,
    methodName: String,
    methodParameters: String,
): ComposablePreviewWithResult<T, R> = object : ComposablePreviewWithResult<T, R> {
    override val previewInfo: T = previewInfo
    override val previewIndex: Int? = previewIndex
    override val previewIndexDisplayName: String? = previewParameterDisplayName
    override val otherAnnotationsInfo: AnnotationInfoList? = otherAnnotationsInfo
    override val declaringClass: String = declaringClass
    override val methodName: String = methodName
    override val methodParametersType: String = methodParameters

    @Composable
    override fun invoke(): R {
        throw NotImplementedError()
    }
}

private fun <T> createComposablePreview(
    previewInfo: T,
    previewIndex: Int?,
    previewParameterDisplayName: String?,
    otherAnnotationsInfo: AnnotationInfoList?,
    declaringClass: String,
    methodName: String,
    methodParameters: String,
): ComposablePreview<T> {
    val previewWithResult = createComposablePreviewWithResult<T, Unit>(
        previewInfo = previewInfo,
        previewIndex = previewIndex,
        previewParameterDisplayName = previewParameterDisplayName,
        otherAnnotationsInfo = otherAnnotationsInfo,
        declaringClass = declaringClass,
        methodName = methodName,
        methodParameters = methodParameters,
    )
    return object : ComposablePreview<T>, ComposablePreviewWithResult<T, Unit> by previewWithResult {
        @Composable
        override fun invoke() {
        }
    }
}

fun previewBuilder(
    previewInfo: AndroidPreviewInfo = AndroidPreviewInfo(),
    previewIndex: Int? = null,
    previewParameterDisplayName: String? = null,
    otherAnnotationsInfo: AnnotationInfoList? = null,
    declaringClass: String = "",
    methodName: String = "",
    methodParameters: String = "",
): ComposablePreview<AndroidPreviewInfo> = createComposablePreview(
    previewInfo = previewInfo,
    previewIndex = previewIndex,
    previewParameterDisplayName = previewParameterDisplayName,
    otherAnnotationsInfo = otherAnnotationsInfo,
    declaringClass = declaringClass,
    methodName = methodName,
    methodParameters = methodParameters,
)

fun previewBuilder(
    previewInfo: GlancePreviewInfo = GlancePreviewInfo(),
    previewIndex: Int? = null,
    previewParameterDisplayName: String? = null,
    otherAnnotationsInfo: AnnotationInfoList? = null,
    declaringClass: String = "",
    methodName: String = "",
    methodParameters: String = "",
): ComposablePreview<GlancePreviewInfo> = createComposablePreview(
    previewInfo = previewInfo,
    previewIndex = previewIndex,
    previewParameterDisplayName = previewParameterDisplayName,
    otherAnnotationsInfo = otherAnnotationsInfo,
    declaringClass = declaringClass,
    methodName = methodName,
    methodParameters = methodParameters,
)

fun androidPreviewBuilder(
    widthDp: Int = -1,
    heightDp: Int = -1,
    previewIndex: Int? = null,
    previewParameterDisplayName: String? = null,
    otherAnnotationsInfo: AnnotationInfoList? = null,
    declaringClass: String = "",
    methodName: String = "",
    methodParameters: String = "",
): ComposablePreview<AndroidPreviewInfo> = createComposablePreview(
    previewInfo = AndroidPreviewInfo(widthDp = widthDp, heightDp = heightDp),
    previewIndex = previewIndex,
    previewParameterDisplayName = previewParameterDisplayName,
    otherAnnotationsInfo = otherAnnotationsInfo,
    declaringClass = declaringClass,
    methodName = methodName,
    methodParameters = methodParameters,
)

fun glancePreviewBuilder(
    widthDp: Int = -1,
    heightDp: Int = -1,
    previewIndex: Int? = null,
    previewParameterDisplayName: String? = null,
    otherAnnotationsInfo: AnnotationInfoList? = null,
    declaringClass: String = "",
    methodName: String = "",
    methodParameters: String = "",
): ComposablePreview<GlancePreviewInfo> = createComposablePreview(
    previewInfo = GlancePreviewInfo(widthDp = widthDp, heightDp = heightDp),
    previewIndex = previewIndex,
    previewParameterDisplayName = previewParameterDisplayName,
    otherAnnotationsInfo = otherAnnotationsInfo,
    declaringClass = declaringClass,
    methodName = methodName,
    methodParameters = methodParameters,
)

fun previewBuilder(
    previewInfo: WearTilePreviewInfo = WearTilePreviewInfo(),
    previewIndex: Int? = null,
    previewParameterDisplayName: String? = null,
    otherAnnotationsInfo: AnnotationInfoList? = null,
    declaringClass: String = "",
    methodName: String = "",
    methodParameters: String = "",
): ComposablePreviewWithResult<WearTilePreviewInfo, Unit> = createComposablePreviewWithResult(
    previewInfo = previewInfo,
    previewIndex = previewIndex,
    previewParameterDisplayName = previewParameterDisplayName,
    otherAnnotationsInfo = otherAnnotationsInfo,
    declaringClass = declaringClass,
    methodName = methodName,
    methodParameters = methodParameters,
)

fun <R> wearTilePreviewBuilder(
    previewIndex: Int? = null,
    previewParameterDisplayName: String? = null,
    otherAnnotationsInfo: AnnotationInfoList? = null,
    declaringClass: String = "",
    methodName: String = "",
    methodParameters: String = "",
): ComposablePreviewWithResult<WearTilePreviewInfo, R> = createComposablePreviewWithResult(
    previewInfo = WearTilePreviewInfo(),
    previewIndex = previewIndex,
    previewParameterDisplayName = previewParameterDisplayName,
    otherAnnotationsInfo = otherAnnotationsInfo,
    declaringClass = declaringClass,
    methodName = methodName,
    methodParameters = methodParameters,
)
