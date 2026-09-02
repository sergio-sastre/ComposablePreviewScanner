package sergio.sastre.composable.preview.scanner.core.scanner

import io.github.classgraph.ClassGraph
import nonapi.io.github.classgraph.utils.VersionFinder
import sergio.sastre.composable.preview.scanner.core.scanner.config.ClassGraphSourceScanner
import sergio.sastre.composable.preview.scanner.core.scanner.config.ClassGraphSourceScannerWithResult
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.Classpath
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.ClasspathPreviewsFinder
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.ClasspathPreviewsFinderWithResult
import sergio.sastre.composable.preview.scanner.core.scanner.config.SourceScannerWithResult
import sergio.sastre.composable.preview.scanner.core.scanner.config.SourceScanner
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.validator.ClasspathValidator
import sergio.sastre.composable.preview.scanner.core.scanner.exceptions.ScanSourceNotSupported
import sergio.sastre.composable.preview.scanner.core.scanresult.RequiresLargeHeap
import sergio.sastre.composable.preview.scanner.core.annotations.RequiresShowStandardStreams
import sergio.sastre.composable.preview.scanner.core.scanner.exceptions.ScanningLogsNotSupported
import sergio.sastre.composable.preview.scanner.core.scanresult.filter.ScanResultFilter
import sergio.sastre.composable.preview.scanner.core.scanresult.filter.ScanResultFilterWithResult
import sergio.sastre.composable.preview.scanner.core.utils.isRunningOnJvm
import java.io.File
import java.io.InputStream

/**
 * Core Component to scan for Previews
 * @param defaultPackageTreesOfCrossModuleCustomPreviews package where external previews
 * (i.e. previews defined in another dependency or module) can be found like those in "androidx.compose.ui.tooling.preview"
 */
abstract class ComposablePreviewScannerWithResult<T, R>(
    private val findComposableWithPreviewsInClass: ClasspathPreviewsFinderWithResult<T, R>,
    private val defaultPackageTreesOfCrossModuleCustomPreviews: List<String> = emptyList()
) : SourceScannerWithResult<T, R> {

    protected var updatedClassGraph =
        ClassGraph()
            .ignoreMethodVisibility()
            .enableClassInfo()
            .enableMethodInfo()
            .enableAnnotationInfo()
            .apply {
                // Otherwise Classgraph throws exception
                if (VersionFinder.JAVA_MAJOR_VERSION < 24){
                   enableMemoryMapping()
                }
            }


    protected var classpath: Classpath? = null
    protected var isLoggingEnabled: Boolean = false

    protected open val classGraphSourceScanner: ClassGraphSourceScannerWithResult<T, R>
        get() = ClassGraphSourceScannerWithResult(
            classGraph = updatedClassGraph,
            classpath = classpath,
            findComposableWithPreviewsInClass = findComposableWithPreviewsInClass,
            isLoggingEnabled = isLoggingEnabled
        )

    /**
     * Enables logging of the scanning process, like the time it takes to scan and find @Previews
     * and the amount of previews found among others
     *
     * Warning: Not supported when running Instrumentation tests
     */
    @RequiresShowStandardStreams
    open fun enableScanningLogs(): ComposablePreviewScannerWithResult<T, R> = apply {
        if(!isRunningOnJvm()) throw ScanningLogsNotSupported()
        isLoggingEnabled = true
    }

    /**
     * Prepares the scanner to find previews scanned from a Source Set like 'screenshotTest', 'androidTest', 'main' or a custom one via the given sourceSetClasspath
     * It uses compiled classes of that source set.
     * Check SourceSetClasspath to find their locations under the /build folder of the corresponding module.
     *
     * Make sure those compiled classes exist and are up to date before scanning them.
     * For that you can execute ./gradlew :<module>:compile<variant><sourceSet>Kotlin,
     * for instance: ./gradlew :mymodule:compileReleaseScreenshotTestKotlin
     *
     * @param sourceSetClasspath the classpath pointing to the package where compiled classes of a Source Set are located
     * @param packageTreesOfCrossModuleCustomPreviews package where external previews (i.e. previews defined in another dependency or module)
     * different can be found. Previews under "androidx.compose.ui.tooling.preview", like @PreviewLightDark, do not need to be added here.
     * In most cases, you can leave it empty unless you see some custom-annotated-Previews missing, whose annotation packages should be added here.
     */
    @JvmOverloads
    open fun setTargetSourceSetWithResult(
        sourceSetClasspath: Classpath,
        packageTreesOfCrossModuleCustomPreviews: List<String> = emptyList()
    ): ClassGraphSourceScannerWithResult<T, R> {
        ClasspathValidator(sourceSetClasspath).validate()

        val absolutePath =
            File(sourceSetClasspath.rootDir, sourceSetClasspath.packagePath).absolutePath
        findComposableWithPreviewsInClass
            .applyOverridenClasspath(sourceSetClasspath)
            .applyCrossModuleCustomPreviewPackageTrees(
                packageTreesOfCrossModuleCustomPreviews + defaultPackageTreesOfCrossModuleCustomPreviews
            )

        updatedClassGraph.overrideClasspath(absolutePath)
        classpath = sourceSetClasspath

        return classGraphSourceScanner
    }
}

abstract class ComposablePreviewScanner<T>(
    private val findComposableWithPreviewsInClass: ClasspathPreviewsFinder<T>,
    defaultPackageTreesOfCrossModuleCustomPreviews: List<String> = emptyList()
) : ComposablePreviewScannerWithResult<T, Unit>(
    findComposableWithPreviewsInClass = findComposableWithPreviewsInClass,
    defaultPackageTreesOfCrossModuleCustomPreviews = defaultPackageTreesOfCrossModuleCustomPreviews
), SourceScanner<T> {

    override val classGraphSourceScanner: ClassGraphSourceScanner<T>
        get() = ClassGraphSourceScanner(
            classGraph = updatedClassGraph,
            classpath = classpath,
            findComposableWithPreviewsInClass = findComposableWithPreviewsInClass,
            isLoggingEnabled = isLoggingEnabled
        )

    @RequiresShowStandardStreams
    final override fun enableScanningLogs(): ComposablePreviewScanner<T> = apply {
        super.enableScanningLogs()
    }

    @JvmOverloads
    final fun setTargetSourceSet(
        sourceSetClasspath: Classpath,
        packageTreesOfCrossModuleCustomPreviews: List<String> = emptyList()
    ): ClassGraphSourceScanner<T> =
        setTargetSourceSetWithResult(sourceSetClasspath, packageTreesOfCrossModuleCustomPreviews) as ClassGraphSourceScanner<T>

    @RequiresLargeHeap
    override fun scanAllPackages(): ScanResultFilter<T> =
        classGraphSourceScanner.scanAllPackages()

    override fun scanPackageTrees(vararg packageTrees: String): ScanResultFilter<T> =
        classGraphSourceScanner.scanPackageTrees(*packageTrees)

    override fun scanPackageTrees(
        include: List<String>,
        exclude: List<String>
    ): ScanResultFilter<T> =
        classGraphSourceScanner.scanPackageTrees(include, exclude)

    override fun scanFile(jsonFile: File): ScanResultFilter<T> =
        classGraphSourceScanner.scanFile(jsonFile)

    override fun scanFile(
        targetInputStream: InputStream,
        customPreviewsInfoInputStream: InputStream
    ): ScanResultFilter<T> =
        classGraphSourceScanner.scanFile(targetInputStream, customPreviewsInfoInputStream)
}
