package sergio.sastre.composable.preview.scanner.android.api30plus

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun Example(){
    Text("Example 2")
}

@Preview(apiLevel = 31)
@Composable
fun ExamplePreview(){
    Example()
}