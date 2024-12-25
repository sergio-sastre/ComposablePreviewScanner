package sergio.sastre.composable.preview.scanner.core.scanner.classloader

import io.github.classgraph.ClassInfo

internal interface ClassLoader {
    fun loadClass(classInfo: ClassInfo): Class<*>
}