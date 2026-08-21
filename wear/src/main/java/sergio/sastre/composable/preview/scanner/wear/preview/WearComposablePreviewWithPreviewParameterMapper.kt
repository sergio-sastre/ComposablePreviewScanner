package sergio.sastre.composable.preview.scanner.wear.preview

import io.github.classgraph.AnnotationInfoList
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapper
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.full.functions
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.kotlinFunction

class WearComposablePreviewWithPreviewParameterMapper<T, R>(
    private val previewParameterClassName: String,
    override val previewMethod: Method,
    override val previewInfo: T,
    override val annotationsInfo: AnnotationInfoList?,
) : ComposablePreviewMapper<T>(previewMethod, previewInfo, annotationsInfo) {

    private val previewParameterClass: Class<*>? by lazy {
        try {
            Class.forName(previewParameterClassName)
        } catch (e: Exception) {
            null
        }
    }

    private val provideComposablePreview = WearProvideComposablePreview<T, R>()

    private fun Method.findPreviewParameterAnnotation(): Any? {
        val previewParameterClass = previewParameterClass ?: return null
        return annotations.find { it.annotationClass.java.name == previewParameterClass.name } ?:
               parameterAnnotations.flatten().find { it.annotationClass.java.name == previewParameterClass.name }
    }

    private fun createProviderInstance(clazz: Class<*>): Any? {
        return try {
            clazz.getDeclaredConstructor().apply { isAccessible = true }.newInstance()
        } catch (e: Exception) {
            null
        }
    }

    private fun getPropertyValue(instance: Any, propertyName: String): Any? {
        return try {
            instance.javaClass.getMethod(propertyName).invoke(instance)
        } catch (e: Exception) {
            null
        }
    }

    private fun Collection<KFunction<*>>.getDisplayNameFunction(): KFunction<*>? {
        return find { it.name == "getDisplayName" && it.valueParameters.size == 1 }
    }

    fun mapToWearComposablePreviews(): Sequence<WearComposablePreview<T, R>> {
        val previewParameterAnnotation = previewMethod.findPreviewParameterAnnotation()
        if (previewParameterAnnotation == null) {
            return sequenceOf(provideComposablePreview(this))
        }

        val providerClass = getPropertyValue(previewParameterAnnotation, "provider") as? Class<*> ?: return sequenceOf(provideComposablePreview(this))
        val limit = getPropertyValue(previewParameterAnnotation, "limit") as? Int ?: Int.MAX_VALUE
        val providerInstance = createProviderInstance(providerClass) ?: return sequenceOf(provideComposablePreview(this))
        val values = getPropertyValue(providerInstance, "getValues") as? Sequence<*> ?: return sequenceOf(provideComposablePreview(this))

        val displayNameFunction = providerClass.kotlin.functions.getDisplayNameFunction()

        return values.take(limit).mapIndexed { index, value ->
            val displayName = displayNameFunction?.call(providerInstance, value) as? String
            provideComposablePreview(
                composablePreviewMapper = this,
                previewIndex = index,
                previewParameterDisplayName = displayName,
                parameter = value
            )
        }
    }

    override fun mapToComposablePreviews(): Sequence<sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview<T>> {
        throw UnsupportedOperationException("Use mapToWearComposablePreviews() instead")
    }
}
