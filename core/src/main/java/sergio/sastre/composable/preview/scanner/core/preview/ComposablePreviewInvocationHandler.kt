package sergio.sastre.composable.preview.scanner.core.preview

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import kotlin.math.pow
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.kotlinFunction

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
        val safeArgs1 = args ?: emptyArray()
        val defaultParamSize =
            composableMethod.kotlinFunction!!.parameters.filter { it.isOptional }.size
        val defaultParamsAsNull: Array<out Any?> = arrayOfNulls(defaultParamSize)
        val extras: MutableList<Int> = if(defaultParamSize > 0) mutableListOf(2.0.pow(defaultParamSize).toInt()-1) else mutableListOf()

        // 4 params -> 2 a la 4
        // 1 return 1,
        // 3 -> returns 1+2,
        // 5 -> returns 1+3,
        // 7 -> returns 1+2+3,
        // 9 -> returns 1+4
        // 11 -> returns 1+2+4
        // 13 -> returns 1+3+4
        // 15 -> returns 1+2+3+4
        val safeArgs: Array<out Any?> =
            (defaultParamsAsNull.toMutableList() + safeArgs1.toMutableList() + extras).toTypedArray()

        val safeArgsWithParam =
            when (parameter != NoParameter) {
                true -> arrayOf(parameter, *safeArgs)
                false -> safeArgs
            }

        return try {
            composableMethod.invoke(null, *safeArgsWithParam)
        } catch (exception: NullPointerException) {
            // This is for @Composables inside a Class
            // WARNING: This just handles one nesting level
            composableMethod.invoke(
                composableMethod.declaringClass.getDeclaredConstructor().newInstance(),
                *safeArgsWithParam
            )
        } catch (exception: IllegalArgumentException) {
            composableMethod.kotlinFunction!!.apply { isAccessible = true }.call(*safeArgsWithParam)
        }
    }
}