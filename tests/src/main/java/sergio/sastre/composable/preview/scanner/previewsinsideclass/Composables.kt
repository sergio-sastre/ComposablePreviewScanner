package sergio.sastre.composable.preview.scanner.previewsinsideclass

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class Composables {

    @Composable
    fun Example() {
        Text("Example 2")
    }

    @Preview
    @Composable
    fun ExamplePreview() {
        Example()
    }

    @Preview
    @Composable
    fun ExamplePreview(
        hello: String = "arg1",
    ) {
        Text(hello)
    }

}