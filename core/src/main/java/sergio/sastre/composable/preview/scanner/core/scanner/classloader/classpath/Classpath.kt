package sergio.sastre.composable.preview.scanner.core.scanner.classloader.classpath

data class Classpath(
    val packagePath: String,
    val buildDir: String = "${System.getProperty("user.dir")}/build"
)