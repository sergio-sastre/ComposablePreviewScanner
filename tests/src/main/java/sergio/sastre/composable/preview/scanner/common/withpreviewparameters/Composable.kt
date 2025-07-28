package sergio.sastre.composable.preview.scanner.common.withpreviewparameters

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import sergio.sastre.composable.preview.scanner.CommonStringProvider

@Preview
@Composable
fun PreviewParameterWithoutLimitPreview(
    @PreviewParameter(provider = CommonStringProvider::class) name: String
){
    Text(name)
}

@Preview
@Composable
fun PreviewParameterWithLimit1Preview(
    @PreviewParameter(provider = CommonStringProvider::class, limit = 1) name: String
){
    Text(name)
}