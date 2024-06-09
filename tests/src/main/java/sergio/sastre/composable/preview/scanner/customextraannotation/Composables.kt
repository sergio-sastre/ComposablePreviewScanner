package sergio.sastre.composable.preview.scanner.customextraannotation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class Foo {
    val name = "foo"
}

annotation class MyAnnotation(
    val device: Device
)

enum class Device(val height: Int, val width: Int) {
    PIXEL_XL(height = 800, width = 200),
    PIXEL_5(height = 600, width = 150)
}

annotation class ScreenshotTestConfig(
    val device: Device,
    val locale: String,
    val array: Array<String>,
    // val myAnnotation: MyAnnotation
    // val clazz: KClass<out Any>,
)

@Composable
fun Example() {
    Text("Example 2")
}

@ScreenshotTestConfig(
    device = Device.PIXEL_XL,
    locale = "ar",
    array = ["1", "2"],
    //myAnnotation = MyAnnotation(Device.PIXEL_XL)
    //clazz = Foo::class
)
@Preview
@Composable
fun ExamplePreview() {
    Example()
}