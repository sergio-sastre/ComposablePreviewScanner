package sergio.sastre.composable.preview.scanner.common.custompreviewannotation

import org.jetbrains.compose.ui.tooling.preview.Preview

@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.FUNCTION
)
@Preview(showBackground = true)
@Preview(showBackground = true, backgroundColor = 0xFF000080)
@Preview(backgroundColor = 0xFF000080)
annotation class MyCustomBackgroundPreview
