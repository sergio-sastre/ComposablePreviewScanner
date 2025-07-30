package defaultparams.before.previewparameters.throwserror.common

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import sergio.sastre.composable.preview.scanner.CommonStringProvider

@Preview
@Composable
fun ExampleDefaultPlusPreviewParameters(
    hello: String? = "arg1",
    @PreviewParameter(CommonStringProvider::class) name: String,
    hello1: String = "arg2",
    hello2: String = "arg3",
) {
    Text("$name $hello $hello1 $hello2")
}