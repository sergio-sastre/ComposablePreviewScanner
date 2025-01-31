package sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.validator

import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.Classpath
import sergio.sastre.composable.preview.scanner.core.scanner.exceptions.CompiledClassesNotFound
import sergio.sastre.composable.preview.scanner.core.utils.isRunningOnJvm
import java.io.File

class ClasspathValidator(
    val classpath: Classpath
) {
    fun validate() {
        // Instrumentation tests use classloaders that cannot access local files, like those under /build
        // so it can only read classes that are in the .apk
        if (isRunningOnJvm()) {
            val compiledClassesDir = File(classpath.rootDir, classpath.packagePath)
            if (!compiledClassesDir.exists()) throw CompiledClassesNotFound(classpath)
        }
    }
}