package sergio.sastre.composable.preview.scanner.android.previewparameters

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

class DpValueClassProvider : PreviewParameterProvider<Dp?> {
    override val values: Sequence<Dp?> = sequenceOf(32.dp, 64.dp, null)
}

@Preview(name = "valueClassDp")
@Composable
fun ValueClassPreviewParameterPreview(
    @PreviewParameter(provider = DpValueClassProvider::class) size: Dp?,
) {
    Text(size.toString())
}

class ListDpValueClassProvider : PreviewParameterProvider<List<Dp>> {
    override val values: Sequence<List<Dp>> = sequenceOf(listOf(32.dp, 64.dp))
}

@Preview(name = "valueClassListDp")
@Composable
fun ListValueClassPreviewParameterPreview(
    @PreviewParameter(provider = ListDpValueClassProvider::class) sizes: List<Dp>,
) {
    Text(sizes.toString())
}

class ArrayDpValueClassProvider : PreviewParameterProvider<Array<Dp>> {
    override val values: Sequence<Array<Dp>> = sequenceOf(arrayOf(32.dp, 64.dp))
}

@Preview(name = "valueClassArrayDp")
@Composable
fun ArrayValueClassPreviewParameterPreview(
    @PreviewParameter(provider = ArrayDpValueClassProvider::class) sizes: Array<Dp>,
) {
    Text(sizes.joinToString())
}

data class MyClass(val name: String)

class MyClassProvider : PreviewParameterProvider<MyClass> {
    override val values: Sequence<MyClass> = sequenceOf(MyClass("test"))
}

@Preview(name = "myClass")
@Composable
fun MyClassPreviewParameterPreview(
    @PreviewParameter(provider = MyClassProvider::class) myClass: MyClass,
) {
    Text(myClass.name)
}

class WildcardListProvider : PreviewParameterProvider<List<*>> {
    override val values: Sequence<List<*>> = sequenceOf(listOf(1, "2"))
}

@Preview(name = "wildcardList")
@Composable
fun `WildcardList-with-hypen-backsticks`(
    @PreviewParameter(provider = WildcardListProvider::class) list: List<*>,
) {
    Text(list.toString())
}
