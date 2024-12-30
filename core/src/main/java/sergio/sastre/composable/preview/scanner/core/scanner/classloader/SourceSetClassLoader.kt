package sergio.sastre.composable.preview.scanner.core.scanner.classloader

import io.github.classgraph.ClassInfo
import java.io.File
import java.net.URLClassLoader

internal class SourceSetClassLoader(
    private val classpath: String
): ClassLoader {
    private val buildDir = "build/$classpath"

    override fun loadClass(classInfo: ClassInfo): Class<*> {
        val url = File(buildDir).toURI().toURL()
        val classLoader = URLClassLoader(arrayOf(url), this.javaClass.classLoader)
        return classLoader.loadClass(classInfo.name)
    }
}