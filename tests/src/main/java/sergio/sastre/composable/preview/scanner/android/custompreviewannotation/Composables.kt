package sergio.sastre.composable.preview.scanner.android.custompreviewannotation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun Example(){
    Text("Example 2")
}

@MyCustomDarkModePreview
@Composable
fun ExamplePreview(){
    Example()
}