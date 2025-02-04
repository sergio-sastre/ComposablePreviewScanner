package sergio.sastre.composable.preview.scanner.core.preview

import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapper
import java.lang.reflect.Proxy
import kotlin.reflect.jvm.kotlinFunction

/**
 * Provides an invokable ComposablePreview
 */
class ProvideComposablePreview<T> {
    operator fun invoke(
        composablePreviewMapper: ComposablePreviewMapper<T>,
        previewIndex: Int? = null,
        parameter: Any? = ComposablePreviewInvocationHandler.NoParameter,
    ): ComposablePreview<T> {

        val proxy = Proxy.newProxyInstance(
            ComposablePreview::class.java.classLoader,
            arrayOf(ComposablePreview::class.java),
            ComposablePreviewInvocationHandler(
                composableMethod = composablePreviewMapper.previewMethod,
                parameter = parameter
            ),
        ) as ComposablePreview<T>

        // Wrap the call to the proxy in an object so that we can override the toString method
        // to provide a more descriptive name for the test and resulting snapshot filename.
        return object : ComposablePreview<T> by proxy {
            override val previewInfo: T = composablePreviewMapper.previewInfo
            override val previewIndex: Int? = previewIndex
            override val otherAnnotationsInfo = composablePreviewMapper.annotationsInfo
            override val declaringClass: String =
                composablePreviewMapper.previewMethod.declaringClass.toClassName()
            override val methodName: String = composablePreviewMapper.previewMethod.name

            override val methodParametersType: String = methodParametersTypeAsString()

            override fun toString(): String {
                return buildList<String> {
                    add(composablePreviewMapper.previewMethod.declaringClass.toClassName())
                    add(composablePreviewMapper.previewMethod.name)
                    add(methodParametersTypeAsString())
                    if (previewIndex != null) add(previewIndex.toString())
                }.joinToString("_")
            }

            private fun Class<*>.toClassName(): String = canonicalName ?: simpleName

            /**
             * Returns the type of the parameters as an underscore separated simple string
             *
             * Composable from preview methods contain always 2 parameters added at the end by the compiler:
             * 1. androidx.compose.runtime.Composer
             * 2. Int
             *
             * Moreover, if it is a Preview with default parameters, one extra 3. argument is added as a mask for default values
             *
             * All these 2-3 parameters are placed at the end of the method.
             */
            @Suppress("NewApi")
            private fun methodParametersTypeAsString(): String {
                val previewMethod = composablePreviewMapper.previewMethod
                val hasDefaultParams = previewMethod.kotlinFunction!!.parameters.any { it.isOptional }
                val count = if (hasDefaultParams) 3 else 2
                return previewMethod
                    .genericParameterTypes
                    .dropLast(count)
                    .joinToString("_") {
                        // From java.lang.List<java.lang.Integer> to List<Integer>
                        it.typeName.replace(Regex("\\b[a-zA-Z_][a-zA-Z0-9_]*\\."), "")
                    }
            }
        }
    }
}