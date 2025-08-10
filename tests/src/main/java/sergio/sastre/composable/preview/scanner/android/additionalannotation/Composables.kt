package sergio.sastre.composable.preview.scanner.android.additionalannotation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun Example(){
    Text("Example")
}

@Preview(
    widthDp = 200,
    heightDp = 200
)
@Composable
fun ExamplePreview(){
    Example()
}