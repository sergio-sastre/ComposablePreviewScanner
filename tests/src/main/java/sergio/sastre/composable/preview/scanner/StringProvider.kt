package sergio.sastre.composable.preview.scanner

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class StringProvider : PreviewParameterProvider<String> {
    override val values: Sequence<String>
        get() = sequenceOf("Jim", "Jens")
}