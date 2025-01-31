package sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.classloaders

import io.github.classgraph.ClassInfo
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.Classpath
import sergio.sastre.composable.preview.scanner.core.scanner.exceptions.ClassNotFoundInSourceSetException
import sergio.sastre.composable.preview.scanner.core.utils.isRunningOnJvm
import java.io.File
import java.net.URLClassLoader

internal class SourceSetClassLoader(
    private val classpath: Classpath
) : ClassLoader {
    private val buildDir = "build/${classpath.packagePath}"

    override fun loadClass(classInfo: ClassInfo): Class<*> {
        val url = File(buildDir).toURI().toURL()
        try {
            val classLoader = URLClassLoader(arrayOf(url), this.javaClass.classLoader)
            return classLoader.loadClass(classInfo.name)
        } catch (e: ClassNotFoundException) {
            when (!isRunningOnJvm() && classpath.packagePath.lowercase().contains("screenshottest")) {
                true -> throw ClassNotFoundInSourceSetException()
                false -> throw e
            }
        }
    }
}