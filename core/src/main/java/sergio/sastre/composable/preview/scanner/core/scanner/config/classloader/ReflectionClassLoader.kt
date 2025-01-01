package sergio.sastre.composable.preview.scanner.core.scanner.config.classloader

import io.github.classgraph.ClassInfo

internal class ReflectionClassLoader: ClassLoader {
    override fun loadClass(classInfo: ClassInfo): Class<*> =
        Class.forName(classInfo.name, false, this.javaClass.classLoader)
}