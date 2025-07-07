package sergio.sastre.composable.preview.scanner.glance

import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.classgraph.AnnotationInfoList
import io.github.classgraph.AnnotationParameterValueList
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.ProvideComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewInfoMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapperCreator
import sergio.sastre.composable.preview.scanner.core.scanner.ComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.ClasspathPreviewsFinder
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.isAccessible

/**
 * Scans the target package trees for @androidx.compose.ui.tooling.preview.Preview and can returns their Composable,
 * which can be invoked, along with other infos that might be relevant, like the Preview values (i.e. AndroidPreviewInfo)
 * and other annotations applied to the @Preview
 */
class GlanceComposablePreviewScanner : ComposablePreviewScanner<GlancePreviewInfo>(
    findComposableWithPreviewsInClass = ClasspathPreviewsFinder(
        annotationToScanClassName = "androidx.glance.preview.Preview",
        previewInfoMapper = GlanceComposablePreviewInfoMapper(),
        previewMapperCreator = GlancePreviewMapperCreator(),
    ),
    defaultPackageTreesOfCrossModuleCustomPreviews = emptyList()
) {

    private class GlanceComposablePreviewInfoMapper :
        ComposablePreviewInfoMapper<GlancePreviewInfo> {
        override fun mapToComposablePreviewInfo(parameters: AnnotationParameterValueList): GlancePreviewInfo =
            GlancePreviewInfo(
                widthDp = parameters.valueForKey("widthDp") ?: -1,
                heightDp = parameters.valueForKey("heightDp") ?: -1
            )

        @Suppress("UNCHECKED_CAST")
        private fun <T> AnnotationParameterValueList.valueForKey(key: String): T? =
            this[key]?.value as T?
    }

    private class GlancePreviewMapperCreator : ComposablePreviewMapperCreator<GlancePreviewInfo> {
        override fun createComposablePreviewMapper(
            previewMethod: Method,
            previewInfo: GlancePreviewInfo,
            annotationsInfo: AnnotationInfoList?
        ): ComposablePreviewMapper<GlancePreviewInfo> =
            GlancePreviewMapper(
                previewMethod = previewMethod,
                previewInfo = previewInfo,
                annotationsInfo = annotationsInfo,
            )

        private data class GlancePreviewMapper<T>(
            override val previewMethod: Method,
            override val previewInfo: T,
            override val annotationsInfo: AnnotationInfoList?,
        ) : ComposablePreviewMapper<T>(previewMethod, previewInfo, annotationsInfo) {

            private val provideComposablePreview =
                ProvideComposablePreview<T>()

            private fun Method.findPreviewParameterAnnotation(): PreviewParameter? {
                return this.parameterAnnotations
                    .flatMap { it.toList() }
                    .find { it is PreviewParameter } as PreviewParameter?
            }

            /**
             * Creates an instance of the [PreviewParameterProvider] using the no-args constructor
             * even if the class or constructor is private.
             */
            private fun KClass<out PreviewParameterProvider<*>>.createInstanceUnsafe(): PreviewParameterProvider<*> {
                val noArgsConstructor =
                    constructors.single { it.parameters.all(KParameter::isOptional) }
                noArgsConstructor.isAccessible = true
                return noArgsConstructor.call()
            }

            override fun mapToComposablePreviews(): Sequence<ComposablePreview<T>> {
                val previewParameterAnnotation = previewMethod.findPreviewParameterAnnotation()
                    ?: return sequenceOf(provideComposablePreview(this))

                return previewParameterAnnotation.provider.createInstanceUnsafe()
                    .values
                    .take(previewParameterAnnotation.limit)
                    .mapIndexed { index, value -> index to value }
                    .map { (index, value) ->
                        provideComposablePreview(
                            composablePreviewMapper = this,
                            previewIndex = index,
                            parameter = value,
                        )
                    }
            }
        }
    }
}
