package sergio.sastre.composable.preview.scanner.android.previewparametersdisplayname

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider

// 'private' to make sure it can also be accessed in tests
private class AndroidStringProviderWithDisplayName : PreviewParameterProvider<String?> {

    // Add method with same name but different signature to ensure
    // the other method is picked when using reflection
    fun getDisplayName(index: Int?): String? = throw RuntimeException("WRONG")

    override val values: Sequence<String?>
        get() = sequenceOf("Jim", "Jens", null)

    override fun getDisplayName(index: Int): String? {
        return values.elementAt(index)
    }
}

// Overriding getDisplayName with a non-nullable String return (covariant narrowing)
private class AndroidStringProviderWithNonNullableDisplayName : PreviewParameterProvider<String> {

    override val values: Sequence<String>
        get() = sequenceOf("Jim", "Jens")

    override fun getDisplayName(index: Int): String {
        return values.elementAt(index)
    }
}

// getDisplayName is only declared as an interface default, so implementing providers inherit it
private interface ProviderWithDisplayNameDefault : PreviewParameterProvider<String> {
    override fun getDisplayName(index: Int): String? {
        return values.elementAt(index)
    }
}

private class AndroidStringProviderWithInterfaceInheritedDisplayName : ProviderWithDisplayNameDefault {
    override val values: Sequence<String>
        get() = sequenceOf("Jim", "Jens")
}

// getDisplayName is only declared on an abstract superclass, so subclasses inherit it
private abstract class BaseProviderWithDisplayName : PreviewParameterProvider<String> {
    override fun getDisplayName(index: Int): String? {
        return values.elementAt(index)
    }
}

private class AndroidStringProviderWithSuperclassInheritedDisplayName : BaseProviderWithDisplayName() {
    override val values: Sequence<String>
        get() = sequenceOf("Jim", "Jens")
}

@Composable
fun Example(name: String?) {
    Text(name.toString())
}

@Preview(group = "no-preview-parameter-limit")
@Composable
fun ExamplePreviewNoLimit(
    @PreviewParameter(provider = AndroidStringProviderWithDisplayName::class) name: String?
) {
    Example(name)
}

@Preview(group = "non-nullable-display-name")
@Composable
fun ExamplePreviewNonNullableDisplayName(
    @PreviewParameter(provider = AndroidStringProviderWithNonNullableDisplayName::class) name: String
) {
    Example(name)
}

@Preview(group = "interface-inherited-display-name")
@Composable
fun ExamplePreviewInterfaceInheritedDisplayName(
    @PreviewParameter(provider = AndroidStringProviderWithInterfaceInheritedDisplayName::class) name: String
) {
    Example(name)
}

@Preview(group = "superclass-inherited-display-name")
@Composable
fun ExamplePreviewSuperclassInheritedDisplayName(
    @PreviewParameter(provider = AndroidStringProviderWithSuperclassInheritedDisplayName::class) name: String
) {
    Example(name)
}
