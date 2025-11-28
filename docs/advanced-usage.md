# Advanced Usage

## Screenshot File Names

ComposablePreviewScanner also provides classes to customize the name of the generated screenshots based on its Preview Info.

These are `AndroidPreviewScreenshotIdBuilder`, `GlancePreviewScreenshotIdBuilder` and `CommonPreviewScreenshotIdBuilder` respectively, and they both share the same API.

By default, these classes do not include the Preview Info in the screenshot file name if it is the same as its default value, but it can be configured to behave differently.

That means, for `@Preview(showBackground = false)`, showBackground would not be included in the screenshot file name since it is the default.

### API Example

```kotlin 
AndroidPreviewScreenshotIdBuilder(preview)
    .ignoreClassName()
    .ignoreMethodName()
    // use this if you have previews in the same file with the same method name but different signature
    .doNotIgnoreMethodParametersType() 
    .ignoreIdFor("heightDp")
    .ignoreIdFor("widthDp")
    .overrideDefaultIdFor(
       previewInfoName = "showBackground",
       applyInfoValue = { info ->
           when (info.showBackground) {
               true -> "WITH_BACKGROUND"
               false -> "WITHOUT_BACKGROUND"
           }
       }
    )
    .build()
```

### Usage in Tests

And then in your test:

```kotlin
@Test
fun snapshot() {
    paparazzi.snapshot(
        name = createScreenshotIdFor(preview)
    ) {
        preview()
    }
}
```

### Example Output

So, for the following Preview:

```kotlin
class MyClass {
    
    @Preview(widthDp = 33, heightDp = 33, fontScale = 1.5f)
    @Composable
    fun MyComposable(){
        // Composable code here
    }
}
```

`createScreenshotIdFor(preview)` will generate the following id: `"MyClass.MyComposable.FONT_1_5f_WITHOUT_BACKGROUND"`

---

## Parsing Preview Device String (Android Previews)

Since 0.4.0, ComposablePreviewScanner also provides `DevicePreviewInfoParser.parse(device: String)` which returns a `Device` object containing all the necessary information to support different devices in your Roborazzi & Paparazzi screenshot tests!

### Supported Device Strings

It can parse ALL possible combinations of "device strings" up to Android Studio Narwhal, namely:

```kotlin
// The over 80 devices supported either by id and/or name, for instance:
@Preview(device = "id:pixel_9_pro")
@Preview(device = "name:Pixel 9 Pro")
@Preview(device = "spec:parent=pixel_9_pro, orientation=landscape, navigation=buttons")

// And custom devices
@Preview(device = "spec:width = 411dp, height = 891dp, orientation = landscape, dpi = 420, isRound = false, chinSize = 0dp, cutout = corner")
@Preview(device = "spec:id=reference_desktop,shape=Normal,width=1920,height=1080,unit=px,dpi=160") // in pixels
@Preview(device = "spec:id=reference_desktop,shape=Normal,width=1920,height=1080,unit=dp,dpi=160") // in dp
```

For further info on how to use them, see [Roborazzi Guide](roborazzi-guide.md) and [Paparazzi Guide](paparazzi-guide.md) respectively.

---

[← Back to Main README](../README.md)
