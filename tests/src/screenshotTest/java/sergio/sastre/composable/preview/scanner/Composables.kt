package sergio.sastre.composable.preview.scanner

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewParameter
import sergio.sastre.composable.preview.custompreviews.CrossModuleCustomPreview

class ComposePreviewScreenshotTests {
    @Composable
    fun Example(text: String) {
        Text(text)
    }

    // Total: 14 Previews
    @Preview                 // 1 Preview
    @MyCustomDarkModePreview // 2 Previews
    @PreviewDynamicColors    // 4 previews
    @Composable
    fun ScreenshotTestExampleMultiplePreview(
        @PreviewParameter(provider = StringProvider::class) name: String
    ) {
        Example(name)
    }

    @CrossModuleCustomPreview // 3 Previews
    @Composable
    fun ScreenshotTestExampleMultiplePreview(){
        Example("Example")
    }
}