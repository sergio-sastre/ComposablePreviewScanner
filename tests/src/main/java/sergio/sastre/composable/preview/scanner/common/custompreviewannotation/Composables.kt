package sergio.sastre.composable.preview.scanner.common.custompreviewannotation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun Example() {
    Text("Example 2")
}

@MyCustomBackgroundPreview
@MyCustomSizePreview
@Composable
fun ExamplePreview() {
    Example()
}