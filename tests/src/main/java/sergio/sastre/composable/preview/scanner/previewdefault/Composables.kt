package sergio.sastre.composable.preview.scanner.previewdefault

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

// TODO - default + Preview Parameters
// TODO - These combinations inside a class
// TODO - pass a Composable, which is more sofisticated
// TODO - adjust screenshot Id when using default params (Composer shows up)
@Preview
@Composable
fun Example2(
    name: Map<String,String> = mapOf("arg0" to "map"),
    hello: String? = "arg1",
    hello1: String = "arg2",
    hello2: String = "arg3"
){
    Text(name.keys.first() + " " + hello + " " + hello1 + " " +hello2)
}