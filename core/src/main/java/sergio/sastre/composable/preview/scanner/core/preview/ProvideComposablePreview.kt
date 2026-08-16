package sergio.sastre.composable.preview.scanner.core.preview

import androidx.compose.runtime.reflect.asComposableMethod
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapper
import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Proxy
import java.lang.reflect.Type
import java.lang.reflect.WildcardType

/**
 * Provides an invokable ComposablePreview
 */
class ProvideComposablePreview<T> {
    operator fun invoke(
        composablePreviewMapper: ComposablePreviewMapper<T>,
        previewIndex: Int? = null,
        previewParameterDisplayName: String? = null,
        parameter: Any? = ComposablePreviewInvocationHandler.NoParameter,
    ): ComposablePreview<T> {

        val proxy = Proxy.newProxyInstance(
            ComposablePreview::class.java.classLoader,
            arrayOf(ComposablePreview::class.java),
            ComposablePreviewInvocationHandler(
                composableMethod = composablePreviewMapper.previewMethod,
                parameter = parameter,
                annotationsInfo = composablePreviewMapper.annotationsInfo
            ),
        ) as ComposablePreview<T>

        // Wrap the call to the proxy in an object so that we can override the toString method
        // to provide a more descriptive name for the test and resulting snapshot filename.
        return object : ComposablePreview<T> by proxy {
            override val previewInfo: T = composablePreviewMapper.previewInfo
            override val previewIndex: Int? = previewIndex
            override val previewIndexDisplayName: String? = previewParameterDisplayName
            override val otherAnnotationsInfo = composablePreviewMapper.annotationsInfo
            override val declaringClass: String =
                composablePreviewMapper.previewMethod.declaringClass.toClassName()

            /**
             * The JVM method name, including any compiler-generated hash (e.g., "-8Feqmps").
             *
             * This hash is appended by the Compose compiler for overloads or methods using value classes
             * to ensure uniqueness at the JVM level. We intentionally keep the raw JVM name to
             * guarantee unique ComposablePreview IDs (and snapshot filenames), even if it means
             * IDs might change across compiler versions or rebuilds.
             */
            override val methodName: String = composablePreviewMapper.previewMethod.name

            override val methodParametersType: String = methodParametersTypeAsString()

            override fun toString(): String {
                return buildList<String> {
                    add(declaringClass)
                    add(methodName)
                    if (methodParametersType.isNotBlank()){
                        add(methodParametersType)
                    }
                    if (previewIndex != null) {
                        add(previewIndex.toString())
                    }
                }.joinToString("_")
            }

            private fun Class<*>.toClassName(): String = canonicalName ?: simpleName

            /**
             * Returns the type of the (real) parameters as an underscore separated simple string.
             *
             * Preview methods always have compiler-added parameters at the end (Composer, an Int
             * `changed` mask, and — for previews with default parameters — an extra Int default mask).
             * We resolve how many of the trailing parameters are compiler-added via androidx'
             * [asComposableMethod] (`java.lang.reflect` based) rather than kotlin-reflect: resolving
             * `Method.kotlinFunction` throws `KotlinReflectionInternalError` for previews whose
             * signature contains a value class (e.g. a value-class `@PreviewParameter`), because
             * kotlin-reflect's ValueClassAwareCaller does not account for the synthetic Compose params.
             */
            @Suppress("NewApi")
            private fun methodParametersTypeAsString(): String {
                val previewMethod = composablePreviewMapper.previewMethod
                val realParametersCount = previewMethod.asComposableMethod()?.parameterCount
                    ?: (previewMethod.parameterTypes.size - 2).coerceAtLeast(0)
                return previewMethod
                    .genericParameterTypes
                    .take(realParametersCount)
                    .mapIndexed { index, type ->
                        val v = if (index == 0) parameter else null
                        type.toResolvedTypeName(v)
                            .replace(Regex("\\b[a-zA-Z_][a-zA-Z0-9_]*\\."), "")
                            .replace("\\s+".toRegex(), "_") // blanks cause problems with some libs, like Android-Testify
                    }
                    .joinToString("_")
            }

            private fun Type.toResolvedTypeName(value: Any? = null, isInsideGeneric: Boolean = false): String {
                return when (this) {
                    is Class<*> -> {
                        if (isArray) return "${componentType.toResolvedTypeName(isInsideGeneric = isInsideGeneric)}[]"
                        val unbox = getUnderlyingType()
                        if (unbox != null) {
                            val nullable = !isPrimitive && !isInsideGeneric
                            return "${simpleName}${if (nullable) "?" else ""}_${unbox.simpleName}"
                        }
                        if (value != null && value != ComposablePreviewInvocationHandler.NoParameter) {
                            val valueClass = value.javaClass
                            val valueUnbox = valueClass.getUnderlyingType()
                            if (valueUnbox != null && (this == valueUnbox || this.toString() == valueUnbox.toString())) {
                                return "${valueClass.simpleName}_${valueUnbox.simpleName}"
                            }
                        }
                        simpleName
                    }
                    is ParameterizedType -> {
                        val raw = (rawType as? Class<*>)?.simpleName ?: rawType.toString().substringAfterLast('.')
                        val args = actualTypeArguments.joinToString(", ") { it.toResolvedTypeName(isInsideGeneric = true) }
                        "$raw<$args>"
                    }
                    is GenericArrayType -> "${genericComponentType.toResolvedTypeName(isInsideGeneric = isInsideGeneric)}[]"
                    is WildcardType -> {
                        val lowerBound = lowerBounds.firstOrNull()
                        if (lowerBound != null) {
                            "? super ${lowerBound.toResolvedTypeName(isInsideGeneric = isInsideGeneric)}"
                        } else {
                            val upperBound = upperBounds.firstOrNull()
                            if (upperBound == null || upperBound == Any::class.java || upperBound.toString() == "class java.lang.Object") {
                                "?"
                            } else {
                                "? extends ${upperBound.toResolvedTypeName(isInsideGeneric = isInsideGeneric)}"
                            }
                        }
                    }
                    else -> toString().substringAfterLast('.')
                }
            }

            private fun Class<*>.getUnderlyingType(): Class<*>? =
                try {
                    declaredMethods.firstOrNull { it.name == "unbox-impl" }?.returnType
                } catch (_: Exception) {
                    null
                }
        }
    }
}