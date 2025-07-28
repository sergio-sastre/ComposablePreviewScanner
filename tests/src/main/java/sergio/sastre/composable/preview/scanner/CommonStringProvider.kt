package sergio.sastre.composable.preview.scanner

import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider

class CommonStringProvider : PreviewParameterProvider<String> {
    override val values: Sequence<String>
        get() = sequenceOf("Jim", "Jens")
}