package sergio.sastre.composable.preview.scanner.core.preview

import sergio.sastre.composable.preview.scanner.core.preview.exception.PreviewParameterIsNotFirstArgumentException
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Modifier
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
        val safeArgs: Array<out Any?> = fillMissingComposeArgs(args)
        val safeArgsWithParam =
            when (parameter != NoParameter) {
                true -> arrayOf(parameter, *safeArgs)
                false -> safeArgs
            }

        val isInsideClass = !Modifier.isStatic(composableMethod.modifiers)
        val kotlinComposableMethod = composableMethod.kotlinFunction!!.apply { isAccessible = true }
        return when (isInsideClass) {
            false -> kotlinComposableMethod.call(*safeArgsWithParam)
            true -> kotlinComposableMethod.call(
                composableMethod.declaringClass.getDeclaredConstructor().newInstance(),
                *safeArgsWithParam
            )
        }
    }

    private fun fillMissingComposeArgs(passedComposeArgs: Array<out Any>?): Array<out Any?> {
        val safeArgs = passedComposeArgs ?: emptyArray()
        val allParams = composableMethod.kotlinFunction!!.parameters
        val defaultParams = allParams.filter { it.isOptional }
        when (defaultParams.isEmpty()) {
            true -> return safeArgs
            false -> {
                // Very rare case:
                // if @PreviewParameters & default parameters available
                // And @PreviewParameters is not the first of all arguments
                // we cannot handle it:
                // we don't know the value of that argument to pass it at a certain index
                // and it'd throw an UndeclaredThrowableException
                //
                // Update: It seems that from AS Meerkat on, this is enforced :)
                if (allParams.any { !it.isOptional && it.index != 0 }){
                    throw PreviewParameterIsNotFirstArgumentException(
                        className =  composableMethod.declaringClass.name,
                        methodName = composableMethod.name
                    )
                }

                // In kotlin reflect, null params resolve to default kotlin params.
                // These params are added at the beginning of the method by the Compose Compiler
                val defaultParamsAsNull: Array<out Any?> = arrayOfNulls(defaultParams.size)

                // When default params available, the Compose Compiler adds a mask at the end of the method
                // to map the default parameters to their corresponding default values.
                //
                // This mask contains 1 bit for each parameter (0 -> null, 1 -> default value),
                // including default params and those passed via @PreviewParameters
                // so in order to resolve all parameters to their corresponding values,
                // you need the highest possible number in binary e.g.
                // 1 param  -> 1
                // 2 params -> 11 -> 3
                // 3 params -> 111 -> 7
                // 4 params -> 1111 -> 15
                // x params -> 2 pow (x) - 1
                val paramsMask: MutableList<Int> = mutableListOf(2.0.pow(allParams.size).toInt() - 1)
                return (defaultParamsAsNull.toMutableList() + safeArgs.toMutableList() + paramsMask).toTypedArray()
            }
        }
    }
}