package sergio.sastre.composable.preview.scanner.core.preview

import androidx.compose.runtime.reflect.asComposableMethod
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapper
import java.lang.reflect.Proxy

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
            override val methodName: String = composablePreviewMapper.previewMethod.name

            override val methodParametersType: String = methodParametersTypeAsString()

            override fun toString(): String {
                return buildList<String> {
                    add(composablePreviewMapper.previewMethod.declaringClass.toClassName())
                    add(composablePreviewMapper.previewMethod.name)
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
                    .joinToString("_") {
                        // From java.lang.List<java.lang.Integer> to List<Integer>
                        it.typeName
                            .replace(Regex("\\b[a-zA-Z_][a-zA-Z0-9_]*\\."), "")
                            .replace("\\s+".toRegex(), "_") // blanks cause problems with some libs, like Android-Testify
                    }
            }
        }
    }
}