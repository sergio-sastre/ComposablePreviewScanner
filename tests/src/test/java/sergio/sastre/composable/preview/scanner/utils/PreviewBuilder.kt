package sergio.sastre.composable.preview.scanner.utils

import androidx.compose.runtime.Composable
import io.github.classgraph.AnnotationInfoList
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.common.CommonPreviewInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.glance.GlancePreviewInfo

private fun <T> createComposablePreview(
    previewInfo: T,
    previewIndex: Int?,
    otherAnnotationsInfo: AnnotationInfoList?,
    declaringClass: String,
    methodName: String,
    methodParameters: String,
): ComposablePreview<T> = object : ComposablePreview<T> {
    override val previewInfo: T = previewInfo
    override val previewIndex: Int? = previewIndex
    override val otherAnnotationsInfo: AnnotationInfoList? = otherAnnotationsInfo
    override val declaringClass: String = declaringClass
    override val methodName: String = methodName
    override val methodParametersType: String = methodParameters

    @Composable
    override fun invoke() {
    }
}

fun previewBuilder(
    previewInfo: AndroidPreviewInfo = AndroidPreviewInfo(),
    previewIndex: Int? = null,
    otherAnnotationsInfo: AnnotationInfoList? = null,
    declaringClass: String = "",
    methodName: String = "",
    methodParameters: String = "",
): ComposablePreview<AndroidPreviewInfo> = createComposablePreview(
    previewInfo = previewInfo,
    previewIndex = previewIndex,
    otherAnnotationsInfo = otherAnnotationsInfo,
    declaringClass = declaringClass,
    methodName = methodName,
    methodParameters = methodParameters,
)

fun previewBuilder(
    previewInfo: CommonPreviewInfo = CommonPreviewInfo(),
    previewIndex: Int? = null,
    otherAnnotationsInfo: AnnotationInfoList? = null,
    declaringClass: String = "",
    methodName: String = "",
    methodParameters: String = "",
): ComposablePreview<CommonPreviewInfo> = createComposablePreview(
    previewInfo = previewInfo,
    previewIndex = previewIndex,
    otherAnnotationsInfo = otherAnnotationsInfo,
    declaringClass = declaringClass,
    methodName = methodName,
    methodParameters = methodParameters,
)

fun previewBuilder(
    previewInfo: GlancePreviewInfo = GlancePreviewInfo(),
    previewIndex: Int? = null,
    otherAnnotationsInfo: AnnotationInfoList? = null,
    declaringClass: String = "",
    methodName: String = "",
    methodParameters: String = "",
): ComposablePreview<GlancePreviewInfo> = createComposablePreview(
    previewInfo = previewInfo,
    previewIndex = previewIndex,
    otherAnnotationsInfo = otherAnnotationsInfo,
    declaringClass = declaringClass,
    methodName = methodName,
    methodParameters = methodParameters,
)

fun androidPreviewBuilder(
    previewIndex: Int? = null,
    otherAnnotationsInfo: AnnotationInfoList? = null,
    declaringClass: String = "",
    methodName: String = "",
    methodParameters: String = "",
): ComposablePreview<AndroidPreviewInfo> = createComposablePreview(
    previewInfo = AndroidPreviewInfo(),
    previewIndex = previewIndex,
    otherAnnotationsInfo = otherAnnotationsInfo,
    declaringClass = declaringClass,
    methodName = methodName,
    methodParameters = methodParameters,
)

fun commonPreviewBuilder(
    previewIndex: Int? = null,
    otherAnnotationsInfo: AnnotationInfoList? = null,
    declaringClass: String = "",
    methodName: String = "",
    methodParameters: String = "",
): ComposablePreview<CommonPreviewInfo> = createComposablePreview(
    previewInfo = CommonPreviewInfo(),
    previewIndex = previewIndex,
    otherAnnotationsInfo = otherAnnotationsInfo,
    declaringClass = declaringClass,
    methodName = methodName,
    methodParameters = methodParameters,
)

fun glancePreviewBuilder(
    previewIndex: Int? = null,
    otherAnnotationsInfo: AnnotationInfoList? = null,
    declaringClass: String = "",
    methodName: String = "",
    methodParameters: String = "",
): ComposablePreview<GlancePreviewInfo> = createComposablePreview(
    previewInfo = GlancePreviewInfo(),
    previewIndex = previewIndex,
    otherAnnotationsInfo = otherAnnotationsInfo,
    declaringClass = declaringClass,
    methodName = methodName,
    methodParameters = methodParameters,
)