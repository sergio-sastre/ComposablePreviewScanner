package sergio.sastre.composable.preview.scanner.common.previewdefault

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import sergio.sastre.composable.preview.scanner.CommonStringProvider

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ExampleDefault(
    hello: String? = "arg1",
    hello1: String = "arg2",
    name: @Composable () -> Unit = { Text("Defaults = ")},
    hello2: String = "arg3"
) {
    TopAppBar(
        title = {
            Column {
                name()
                Text("$hello $hello1 $hello2")
            }
        }
    )
}

@Preview
@Composable
fun ExampleDefault(
    arg0: Map<String, String> = mapOf("arg0" to "map"),
    arg1: String? = "arg1",
    arg2: String = "arg2",
    arg3: String = "arg3"
) {
    Text("${arg0.keys.first()} $arg1 $arg2 $arg3")
}

// Works as far as @PreviewParameter is the first in the list
@Preview
@Composable
fun ExampleDefaultPlusPreviewParameters(
    @PreviewParameter(CommonStringProvider::class) name: String?,
    hello: String? = "arg1",
    hello1: String = "arg2",
    hello2: String = "arg3",
) {
    Text("$name $hello $hello1 $hello2")
}