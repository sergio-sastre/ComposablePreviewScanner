package sergio.sastre.composable.preview.scanner.android.included.nested

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import sergio.sastre.composable.preview.scanner.android.included.IncludeScreenshot

@Composable
fun Example(){
    Text("Example")
}

@Composable
fun Example2(){
    Text("Example 2")
}

@Preview
@Composable
@IncludeScreenshot
fun ExamplePreview(){
    Example()
}

@Preview
@Composable
fun Example2Preview(){
    Example2()
}