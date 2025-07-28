package sergio.sastre.composable.preview.scanner.android.excluded

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

annotation class ExcludeScreenshot

@Composable
fun Example(){
    Text("Example")
}

@ExcludeScreenshot
@Preview
@Composable
fun ExamplePreview(){
    Example()
}