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

    @get:Input
    abstract val numOfShards: Property<Int>

    @TaskAction
    fun generateTests() {
        val testDir = outputDir.get().asFile
        testDir.mkdirs()

        val packagesExpr = scanPackageTrees.get().joinToString(", ") { "\"$it\"" }
        val includePrivatePreviewsExpr = includePrivatePreviews.get()
        val className = testClassName.get()
        val packageName = testPackageName.get()
        val shards = numOfShards.get()

        val directory = File(testDir, packageName.replace(".", "/"))
        directory.mkdirs()

        if (shards < 1) {
            logger.info("Number of shards must be at least 1")
        } else if (shards == 1) {
            File(directory, "$className.kt").writeText(
                generateTestFileContent(
                    packageName,
                    className,
                    packagesExpr,
                    includePrivatePreviewsExpr,
                    shardIndex = null,
                    numShards = 1,
                    includeHeader = true
                )
            )
            logger.info("Generated Paparazzi test file: ${directory.absolutePath}/$className.kt")
        } else {
            val targetFile = File(directory, "$className.kt")
            val content = buildString {
                // First shard with header and shared code
                append(
                    generateTestFileContent(
                        packageName,
                        "${className}Shard1",
                        packagesExpr,
                        includePrivatePreviewsExpr,
                        shardIndex = 0,
                        numShards = shards,
                        includeHeader = true
                    )
                )
                // Remaining shards: only class declarations
                for (index in 1 until shards) {
                    append("\n\n")
                    append(
                        generateTestFileContent(
                            packageName,
                            "${className}Shard${index + 1}",
                            packagesExpr,
                            includePrivatePreviewsExpr,
                            shardIndex = index,
                            numShards = shards,
                            includeHeader = false
                        )
                    )
                }
            }
            targetFile.writeText(content)
            logger.info("Generated Paparazzi test file: ${directory.absolutePath}/$className.kt")
        }
    }

    private fun generateTestFileContent(
        packageName: String,
        className: String,
        packagesExpr: String,
        includePrivatePreviewsExpr: Boolean,
        shardIndex: Int?,
        numShards: Int,
        includeHeader: Boolean
    ): String {
        val valuesExpr = if (shardIndex == null || numShards <= 1) {
            "cachedPreviews"
        } else {
            "cachedPreviews.filterIndexed { index, _ -> index % $numShards == $shardIndex }"
        }

        val header = """
            package $packageName
            
            import androidx.compose.foundation.background
            import androidx.compose.foundation.layout.Box
            import androidx.compose.runtime.Composable
            import androidx.compose.ui.Modifier
            import androidx.compose.ui.graphics.Color
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
            import kotlin.math.ceil
            import android.content.res.Configuration.UI_MODE_NIGHT_MASK
            import android.content.res.Configuration.UI_MODE_NIGHT_YES
            import androidx.compose.foundation.layout.size
            import androidx.compose.ui.unit.dp
            
            private val paparazziTestName =
                TestName(packageName = "Paparazzi", className = "Preview", methodName = "Test")
            
            private class SnapshotVerifierPaparazzi(
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

            private class HtmlReportWriterPaparazzi: SnapshotHandler {
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

            object PaparazziPreviewRule {
                const val UNDEFINED_API_LEVEL = -1
                const val MAX_API_LEVEL = 36
                
                fun createFor(preview: ComposablePreview<AndroidPreviewInfo>): Paparazzi {
                    val previewInfo = preview.previewInfo
                    val previewApiLevel = when(previewInfo.apiLevel == UNDEFINED_API_LEVEL) {
                        true -> MAX_API_LEVEL
                        false -> previewInfo.apiLevel
                    }
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
                            true -> SnapshotVerifierPaparazzi(0.0)
                            false -> HtmlReportWriterPaparazzi()
                        },
                        // maxPercentDifference can be configured here if needed
                        maxPercentDifference = 0.0
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

            // Expensive scan cached once per file to be shared by all shard classes
            private val cachedPreviews: List<ComposablePreview<AndroidPreviewInfo>> by lazy {
                AndroidComposablePreviewScanner()
                    .scanPackageTrees($packagesExpr)
                    ${if (includePrivatePreviewsExpr) ".includePrivatePreviews()" else ""}
                    .getPreviews()
            }
        """.trimIndent()

        val classSection = """
            @RunWith(Parameterized::class)
            class $className(
                val preview: ComposablePreview<AndroidPreviewInfo>,
            ) {
            
                companion object {
                    @JvmStatic
                    @Parameterized.Parameters
                    fun values(): List<ComposablePreview<AndroidPreviewInfo>> = $valuesExpr
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

        return if (includeHeader) "$header\n\n$classSection" else classSection
    }
}