package sergio.sastre.composable.preview.scanner.wear

import androidx.wear.tiles.tooling.preview.TilePreviewData
import io.github.classgraph.ClassGraph
import sergio.sastre.composable.preview.scanner.wear.preview.WearComposablePreview
import sergio.sastre.composable.preview.scanner.wear.preview.WearComposablePreviewWithPreviewParameterMapper
import sergio.sastre.composable.preview.scanner.wear.WearScannerUtils.loadClassAndGetMethod
import sergio.sastre.composable.preview.scanner.wear.WearScannerUtils.valueForKey

/**
 * Scans the target package trees for @androidx.wear.tiles.tooling.preview.Preview and returns their Composable,
 * which can be invoked to get TilePreviewData.
 */
class WearTileComposablePreviewScanner {

    private val annotationName = "androidx.wear.tiles.tooling.preview.Preview"

    fun scanPackageTrees(vararg packageTrees: String): List<WearComposablePreview<WearTilePreviewInfo, TilePreviewData>> {
        val scanResult = ClassGraph()
            .enableClassInfo()
            .enableMethodInfo()
            .enableAnnotationInfo()
            .acceptPackages(*packageTrees)
            .scan()

        return scanResult.use { result ->
            result.allClasses
                .filter { classInfo ->
                    packageTrees.any { classInfo.name.startsWith(it) }
                }
                .flatMap { classInfo ->
                    classInfo.declaredMethodInfo
                        .filter { it.hasAnnotation(annotationName) }
                        .flatMap { methodInfo ->
                            val method = methodInfo.loadClassAndGetMethod()
                            val annotations = methodInfo.getAnnotationInfoRepeatable(annotationName)
                            annotations.flatMap { annotation ->
                                val previewInfo = WearTilePreviewInfo(
                                    name = annotation.parameterValues.valueForKey("name") as? String ?: "",
                                    group = annotation.parameterValues.valueForKey("group") as? String ?: "",
                                    device = annotation.parameterValues.valueForKey("device") as? String ?: "",
                                    fontScale = annotation.parameterValues.valueForKey("fontScale") as? Float ?: 1f,
                                    locale = annotation.parameterValues.valueForKey("locale") as? String ?: ""
                                )
                                
                                val mapper = WearComposablePreviewWithPreviewParameterMapper<WearTilePreviewInfo, TilePreviewData>(
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
