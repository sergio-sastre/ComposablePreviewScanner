package sergio.sastre.composable.preview.scanner.core.preview

import androidx.compose.runtime.Composable
import io.github.classgraph.AnnotationInfoList
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

/**
 * A Unique ComposablePreview. @Composable methods annotated with multi-previews @Preview result into
 * one ComposablePreviews per @Preview, each of them identified by a previewIndex
 */
interface ComposablePreview<T> {
    val previewInfo: T
    val previewIndex: Int?
    val otherAnnotationsInfo: AnnotationInfoList?
    val declaringClass: String
    val methodName: String

    @Composable
    operator fun invoke()
}

/**
 * Gets the T annotation if saved via ScanResultFilter#includeAnnotationInfoForAllOf(...).
 * It uses reflection for that.
 *
 * WARNING: This might cause issues with:
 * 1. Repeatable annotations -> if duplicated only one is taken
 * 2. Annotations that have as params any of the following types (throws exceptions):
 *  2.1. Annotation
 *  2.2  KClass
 */
inline fun <reified T : Annotation> ComposablePreview<*>.getAnnotation(): T? {
    val annotationParams = otherAnnotationsInfo
        ?.filter { it.name == T::class.java.name }
        ?.firstOrNull()?.parameterValues
        ?: return null
    val annotationValues = mutableMapOf<KParameter, Any?>()

    val primaryConstructor = T::class.primaryConstructor
    for (paramName in annotationParams.map { it.name }) {
        val paramValue = annotationParams.getValue(paramName)
        val parameter = primaryConstructor?.parameters?.find { it.name == paramName }
        if (parameter != null) {
            val parameterType = parameter.type.classifier as? KClass<*>
            val value = if (parameterType?.java?.isEnum == true) {
                // Resolve enum constant by its name
                parameterType.java.enumConstants.firstOrNull { enumConstant ->
                    (enumConstant as? Enum<*>)?.name == paramValue.toString().substringAfterLast(".")
                }
            } else {
                paramValue
            }
            annotationValues[parameter] = value
        }
    }

    return when (primaryConstructor != null) {
        true -> {
            try {
                primaryConstructor.callBy(annotationValues)
            } catch (e: Exception) {
                throw e
            }
        }
        false -> null
    }
}
