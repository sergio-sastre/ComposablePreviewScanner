package sergio.sastre.composable.preview.scanner.wear

import io.github.classgraph.ClassGraph
import sergio.sastre.composable.preview.scanner.wear.preview.WearComposablePreview
import sergio.sastre.composable.preview.scanner.wear.preview.WearComposablePreviewWithPreviewParameterMapper
import sergio.sastre.composable.preview.scanner.wear.WearScannerUtils.loadClassAndGetMethod
import sergio.sastre.composable.preview.scanner.wear.WearScannerUtils.valueForKey

/**
 * Scans the target package trees for @androidx.compose.ui.tooling.preview.Preview and returns their Composable,
 * including those from Wear-specific multi-preview annotations.
 */
class WearComposablePreviewScanner {

    private val annotationName = "androidx.compose.ui.tooling.preview.Preview"
    private val wearPreviewPackages = listOf(
        "androidx.wear.compose.ui.tooling.preview",
        "androidx.compose.ui.tooling.preview"
    )

    fun scanPackageTrees(vararg packageTrees: String): List<WearComposablePreview<WearPreviewInfo, Unit>> {
        val scanResult = ClassGraph()
            .enableClassInfo()
            .enableMethodInfo()
            .enableAnnotationInfo()
            .acceptPackages(*packageTrees)
            .acceptPackages(*wearPreviewPackages.toTypedArray())
            .scan()

        return scanResult.use { result ->
            result.allClasses
                .filter { classInfo -> 
                    packageTrees.any { classInfo.name.startsWith(it) }
                }
                .flatMap { classInfo ->
                    classInfo.declaredMethodInfo
                        .filter { methodInfo ->
                            methodInfo.hasAnnotation(annotationName) || 
                            methodInfo.annotationInfo.any { it.classInfo?.hasAnnotation(annotationName) == true }
                        }
                        .flatMap { methodInfo ->
                            val method = methodInfo.loadClassAndGetMethod()
                            val annotations = methodInfo.getAnnotationInfoRepeatable(annotationName)
                            
                            annotations.flatMap { annotation ->
                                val previewInfo = WearPreviewInfo(
                                    name = annotation.parameterValues.valueForKey("name") as? String ?: "",
                                    group = annotation.parameterValues.valueForKey("group") as? String ?: "",
                                    apiLevel = annotation.parameterValues.valueForKey("apiLevel") as? Int ?: -1,
                                    widthDp = annotation.parameterValues.valueForKey("widthDp") as? Int ?: -1,
                                    heightDp = annotation.parameterValues.valueForKey("heightDp") as? Int ?: -1,
                                    locale = annotation.parameterValues.valueForKey("locale") as? String ?: "",
                                    fontScale = annotation.parameterValues.valueForKey("fontScale") as? Float ?: 1f,
                                    showBackground = annotation.parameterValues.valueForKey("showBackground") as? Boolean ?: false,
                                    showSystemUi = annotation.parameterValues.valueForKey("showSystemUi") as? Boolean ?: false,
                                    backgroundColor = annotation.parameterValues.valueForKey("backgroundColor") as? Long ?: 0L,
                                    device = annotation.parameterValues.valueForKey("device") as? String ?: "",
                                    uiMode = annotation.parameterValues.valueForKey("uiMode") as? Int ?: 0,
                                    wallpaper = annotation.parameterValues.valueForKey("wallpaper") as? Int ?: -1,
                                )

                                val mapper = WearComposablePreviewWithPreviewParameterMapper<WearPreviewInfo, Unit>(
                                    previewParameterClassName = "androidx.compose.ui.tooling.preview.PreviewParameter",
                                    previewMethod = method,
                                    previewInfo = previewInfo,
                                    annotationsInfo = methodInfo.annotationInfo
                                )

                                mapper.mapToWearComposablePreviews()
                            }
                        }
                }
        }
    }
}
