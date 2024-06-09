package sergio.sastre.composable.preview.scanner.core.preview

import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapper
import java.lang.reflect.Proxy

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
            override val declaringClass: String = composablePreviewMapper.previewMethod.declaringClass.toClassName()
            override val methodName: String = composablePreviewMapper.previewMethod.name

            override fun toString(): String {
                return buildList<String> {
                    add(composablePreviewMapper.previewMethod.declaringClass.toClassName())
                    add(composablePreviewMapper.previewMethod.name)
                    if (previewIndex != null) add(previewIndex.toString())
                }.joinToString("_")
            }

            private fun Class<*>.toClassName(): String = canonicalName ?: simpleName

        }
    }
}