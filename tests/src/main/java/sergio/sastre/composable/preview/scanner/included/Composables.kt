package sergio.sastre.composable.preview.scanner.included

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

annotation class IncludeScreenshot

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