package sergio.sastre.composable.preview.scanner.android.privatepreviews

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import sergio.sastre.composable.preview.scanner.AndroidStringProvider

@Composable
fun Example(name: String?) {
    Text(name.toString())
}

@Preview
@Composable
private fun ExamplePreview() {
    Example("Example")
}

@Preview
@Composable
private fun ExampleParameterizedPreview(
    @PreviewParameter(provider = AndroidStringProvider::class) name: String?
) {
    Example(name)
}
