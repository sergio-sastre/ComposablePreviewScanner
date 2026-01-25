package sergio.sastre.composable.preview.scanner.android.previewparametersdisplayname

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider

// 'private' to make sure it can also be accessed in tests
private class AndroidStringProviderWithDisplayName : PreviewParameterProvider<String?> {
    override val values: Sequence<String?>
        get() = sequenceOf("Jim", "Jens", null)

    override fun getDisplayName(index: Int): String? {
        return values.elementAt(index)
    }
}

@Composable
fun Example(name: String?) {
    Text(name.toString())
}

@Preview(group = "no-preview-parameter-limit")
@Composable
fun ExamplePreviewNoLimit(
    @PreviewParameter(provider = AndroidStringProviderWithDisplayName::class) name: String?
) {
    Example(name)
}
