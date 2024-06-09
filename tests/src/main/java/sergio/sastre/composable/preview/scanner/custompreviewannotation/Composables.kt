package sergio.sastre.composable.preview.scanner.custompreviewannotation

import android.content.res.Configuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview


@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class MyCustomPreview

@Composable
fun Example(){
    Text("Example 2")
}

@MyCustomPreview
@Composable
fun ExamplePreview(){
    Example()
}