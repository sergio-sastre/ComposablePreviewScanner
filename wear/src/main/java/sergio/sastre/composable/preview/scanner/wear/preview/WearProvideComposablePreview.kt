package sergio.sastre.composable.preview.scanner.wear.preview

import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapper
import java.lang.reflect.Proxy
import kotlin.reflect.jvm.kotlinFunction

class WearProvideComposablePreview<T, R> {
    operator fun invoke(
        composablePreviewMapper: ComposablePreviewMapper<T>,
        previewIndex: Int? = null,
        previewParameterDisplayName: String? = null,
        parameter: Any? = WearComposablePreviewInvocationHandler.NoParameter,
    ): WearComposablePreview<T, R> {

        val proxy = Proxy.newProxyInstance(
            WearComposablePreview::class.java.classLoader,
            arrayOf(WearComposablePreview::class.java),
            WearComposablePreviewInvocationHandler(
                composableMethod = composablePreviewMapper.previewMethod,
                parameter = parameter,
                annotationsInfo = composablePreviewMapper.annotationsInfo
            ),
        ) as WearComposablePreview<T, R>

        return object : WearComposablePreview<T, R> by proxy {
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

            @Suppress("NewApi")
            private fun methodParametersTypeAsString(): String {
                val previewMethod = composablePreviewMapper.previewMethod
                val kotlinFunc = previewMethod.kotlinFunction ?: return ""
                val hasDefaultParams = kotlinFunc.parameters.any { it.isOptional }
                
                val isComposable = previewMethod.annotations.any { 
                    it.annotationClass.qualifiedName == "androidx.compose.runtime.Composable" 
                }

                val dropCount = if (isComposable) {
                    if (hasDefaultParams) 3 else 2
                } else {
                    0
                }

                return previewMethod
                    .genericParameterTypes
                    .dropLast(dropCount)
                    .joinToString("_") {
                        it.typeName
                            .replace(Regex("\\b[a-zA-Z_][a-zA-Z0-9_]*\\."), "")
                            .replace("\\s+".toRegex(), "_")
                    }
            }
        }
    }
}
