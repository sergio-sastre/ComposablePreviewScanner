package sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.classloaders

import io.github.classgraph.ClassInfo

internal interface ClassLoader {
    fun loadClass(classInfo: ClassInfo): Class<*>
}