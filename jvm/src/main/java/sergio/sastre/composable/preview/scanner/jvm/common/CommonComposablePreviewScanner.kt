package sergio.sastre.composable.preview.scanner.jvm.common

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
import kotlin.reflect.KParameter
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

/**
 * Scans the target package trees for the @Preview s in "common" in a Compose Multiplatform project
 * and returns their Composable, which can be invoked.
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
            ): CommonPreviewInfo = CommonPreviewInfo(
                name = parameters.valueForKey("name") ?: "",
                group = parameters.valueForKey("group") ?: "",
                widthDp = parameters.valueForKey("widthDp") ?: -1,
                heightDp = parameters.valueForKey("heightDp") ?: -1,
                locale = parameters.valueForKey("locale") ?: "",
                showBackground = parameters.valueForKey("showBackground") ?: false,
                backgroundColor = parameters.valueForKey("backgroundColor") ?: 0L,
            )

            @Suppress("UNCHECKED_CAST")
            private fun <T> AnnotationParameterValueList.valueForKey(key: String): T? =
                this[key]?.value as T?
        }

        private class CommonPreviewMapperCreator :
            ComposablePreviewMapperCreator<CommonPreviewInfo> {
            override fun createComposablePreviewMapper(
                previewMethod: Method,
                previewInfo: CommonPreviewInfo,
                annotationsInfo: AnnotationInfoList?
            ): ComposablePreviewMapper<CommonPreviewInfo> =
                CommonPreviewMapper(
                    previewMethod = previewMethod,
                    previewInfo = previewInfo,
                    annotationsInfo = annotationsInfo
                )
        }

        private data class CommonPreviewMapper<T>(
            override val previewMethod: Method,
            override val previewInfo: T,
            override val annotationsInfo: AnnotationInfoList?,
        ) : ComposablePreviewMapper<T>(previewMethod, previewInfo, annotationsInfo) {

            private val provideComposablePreview = ProvideComposablePreview<T>()

            private fun Method.findPreviewParameterAnnotation(): Any? {
                val previewParameterClass =
                    try {
                        Class.forName("org.jetbrains.compose.ui.tooling.preview.PreviewParameter")
                    } catch (e: ClassNotFoundException) {
                        return null
                    }

                return this.parameterAnnotations
                    .flatMap { it.toList() }
                    .find { previewParameterClass.isInstance(it) }
            }

            /**
             * Creates an instance of the provider class using reflection.
             */
            private fun createProviderInstance(providerClass: Class<*>): Any? {
                val providerKClass = providerClass.kotlin
                val noArgsConstructor = providerKClass.constructors.find {
                    it.parameters.all(
                        KParameter::isOptional
                    )
                }
                noArgsConstructor?.isAccessible = true
                return noArgsConstructor?.call()
            }

            override fun mapToComposablePreviews(): Sequence<ComposablePreview<T>> {
                fun getPropertyValue(target: Any, propertyName: String): Any? {
                    return target::class.memberProperties
                        .find { it.name == propertyName }
                        ?.apply { isAccessible = true }
                        ?.getter
                        ?.call(target)
                }

                val previewParameterAnnotation = previewMethod.findPreviewParameterAnnotation()
                    ?: return sequenceOf(provideComposablePreview(this))

                val providerClass =
                    getPropertyValue(previewParameterAnnotation, "provider") as? Class<*>
                        ?: return sequenceOf(provideComposablePreview(this))

                val providerInstance = createProviderInstance(providerClass)
                    ?: return sequenceOf(provideComposablePreview(this))

                val values = getPropertyValue(providerInstance, "values") as? Sequence<Any>
                    ?: return sequenceOf(provideComposablePreview(this))

                val limit = getPropertyValue(previewParameterAnnotation, "limit") as? Int
                    ?: Int.MAX_VALUE

                return values
                    .take(limit)
                    .mapIndexed { index, value ->
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
