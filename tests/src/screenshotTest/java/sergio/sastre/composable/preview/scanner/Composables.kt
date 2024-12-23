package sergio.sastre.composable.preview.scanner

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun Example() {
    Text("Example")
}

class ComposePreviewScreenshotTests {

    @Preview
    @Composable
    fun ExamplePreview() {
        Example()
    }
}