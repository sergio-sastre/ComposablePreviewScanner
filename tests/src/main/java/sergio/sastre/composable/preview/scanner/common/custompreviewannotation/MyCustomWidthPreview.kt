package sergio.sastre.composable.preview.scanner.common.custompreviewannotation

import org.jetbrains.compose.ui.tooling.preview.Preview

@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.FUNCTION
)
@Preview(widthDp = 100, heightDp = 300)
@Preview(widthDp = 300)
@Preview(widthDp = 300, heightDp = 300)
annotation class MyCustomWidthPreview
