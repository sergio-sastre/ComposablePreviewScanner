package sergio.sastre.composable.preview.scanner.core.preview.mappers

import io.github.classgraph.AnnotationInfoList
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.ProvideComposablePreview
import java.lang.reflect.Method
import kotlin.reflect.KParameter
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

/**
 * A [ComposablePreviewMapper] that can resolve parameters passed to the ComposablePreview
 * if annotated with [previewParameterClassName]
 *
 * @param previewParameterClassName The absolute name of the PreviewParameter class.
 */
data class ComposablePreviewWithPreviewParameterMapper<T>(
    private val previewParameterClassName: String,
    override val previewMethod: Method,
    override val previewInfo: T,
    override val annotationsInfo: AnnotationInfoList?,
) : ComposablePreviewMapper<T>(previewMethod, previewInfo, annotationsInfo) {

    private val previewParameterClass: Class<*>? by lazy {
        try {
            Class.forName(previewParameterClassName)
        } catch (e: ClassNotFoundException) {
            // PreviewParameter not available on classpath (expected on pure JVM)
            null
        }
    }
    private val provideComposablePreview = ProvideComposablePreview<T>()

    private fun Method.findPreviewParameterAnnotation(): Any? =
        previewParameterClass?.let { previewParameterAnnotation ->
            this.parameterAnnotations
                .flatMap { it.toList() }
                .find { previewParameterAnnotation.isInstance(it) }
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

    private fun getPropertyValue(target: Any, propertyName: String): Any? {
        return target::class.memberProperties
            .find { it.name == propertyName }
            ?.apply { isAccessible = true }
            ?.getter
            ?.call(target)
    }

    override fun mapToComposablePreviews(): Sequence<ComposablePreview<T>> {
        val previewParameterAnnotation = previewMethod.findPreviewParameterAnnotation()
            ?: return sequenceOf(provideComposablePreview(this))

        val providerClass =
            getPropertyValue(previewParameterAnnotation, "provider") as? Class<*>
                ?: return sequenceOf(provideComposablePreview(this))

        val providerInstance = createProviderInstance(providerClass)
            ?: return sequenceOf(provideComposablePreview(this))

        val values = getPropertyValue(providerInstance, "values") as? Sequence<Any?>
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