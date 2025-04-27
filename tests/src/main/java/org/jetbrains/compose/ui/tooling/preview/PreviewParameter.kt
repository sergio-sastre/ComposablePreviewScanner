package org.jetbrains.compose.ui.tooling.preview

import kotlin.reflect.KClass

/**
 * Interface to be implemented by any provider of values that you want to be injected as @[Preview]
 * parameters. This allows providing sample information for previews.
 */
interface PreviewParameterProvider<T> {
    /**
     * [Sequence] of values of type [T] to be passed as @[Preview] parameter.
     */
    val values: Sequence<T>

    /**
     * Returns the number of elements in the [values] [Sequence].
     */
    val count get() = values.count()
}

/**
 * [PreviewParameter] can be applied to any parameter of a @[Preview].
 *
 * @param provider A [PreviewParameterProvider] class to use to inject values to the annotated
 * parameter.
 * @param limit Max number of values from [provider] to inject to this parameter.
 */
annotation class PreviewParameter(
    val provider: KClass<out PreviewParameterProvider<*>>,
    val limit: Int = Int.MAX_VALUE
)