package sergio.sastre.composable.preview.scanner.android.previewparameterscount

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import sergio.sastre.composable.preview.scanner.AndroidStringConstructorParameterProvider
import sergio.sastre.composable.preview.scanner.AndroidStringProvider

val stringValues: Sequence<String?> = sequenceOf("Jim", "Jens")
class StringMinusCountParameterProvider(
    override val values: Sequence<String?> = stringValues,
    override val count: Int = -1,
) : PreviewParameterProvider<String?>

@Composable
fun Example(name: String?){
    Text(name.toString())
}

@Preview(group = "count < 0")
@Composable
fun ExamplePreviewProviderInParameterConstructor(
    @PreviewParameter(provider = StringMinusCountParameterProvider ::class) name: String?
){
    Example(name)
}