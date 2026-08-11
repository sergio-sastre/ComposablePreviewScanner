package sergio.sastre.composable.preview.scanner.core.preview

import androidx.compose.runtime.Composer
import androidx.compose.runtime.reflect.asComposableMethod
import io.github.classgraph.AnnotationClassRef
import io.github.classgraph.AnnotationInfoList
import sergio.sastre.composable.preview.scanner.core.preview.exception.PreviewParameterIsNotFirstArgumentException
import sergio.sastre.composable.preview.scanner.core.scanresult.filter.PREVIEW_WRAPPER_ANNOTATION
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * Used to handle calls to a [composableMethod].
 * If a [parameter] is provided, it will be used as the first parameter of the call.
 */
internal class ComposablePreviewInvocationHandler(
    private val composableMethod: Method,
    private val parameter: Any?,
    private val annotationsInfo: AnnotationInfoList?,
) : InvocationHandler {

    /**
     * Used to indicate that no parameter should be used when calling the [composableMethod].
     * We can't use null here as we might want to pass null as an actual parameter to a function.
     */
    object NoParameter

    override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?): Any? {
        if (method?.name != "invoke") return method?.invoke(this, *(args ?: emptyArray()))

        // Args of ComposablePreview.invoke() are the compiler-added [Composer, changed] pair.
        val composer = args?.getOrNull(args.size - 2) as? Composer
        val changed = args?.getOrNull(args.size - 1) as? Int ?: 0

        val previewWrapperData = getPreviewWrapperProvider()
        val wrapMethod = previewWrapperData?.second
        val canWrapPreview = wrapMethod != null && (args?.size ?: 0) >= 2
        return when (canWrapPreview) {
            false -> invokeComposable(composer)
            true -> {
                val content: (Any?, Int) -> Unit = { wrapComposer, _ ->
                    invokeComposable(wrapComposer as? Composer)
                }
                wrapMethod!!.invoke(previewWrapperData.first, content, composer, changed)
            }
        }
    }

    /**
     * Invokes the @Composable via androidx' [asComposableMethod] (`java.lang.reflect` based) instead of
     * kotlin-reflect. ComposableMethod supplies the compiler-added Composer/changed/default-mask
     * parameters itself and fills trailing default parameters, so we only pass the real
     * @PreviewParameter value (if any).
     *
     * This deliberately avoids `kotlin-reflect`: resolving/calling `Method.kotlinFunction` throws
     * `KotlinReflectionInternalError` for previews whose signature contains a value class, because
     * kotlin-reflect's ValueClassAwareCaller derives the expected argument count from the Kotlin
     * descriptor and cannot reconcile the Compose compiler's synthetic JVM parameters.
     */
    private fun invokeComposable(composer: Composer?): Any? {
        val composable = composableMethod.asComposableMethod()
            ?: error(
                "Not a @Composable method: " +
                    "${composableMethod.declaringClass.name}.${composableMethod.name}",
            )

        // Unconditional: a @PreviewParameter anywhere but first can't be placed correctly, whether or
        // not a value was resolved for it. (Matches the previous behavior.)
        requirePreviewParameterIsFirstArgument()

        val realArgs: Array<Any?> = when (parameter) {
            NoParameter -> emptyArray()
            else -> arrayOf(coerceToParameterType(parameter, composable.parameterTypes.firstOrNull()))
        }

        val receiver = when (Modifier.isStatic(composableMethod.modifiers)) {
            true -> null
            false -> composableMethod.declaringClass.getDeclaredConstructor()
                .apply { isAccessible = true }
                .newInstance()
        }

        return composable.invoke(
            requireNotNull(composer) { "A Composer is required to invoke a @Composable preview" },
            receiver,
            *realArgs,
        )
    }

    /**
     * A @PreviewParameter provider yields boxed value-class instances (e.g. a Dp), but the JVM method
     * expects the underlying type (e.g. float). Unbox via the value class's synthetic `unbox-impl` when
     * the value does not already fit the target parameter slot (nullable/generic slots stay boxed).
     * Loops to handle the rare nested-value-class case.
     */
    private fun coerceToParameterType(value: Any?, targetType: Class<*>?): Any? {
        if (targetType == null) return value
        var current: Any? = value
        while (true) {
            val boxed = current ?: break
            if (targetType.isInstance(boxed)) break
            val unbox = boxed.javaClass.declaredMethods.firstOrNull { it.name == "unbox-impl" } ?: break
            unbox.isAccessible = true
            current = unbox.invoke(boxed)
        }
        return current
    }

    /**
     * Compose only supports a single @PreviewParameter and (from AS Meerkat on) enforces it is the
     * first parameter. We forward the provider value as the first argument, so if it is not first we
     * cannot place it correctly — fail explicitly rather than render the wrong argument.
     */
    private fun requirePreviewParameterIsFirstArgument() {
        val previewParameterIndex = composableMethod.parameterAnnotations.indexOfFirst { annotations ->
            annotations.any { it.annotationClass.java.name in PREVIEW_PARAMETER_ANNOTATIONS }
        }
        if (previewParameterIndex > 0) {
            throw PreviewParameterIsNotFirstArgumentException(
                className = composableMethod.declaringClass.name,
                methodName = composableMethod.name,
            )
        }
    }

    private fun getPreviewWrapperProvider(): Pair<Any, Method?>? {
        val annotationParams = annotationsInfo
            ?.firstOrNull { it.name == PREVIEW_WRAPPER_ANNOTATION }
            ?.parameterValues
            ?: return null

        val wrapperClassRef = annotationParams
            .firstOrNull { it.name == "wrapper" }
            ?.value as? AnnotationClassRef
            ?: return null

        return PreviewWrapperCache.getProviderAndWrapMethod(wrapperClassRef.name)
    }

    private companion object {
        private val PREVIEW_PARAMETER_ANNOTATIONS = setOf(
            "androidx.compose.ui.tooling.preview.PreviewParameter",
            "org.jetbrains.compose.ui.tooling.preview.PreviewParameter",
        )
    }
}
