package sergio.sastre.composable.preview.scanner.core.scanner.classloader

import io.github.classgraph.ClassInfo

internal class ReflectionClassLoader: ClassLoader {
    override fun loadClass(classInfo: ClassInfo): Class<*> =
        Class.forName(classInfo.name, false, this.javaClass.classLoader)
        //classInfo.loadClass()
}