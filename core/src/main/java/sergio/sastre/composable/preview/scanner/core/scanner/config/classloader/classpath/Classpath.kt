package sergio.sastre.composable.preview.scanner.core.scanner.config.classloader.classpath

data class Classpath(
    val packagePath: String,
    val rootDir: String = "${System.getProperty("user.dir")}/build"
)