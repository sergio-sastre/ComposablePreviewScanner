package sergio.sastre.composable.preview.scanner

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

import kotlinx.collections.immutable.persistentListOf

val immutableList = persistentListOf("Example")

@Composable
fun Example() {
    Text(immutableList.first())
}

@Preview
@Composable
fun AndroidTestExamplePreview() {
    Example()
}