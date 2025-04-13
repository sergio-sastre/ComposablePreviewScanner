package sergio.sastre.composable.preview.scanner.core.scanner.logger

import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.Classpath

class ScanningTimeLogger {

    private var scanningTime: Long = 0
    private var scanningSource: String = ""
    private var annotationName: String = ""
    private var classpath: Classpath? = null

    fun setScanningTime(scanningTime: Long) {
        this.scanningTime = scanningTime
    }

    fun setPackageTrees(vararg packageTrees: String) {
        scanningSource = "Package trees: ${packageTrees.joinToString(",")}"
    }

    fun setSourceSet(classpath: Classpath) {
        this.classpath = classpath
    }

    fun setAnnotationName(annotationName: String) {
        this.annotationName = annotationName
    }

    fun printScanningTimeLog() {
        println("Composable Preview Scanner")
        println("=====================")
        println("Annotation: $annotationName")
        classpath?.run {
            println("Source set (compiled classes path): $rootDir/$packagePath")
        }
        println(scanningSource)
        println("Scanning time: $scanningTime ms")
    }
}