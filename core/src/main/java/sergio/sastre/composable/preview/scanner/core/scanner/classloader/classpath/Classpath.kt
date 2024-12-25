package sergio.sastre.composable.preview.scanner.core.scanner.classloader.classpath

data class Classpath(
    val packagePath: String,
    val buildDir: String = "${System.getProperty("user.dir")}/build"
) {

    companion object {
        fun fromPackageList(
            packagePaths: List<String>,
            commonBuildDir: String = System.getProperty("user.dir") + "/build"
        ): List<Classpath> =
            packagePaths.map { packagePath -> Classpath(commonBuildDir, packagePath) }
    }
}
