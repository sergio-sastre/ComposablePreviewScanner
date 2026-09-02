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
    open fun setTargetSourceSet(
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

    /**
     * Scan previews in all packages, including those of external dependencies.
     *
     * Warning: Not supported when running Instrumentation tests
     */
    @RequiresLargeHeap
    override fun scanAllPackages(): ScanResultFilterWithResult<T, R> {
        if (!isRunningOnJvm()) throw ScanSourceNotSupported()
        return classGraphSourceScanner.scanAllPackages()
    }

    /**
     * Scan previews in the given packageTrees
     *
     * @param packageTrees where we want to scan previews
     *
     * Warning: Not supported when running Instrumentation tests
     */
    override fun scanPackageTrees(vararg packageTrees: String): ScanResultFilterWithResult<T, R> {
        if (!isRunningOnJvm()) throw ScanSourceNotSupported()
        return classGraphSourceScanner.scanPackageTrees(*packageTrees)
    }

    /**
     * Scan previews in the given ‘include‘ packageTrees, excluding the 'exclude' packageTrees
     *
     * @param include where we want to scan previews
     * @param exclude where we do not want to scan previews, even though they were inside the included packageTrees
     *
     * Warning: Not supported when running Instrumentation tests
     */
    override fun scanPackageTrees(
        include: List<String>,
        exclude: List<String>
    ): ScanResultFilterWithResult<T, R> {
        if (!isRunningOnJvm()) throw ScanSourceNotSupported()
        return classGraphSourceScanner.scanPackageTrees(include, exclude)
    }

    /**
     * Scan previews in the given file
     *
     * @param jsonFile a json file that was generated by using ScanResultDump.dumpScanResultToFile(fileName)
     *
     * Warning: Not supported when running Instrumentation tests
     */
    override fun scanFile(jsonFile: File): ScanResultFilterWithResult<T, R> {
        if (!isRunningOnJvm()) throw ScanSourceNotSupported()
        return classGraphSourceScanner.scanFile(jsonFile)
    }

    /**
     * Scan for previews in the given InputStream.
     *
     * @param targetInputStream to make them accessible in instrumentation tests, like getInstrumentation().context.assets.open(fileName).
     * Such file was generated previously by using ScanResultDump.dumpScanResultToFileInAssets(fileName)
     * @param customPreviewsInfoInputStream to make custom previews defined in another dependency or module,
     * like those in "androidx.compose.ui.tooling.preview", available in instrumentation tests.
     * Such file was generated previously by using ScanResultDump.dumpScanResultToFileInAssets(customPreviewsPackageTrees, customPreviewsFileName)
     */
    override fun scanFile(
        targetInputStream: InputStream,
        customPreviewsInfoInputStream: InputStream
    ): ScanResultFilterWithResult<T, R> =
        classGraphSourceScanner.scanFile(targetInputStream, customPreviewsInfoInputStream)
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
    override fun enableScanningLogs(): ComposablePreviewScanner<T> = apply {
        super.enableScanningLogs()
    }

    override fun setTargetSourceSet(
        sourceSetClasspath: Classpath,
        packageTreesOfCrossModuleCustomPreviews: List<String>
    ): ClassGraphSourceScanner<T> =
        super.setTargetSourceSet(sourceSetClasspath, packageTreesOfCrossModuleCustomPreviews) as ClassGraphSourceScanner<T>

    @RequiresLargeHeap
    override fun scanAllPackages(): ScanResultFilter<T> =
        super.scanAllPackages() as ScanResultFilter<T>

    override fun scanPackageTrees(vararg packageTrees: String): ScanResultFilter<T> =
        super.scanPackageTrees(*packageTrees) as ScanResultFilter<T>

    override fun scanPackageTrees(
        include: List<String>,
        exclude: List<String>
    ): ScanResultFilter<T> =
        super.scanPackageTrees(include, exclude) as ScanResultFilter<T>

    override fun scanFile(jsonFile: File): ScanResultFilter<T> =
        super.scanFile(jsonFile) as ScanResultFilter<T>

    override fun scanFile(
        targetInputStream: InputStream,
        customPreviewsInfoInputStream: InputStream
    ): ScanResultFilter<T> =
        super.scanFile(targetInputStream, customPreviewsInfoInputStream) as ScanResultFilter<T>
}
