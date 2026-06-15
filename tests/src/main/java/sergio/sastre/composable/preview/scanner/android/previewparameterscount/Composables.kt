package sergio.sastre.composable.preview.scanner.android.previewparameterscount

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider

val stringValues: Sequence<String?> = sequenceOf("Jim", "Jens", null)
class StringMinusCountParameterProvider(
    override val values: Sequence<String?> = stringValues,
    override val count: Int = -1,
) : PreviewParameterProvider<String?>

class StringCountGreaterThanValuesSizeParameterProvider(
    override val values: Sequence<String?> = stringValues,
    override val count: Int = stringValues.count() + 1,
) : PreviewParameterProvider<String?>

class StringCountGreaterThanLimitParameterProvider(
    override val values: Sequence<String?> = stringValues,
    override val count: Int = 2,
) : PreviewParameterProvider<String?>

class StringCountLessThanLimitParameterProvider(
    override val values: Sequence<String?> = stringValues,
    override val count: Int = 1,
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

@Preview(group = "count > values size")
@Composable
fun ExamplePreviewCountGreaterThanValuesSizeProviderInParameterConstructor(
    @PreviewParameter(provider = StringCountGreaterThanValuesSizeParameterProvider ::class) name: String?
){
    Example(name)
}

@Preview(group = "count > limit and limit = 1")
@Composable
fun ExamplePreviewCountGreaterThanLimitProviderInParameterConstructor(
    @PreviewParameter(provider = StringCountGreaterThanLimitParameterProvider ::class, limit = 1) name: String?
){
    Example(name)
}

@Preview(group = "count < limit and limit = 2")
@Composable
fun ExamplePreviewCountLessThanLimitProviderInParameterConstructor(
    @PreviewParameter(provider = StringCountLessThanLimitParameterProvider ::class, limit = 2) name: String?
){
    Example(name)
}