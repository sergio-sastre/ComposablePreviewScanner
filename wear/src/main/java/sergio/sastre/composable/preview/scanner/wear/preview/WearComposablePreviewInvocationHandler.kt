package sergio.sastre.composable.preview.scanner.wear.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import io.github.classgraph.AnnotationInfoList
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.math.pow
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.kotlinFunction

internal class WearComposablePreviewInvocationHandler(
    private val composableMethod: Method,
    private val parameter: Any?,
    private val annotationsInfo: AnnotationInfoList?,
) : InvocationHandler {

    object NoParameter

    override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?): Any? {
        if (method?.name != "invoke") return method?.invoke(this, *(args ?: emptyArray()))

        val isComposable = composableMethod.parameterTypes.any { 
            it.name == "androidx.compose.runtime.Composer"
        }

        return if (isComposable) {
            invokeComposable(args)
        } else {
            invokeNonComposable(args)
        }
    }

    private fun invokeComposable(args: Array<out Any>?): Any? {
        val safeArgs = fillMissingComposeArgs(args)

        // Extract composer and changed from the end of args (added by Compose compiler)
        val composer = args?.getOrNull(args.size - 2)
        val changed = args?.getOrNull(args.size - 1) as? Int ?: 0

        val allParams = composableMethod.kotlinFunction!!.parameters
        val hasDefaultParams = allParams.any { it.isOptional }

        val updatedArgs = arrayOf(*safeArgs)
        if (updatedArgs.size >= (if (hasDefaultParams) 3 else 2)) {
            val offset = if (hasDefaultParams) 1 else 0
            updatedArgs[updatedArgs.size - 2 - offset] = composer
            updatedArgs[updatedArgs.size - 1 - offset] = changed
        }

        val safeArgsWithParam =
            when (parameter != NoParameter) {
                true -> arrayOf(parameter, *updatedArgs)
                false -> updatedArgs
            }

        val isInsideClass = !Modifier.isStatic(composableMethod.modifiers)
        val kotlinComposableMethod =
            composableMethod.kotlinFunction!!.apply { isAccessible = true }

        return when (isInsideClass) {
            false -> kotlinComposableMethod.call(*safeArgsWithParam)
            true -> {
                val instance = composableMethod.declaringClass.getDeclaredConstructor().apply { isAccessible = true }.newInstance()
                kotlinComposableMethod.call(instance, *safeArgsWithParam)
            }
        }
    }

    @OptIn(androidx.compose.runtime.InternalComposeApi::class)
    @Suppress("UNCHECKED_CAST")
    private fun invokeNonComposable(args: Array<out Any>?): Any? {
        val composer = args?.getOrNull(args.size - 2) as? androidx.compose.runtime.Composer
        val context = composer?.consume(androidx.compose.ui.platform.LocalContext)
        
        val isInsideClass = !Modifier.isStatic(composableMethod.modifiers)
        val kotlinComposableMethod =
            composableMethod.kotlinFunction!!.apply { isAccessible = true }

        // Wear Tile previews usually take a Context as the only parameter
        val tileArgs = if (composableMethod.parameterCount > 0 && composableMethod.parameterTypes[0].name == "android.content.Context") {
            arrayOf(context)
        } else {
            emptyArray()
        }

        val safeArgsWithParam =
            when (parameter != NoParameter) {
                true -> arrayOf(parameter, *tileArgs)
                false -> tileArgs
            }

        return when (isInsideClass) {
            false -> kotlinComposableMethod.call(*safeArgsWithParam)
            true -> {
                val instance = composableMethod.declaringClass.getDeclaredConstructor().apply { isAccessible = true }.newInstance()
                kotlinComposableMethod.call(instance, *safeArgsWithParam)
            }
        }
    }

    private fun fillMissingComposeArgs(passedComposeArgs: Array<out Any>?): Array<out Any?> {
        val safeArgs = passedComposeArgs ?: emptyArray()
        val allParams = composableMethod.kotlinFunction!!.parameters
        val defaultParams = allParams.filter { it.isOptional }
        
        if (defaultParams.isEmpty()) return safeArgs

        val defaultParamsAsNull: Array<out Any?> = arrayOfNulls(defaultParams.size)
        val paramsMask = mutableListOf(2.0.pow(allParams.size).toInt() - 1)
        return (defaultParamsAsNull.toMutableList() + safeArgs.toMutableList() + paramsMask).toTypedArray()
    }
}
