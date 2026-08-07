package sergio.sastre.composable.preview.scanner.android.previewwrapper

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composer
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.tooling.preview.PreviewWrapperProvider

private class PrivateScaffoldWrapper : PreviewWrapperProvider {

    @Composable
    override fun Wrap(content: @Composable () -> Unit) {
        Scaffold { padding ->
            Box(
                Modifier
                    .padding(padding)
                    .background(Color.Blue)
            ) { content() }
        }
    }
}

class PublicScaffoldWrapper: PreviewWrapperProvider {
    @Composable
    fun Wrap(content: @Composable () -> Unit, misleadingArg: Int){
        // no-op Use this to ensure we pick the other method via reflection
        throw RuntimeException("WRONG - Too many params")
    }

    @Composable
    fun Wrap(content: () -> Unit){
        // no-op Use this to ensure we pick the other method via reflection
        throw RuntimeException("WRONG - Non-Composable Unit param")
    }

    @Composable
    fun Wrap(content: (Composer, Int) -> Unit, composer: Composer, changed: Int) {
        // Bytecode: (Function2, Composer, int)
        // This is IDENTICAL to the overridden Wrap in terms of reflection types!
        throw RuntimeException("WRONG - This is not @Composable and looks identical reflection-wise")
    }

    @Composable
    override fun Wrap(content: @Composable () -> Unit) {
        val color = when(isSystemInDarkTheme()){
            true -> Color.Magenta
            false -> Color.Red
        }
        Scaffold { padding ->
            Box(
                Modifier
                    .padding(padding)
                    .background(color)
            ) { content() }
        }
    }
}

@Preview
@Composable
@PreviewWrapper(wrapper = PrivateScaffoldWrapper::class)
fun PrivatePreviewWrapper() {
    Text("PrivatePreviewWrapper")
}

@PreviewLightDark
@Composable
@PreviewWrapper(wrapper = PublicScaffoldWrapper::class)
fun DefaultParamPublicPreviewWrapper(arg1: String = "DefaultParamPublicPreviewWrapper") {
    Text(arg1)
}