package sergio.sastre.composable.preview.scanner.samemethodname

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import sergio.sastre.composable.preview.scanner.ListProvider
import sergio.sastre.composable.preview.scanner.StringProvider

@Composable
fun Example(name: String){
    Text(name)
}

@Preview
@Composable
fun ExampleSamePreviewName(
    @PreviewParameter(provider = StringProvider::class) name: String
){
    Example(name)
}

@Preview
@Composable
fun ExampleSamePreviewName(
    @PreviewParameter(provider = ListProvider::class) name: List<Int>
){
    Example(name.joinToString("."))
}

@Preview
@Composable
fun ExampleSamePreviewName(){
    Example("This is another preview")
}