package sergio.sastre.composable.preview.scanner.included

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

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
fun ExamplePreview(){
    Example()
}


@Preview
@Composable
fun Example2Preview(){
    Example2()
}