/**
 * This is a replica of the Jetbrains Preview used for @Composables in common
 * https://github.com/JetBrains/compose-multiplatform/blob/master/components/ui-tooling-preview/library/src/commonMain/kotlin/org/jetbrains/compose/ui/tooling/preview/Preview.kt#L26C1-L26C31
 */
package org.jetbrains.compose.ui.tooling.preview

/**
 * [Preview] can be applied to either of the following:
 * - @[Composable] methods with no parameters to show them in the IDE preview.
 * - Annotation classes, that could then be used to annotate @[Composable] methods or other
 * annotation classes, which will then be considered as indirectly annotated with that [Preview].
 */
@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.FUNCTION
)
@Repeatable
annotation class Preview
