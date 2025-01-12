package sergio.sastre.composable.preview.scanner.jvm.common

import io.github.classgraph.AnnotationInfoList
import io.github.classgraph.AnnotationParameterValueList
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.ProvideComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewInfoMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapperCreator
import sergio.sastre.composable.preview.scanner.core.scanner.ComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.core.scanner.config.classloader.classpath.previewfinder.ClasspathPreviewsFinder
import java.lang.reflect.Method

/**
 * Scans the target package trees for the common @Preview s and returns their Composable,
 * which can be invoked.
 *
 * This is meant to be used for @Composables using the @Preview located in "org.jetbrains.compose.ui.tooling.preview.Preview",
 * which is used in Compose Multiplatform for common previews.
 *
 * WARNING: Since ComposablePreviewScanner is based on ClassGraph, which is a Jvm-based Class Scanner,
 * this can only be used inside jvm sources, like Desktop and Android.
 */
class CommonComposablePreviewScanner : ComposablePreviewScanner<CommonPreviewInfo>(
    findComposableWithPreviewsInClass = CommonComposablePreviewFinder()
) {
    private object CommonComposablePreviewFinder {
        operator fun invoke(): ClasspathPreviewsFinder<CommonPreviewInfo> =
            ClasspathPreviewsFinder(
                annotationToScanClassName = "org.jetbrains.compose.ui.tooling.preview.Preview",
                previewInfoMapper = CommonComposablePreviewInfoMapper(),
                previewMapperCreator = CommonPreviewMapperCreator()
            )

        private class CommonComposablePreviewInfoMapper :
            ComposablePreviewInfoMapper<CommonPreviewInfo> {
            override fun mapToComposablePreviewInfo(
                parameters: AnnotationParameterValueList
            ): CommonPreviewInfo = CommonPreviewInfo
        }

        private class CommonPreviewMapperCreator :
            ComposablePreviewMapperCreator<CommonPreviewInfo> {
            override fun createComposablePreviewMapper(
                previewMethod: Method,
                previewInfo: CommonPreviewInfo,
                annotationsInfo: AnnotationInfoList?
            ): ComposablePreviewMapper<CommonPreviewInfo> =
                object :
                    ComposablePreviewMapper<CommonPreviewInfo>(
                        previewMethod = previewMethod,
                        previewInfo = previewInfo,
                        annotationsInfo = annotationsInfo
                    ) {
                    override fun mapToComposablePreviews(): Sequence<ComposablePreview<CommonPreviewInfo>> =
                        sequenceOf(ProvideComposablePreview<CommonPreviewInfo>().invoke(this))
                }
        }
    }
}
