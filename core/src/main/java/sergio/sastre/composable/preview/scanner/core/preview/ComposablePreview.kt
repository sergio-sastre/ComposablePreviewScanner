package sergio.sastre.composable.preview.scanner.core.preview

import androidx.compose.runtime.Composable
import io.github.classgraph.AnnotationClassRef
import io.github.classgraph.AnnotationInfoList
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

/**
 * A Unique ComposablePreview.
 *
 * @Composable methods annotated with multi @Preview result into
 * one ComposablePreview per @Preview, each of them identified by its [previewInfo]
 *
 * @Composable methods that take @PreviewParameter as argument result into
 * one ComposablePreview per parameter, each of them identified by its [previewIndex]
 */
interface ComposablePreview<T> {
    val previewInfo: T
    val previewIndex: Int?
    val previewIndexDisplayName: String?
    val otherAnnotationsInfo: AnnotationInfoList?
    val declaringClass: String
    val methodName: String
    val methodParametersType: String

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
            val value = when {
                parameterType?.java?.isEnum == true -> {
                    // Resolve enum constant by its name
                    parameterType.java.enumConstants.firstOrNull { enumConstant ->
                        (enumConstant as? Enum<*>)?.name == paramValue.toString()
                            .substringAfterLast(".")
                    }
                }

                parameterType == KClass::class -> {
                    (paramValue as? AnnotationClassRef)?.let {
                        Class.forName(it.name).kotlin
                    } ?: paramValue
                }

                else -> paramValue
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

/**
 * A thread-safe in-memory cache for PreviewWrapperProvider instances and their Wrap method.
 * Internal to the core module to avoid leaking state to users while remaining accessible to extensions.
 */
internal object PreviewWrapperCache {
    private val cache = ConcurrentHashMap<String, Pair<Any, Method?>>()

    fun getProviderAndWrapMethod(className: String): Pair<Any, Method?>? {
        return try {
            val cached = cache[className]
            if (cached != null) {
                return cached
            }
            val clazz = Class.forName(className)
            val instance = clazz
                .getDeclaredConstructor()
                .apply { isAccessible = true }
                .newInstance()
            // Find Wrap(Function2, Composer, Int)
            val wrapMethod = clazz.declaredMethods
                .find { method ->
                    method.name == "Wrap" &&
                            method.parameterCount == 3 &&
                            method.parameterTypes[0].name == "kotlin.jvm.functions.Function2" &&
                            method.parameterTypes[1].name == "androidx.compose.runtime.Composer"
                }
                ?.apply { isAccessible = true }
            val newPair = instance to wrapMethod
            val existing = cache.putIfAbsent(className, newPair)
            existing ?: newPair
        } catch (e: Exception) {
            null
        }
    }
}
