package sergio.sastre.composable.preview.scanner.core.scanner.logger

import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.Classpath

class ScanningTimeLogger {

    private var scanningFilesTime: Long = 0
    private var findPreviewsTime: Long = 0
    private var scanningSource: String = ""
    private var annotationName: String = ""
    private var classpath: Classpath? = null

    fun setScanningTime(scanningTime: Long) {
        this.scanningFilesTime = scanningTime
    }

    fun setFindPreviewsTime(findPreviewsTime: Long) {
        this.findPreviewsTime = findPreviewsTime
    }

    fun setPackageTrees(vararg packageTrees: String) {
        scanningSource = "Package trees: ${packageTrees.joinToString(", ")}"
    }

    fun setPackageTrees(included: List<String>, excluded: List<String>) {
        scanningSource =
            "Included package trees: ${included.joinToString(", ")}" +
                    "\n" +
                    "Excluded package trees: ${excluded.joinToString(", ")}"
    }

    fun setAllPackages() {
        scanningSource = "Scans all packages"
    }

    fun setSourceSet(classpath: Classpath) {
        this.classpath = classpath
    }

    fun setAnnotationName(annotationName: String) {
        this.annotationName = annotationName
    }

    fun printFullInfoLog() {
        println("Composable Preview Scanner")
        println("===============================")
        println("@Preview annotation: $annotationName")
        classpath?.run {
            println("Source set (compiled classes path): $rootDir/$packagePath")
        }
        println(scanningSource)
        println()
        println("Time to scan target files: $scanningFilesTime ms")
        println("Time to find @Previews: $findPreviewsTime ms")
        println("---------------------------------------------------")
        println("Total time: ${scanningFilesTime + findPreviewsTime} ms")
        println()
        println()
    }
}