package sergio.sastre.composable.preview.scanner.android.androidvals

import android.graphics.Path
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

// This was added to reproduce #27 when running Paparazzi tests
// https://github.com/sergio-sastre/ComposablePreviewScanner/issues/27

val androidVal = Path()
private val privateAndroidVal = Path()

@Composable
fun Example(){
    androidVal
    Text("Example")
}

@Composable
fun Example2(){
    privateAndroidVal
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