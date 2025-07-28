package defaultparams.before.previewparameters.throwserror.android

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import sergio.sastre.composable.preview.scanner.AndroidStringProvider

@Preview
@Composable
fun ExampleDefaultPlusPreviewParameters(
    hello: String? = "arg1",
    @PreviewParameter(AndroidStringProvider::class) name: String,
    hello1: String = "arg2",
    hello2: String = "arg3",
) {
    Text("$name $hello $hello1 $hello2")
}