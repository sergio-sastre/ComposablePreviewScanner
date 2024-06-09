package sergio.sastre.composable.preview.scanner.core.preview

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

/**
 * Used to handle calls to a [composableMethod].
 * If a [parameter] is provided, it will be used as the first parameter of the call.
 */
internal class ComposablePreviewInvocationHandler(
    private val composableMethod: Method,
    private val parameter: Any?,
) : InvocationHandler {

    /**
     * Used to indicate that no parameter should be used when calling the [composableMethod].
     * We can't use null here as we might want to pass null as an actual parameter to a function.
     */
    object NoParameter

    override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?): Any? {
        val safeArgs = args ?: emptyArray()
        val safeArgsWithParam =
            when (parameter != NoParameter) {
                true -> arrayOf(parameter, *safeArgs)
                false -> safeArgs
            }
        return composableMethod.invoke(null, *safeArgsWithParam)
    }
}