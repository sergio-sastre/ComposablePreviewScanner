package io.github.sergio.sastre.composable.preview.scanner.paparazzi.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class GenerateComposablePreviewPaparazziTestsTask : DefaultTask() {

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    abstract val scanPackageTrees: ListProperty<String>

    @get:Input
    abstract val includePrivatePreviews: Property<Boolean>

    @get:Input
    abstract val testClassName: Property<String>

    @get:Input
    abstract val testPackageName: Property<String>

    @TaskAction
    fun generateTests() {
        val testDir = outputDir.get().asFile
        testDir.mkdirs()

        val packagesExpr = scanPackageTrees.get().joinToString(", ") { "\"$it\"" }
        val includePrivatePreviewsExpr = includePrivatePreviews.get()
        val className = testClassName.get()
        val packageName = testPackageName.get()

        val directory = File(testDir, packageName.replace(".", "/"))
        directory.mkdirs()

        File(directory, "$className.kt").writeText(
            generateTestFileContent(
                packageName,
                className,
                packagesExpr,
                includePrivatePreviewsExpr
            )
        )

        logger.info("Generated Paparazzi test file: ${directory.absolutePath}/$className.kt")
    }

    private fun generateTestFileContent(
        packageName: String,
        className: String,
        packagesExpr: String,
        includePrivatePreviewsExpr: Boolean
    ): String {
        return """
            package $packageName
            
            import android.content.res.Configuration.UI_MODE_NIGHT_MASK
            import android.content.res.Configuration.UI_MODE_NIGHT_YES
            import androidx.compose.ui.graphics.Color
            import androidx.compose.ui.unit.dp
            import androidx.compose.ui.Modifier
            import androidx.compose.foundation.background
            import androidx.compose.foundation.layout.size
            import androidx.compose.foundation.layout.Box
            import androidx.compose.runtime.Composable
            import app.cash.paparazzi.detectEnvironment
            import app.cash.paparazzi.DeviceConfig
            import app.cash.paparazzi.HtmlReportWriter
            import app.cash.paparazzi.Paparazzi
            import app.cash.paparazzi.Snapshot
            import app.cash.paparazzi.SnapshotHandler
            import app.cash.paparazzi.SnapshotVerifier
            import app.cash.paparazzi.TestName
            import com.android.ide.common.rendering.api.SessionParams
            import com.android.resources.*
            import kotlin.math.ceil
            import org.junit.Rule
            import org.junit.Test
            import org.junit.runner.RunWith
            import org.junit.runners.Parameterized
            import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
            import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
            import sergio.sastre.composable.preview.scanner.android.device.DevicePreviewInfoParser
            import sergio.sastre.composable.preview.scanner.android.device.domain.Device
            import sergio.sastre.composable.preview.scanner.android.device.types.DEFAULT
            import sergio.sastre.composable.preview.scanner.android.screenshotid.AndroidPreviewScreenshotIdBuilder
            import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
            
            class Dimensions(
                val screenWidthInPx: Int,
                val screenHeightInPx: Int
            )

            object ScreenDimensions {
                fun dimensions(
                    parsedDevice: Device,
                    widthDp: Int,
                    heightDp: Int
                ): Dimensions {
                    val conversionFactor = parsedDevice.densityDpi / 160f
                    val previewWidthInPx = ceil(widthDp * conversionFactor).toInt()
                    val previewHeightInPx = ceil(heightDp * conversionFactor).toInt()
                    return Dimensions(
                        screenHeightInPx = when (heightDp > 0) {
                            true -> previewHeightInPx
                            false -> parsedDevice.dimensions.height.toInt()
                        },
                        screenWidthInPx = when (widthDp > 0) {
                            true -> previewWidthInPx
                            false -> parsedDevice.dimensions.width.toInt()
                        }
                    )
                }
            }

            object DeviceConfigBuilder {
                fun build(preview: AndroidPreviewInfo): DeviceConfig {
                    val parsedDevice =
                        DevicePreviewInfoParser.parse(preview.device)?.inPx() ?: return DeviceConfig()

                    val dimensions = ScreenDimensions.dimensions(
                        parsedDevice = parsedDevice,
                        widthDp = preview.widthDp,
                        heightDp = preview.heightDp
                    )

                    return DeviceConfig(
                        screenHeight = dimensions.screenHeightInPx,
                        screenWidth = dimensions.screenWidthInPx,
                        density = Density(parsedDevice.densityDpi),
                        xdpi = parsedDevice.densityDpi, // not 100% precise
                        ydpi = parsedDevice.densityDpi, // not 100% precise
                        size = ScreenSize.valueOf(parsedDevice.screenSize.name),
                        ratio = ScreenRatio.valueOf(parsedDevice.screenRatio.name),
                        screenRound = ScreenRound.valueOf(parsedDevice.shape.name),
                        orientation = ScreenOrientation.valueOf(parsedDevice.orientation.name),
                        locale = preview.locale.ifBlank { "en" },
                        nightMode = when (preview.uiMode and UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES) {
                            true -> NightMode.NIGHT
                            false -> NightMode.NOTNIGHT
                        }
                    )
                }
            }
            
            // In order to have full control over the screenshot file names
            // we need to pass our own SnapshotHandler to the Paparazzi TestRule
            private val paparazziTestName =
                TestName(packageName = "Paparazzi", className = "Preview", methodName = "Test")
            
            private class PreviewSnapshotVerifier(
                maxPercentDifference: Double
            ): SnapshotHandler {
                private val snapshotHandler = SnapshotVerifier(
                    maxPercentDifference = maxPercentDifference
                )
                override fun newFrameHandler(
                    snapshot: Snapshot,
                    frameCount: Int,
                    fps: Int
                ): SnapshotHandler.FrameHandler {
                    val newSnapshot = Snapshot(
                        name = snapshot.name,
                        testName = paparazziTestName,
                        timestamp = snapshot.timestamp,
                        tags = snapshot.tags,
                        file = snapshot.file,
                    )
                    return snapshotHandler.newFrameHandler(
                        snapshot = newSnapshot,
                        frameCount = frameCount,
                        fps = fps
                    )
                }

                override fun close() {
                    snapshotHandler.close()
                }
            }

            private class PreviewHtmlReportWriter: SnapshotHandler {
                private val snapshotHandler = HtmlReportWriter()
                override fun newFrameHandler(
                    snapshot: Snapshot,
                    frameCount: Int,
                    fps: Int
                ): SnapshotHandler.FrameHandler {
                    val newSnapshot = Snapshot(
                        name = snapshot.name,
                        testName = paparazziTestName,
                        timestamp = snapshot.timestamp,
                        tags = snapshot.tags,
                        file = snapshot.file,
                    )
                    return snapshotHandler.newFrameHandler(
                        snapshot = newSnapshot,
                        frameCount = frameCount,
                        fps = fps
                    )
                }

                override fun close() {
                    snapshotHandler.close()
                }
            }

            object PaparazziPreviewRule {
                const val UNDEFINED_API_LEVEL = -1
                const val MAX_API_LEVEL = 36
                
                fun createFor(preview: ComposablePreview<AndroidPreviewInfo>): Paparazzi {
                    val previewInfo = preview.previewInfo
                    val previewApiLevel = when(previewInfo.apiLevel == UNDEFINED_API_LEVEL) {
                        true -> MAX_API_LEVEL
                        false -> previewInfo.apiLevel
                    }
                    val tolerance = 0.0
                    return Paparazzi(
                        environment = detectEnvironment().copy(compileSdkVersion = previewApiLevel),
                        deviceConfig = DeviceConfigBuilder.build(preview.previewInfo),
                        supportsRtl = true,
                        showSystemUi = previewInfo.showSystemUi,
                        renderingMode = when {
                            previewInfo.showSystemUi -> SessionParams.RenderingMode.NORMAL
                            previewInfo.widthDp > 0 && previewInfo.heightDp > 0 -> SessionParams.RenderingMode.FULL_EXPAND
                            else -> SessionParams.RenderingMode.SHRINK
                        },
                        snapshotHandler = when(System.getProperty("paparazzi.test.verify")?.toBoolean() == true) {
                            true -> PreviewSnapshotVerifier(tolerance)
                            false -> PreviewHtmlReportWriter()
                        },
                        // maxPercentDifference can be configured here if needed
                        maxPercentDifference = tolerance
                    )
                }
            }

            /**
             * A composable function that wraps content inside a Box with a specified size
             * This is used to simulate what previews render when showSystemUi is true:
             * - The Preview takes up the entire screen
             * - The Composable still keeps its original size, 
             * - Background color of the Device is white, 
             *   but the @Composable background color is the one defined in the Preview
             */
            @Composable
            fun SystemUiSize(
                widthInDp: Int,
                heightInDp: Int,
                content: @Composable () -> Unit
            ) {
                Box(Modifier
                    .size(
                        width = widthInDp.dp,
                        height = heightInDp.dp
                    )
                    .background(Color.White)
                ) {
                    content()
                }
            }

            @Composable
            fun PreviewBackground(
                showBackground: Boolean,
                backgroundColor: Long,
                content: @Composable () -> Unit
            ) {
                when (showBackground) {
                    false -> content()
                    true -> {
                        val color = when (backgroundColor != 0L) {
                            true -> Color(backgroundColor)
                            false -> Color.White
                        }
                        Box(Modifier.background(color)) {
                            content()
                        }
                    }
                }
            }
            
            @RunWith(Parameterized::class)
            class $className(
                val preview: ComposablePreview<AndroidPreviewInfo>,
            ) {
            
                companion object {
                    private val cachedPreviews: List<ComposablePreview<AndroidPreviewInfo>> by lazy {
                        AndroidComposablePreviewScanner()
                            .scanPackageTrees($packagesExpr)
                            ${if (includePrivatePreviewsExpr) ".includePrivatePreviews()" else ""}
                            .getPreviews()
                    }

                    @JvmStatic
                    @Parameterized.Parameters
                    fun values(): List<ComposablePreview<AndroidPreviewInfo>> = cachedPreviews
                }
            
                @get:Rule
                val paparazzi: Paparazzi = PaparazziPreviewRule.createFor(preview)
            
                @Test
                fun snapshot() {
                    val screenshotId = AndroidPreviewScreenshotIdBuilder(preview)
                    .doNotIgnoreMethodParametersType()
                    .build()
                    // Replace invalid characters for file names with its encoded values
                    .replace("<", "%3C")
                    .replace(">", "%3E")
                    .replace("?", "%3F")
                    
                    paparazzi.snapshot(name = screenshotId) {
                        val previewInfo = preview.previewInfo
                        when (previewInfo.showSystemUi) {
                            false -> PreviewBackground(
                                showBackground = previewInfo.showBackground,
                                backgroundColor = previewInfo.backgroundColor,
                            ) {
                                preview()
                            }
                
                            true -> {
                                val parsedDevice = (DevicePreviewInfoParser.parse(previewInfo.device) ?: DEFAULT).inDp()
                                SystemUiSize(
                                    widthInDp = parsedDevice.dimensions.width.toInt(),
                                    heightInDp = parsedDevice.dimensions.height.toInt()
                                ) {
                                    PreviewBackground(
                                        showBackground = true,
                                        backgroundColor = previewInfo.backgroundColor,
                                    ) {
                                        preview()
                                    }
                                }
                            }
                        }
                    }
                }
            }
            """.trimIndent()
    }
}