package sergio.sastre.composable.preview.scanner.wear

import java.lang.reflect.Method

internal object WearScannerUtils {
    fun io.github.classgraph.MethodInfo.loadClassAndGetMethod(): Method {
        val clazz = Class.forName(this.classInfo.name)
        return clazz.declaredMethods.first { it.name == this.name && it.parameterCount == this.parameterInfo.size }
    }

    @Suppress("UNCHECKED_CAST")
    fun io.github.classgraph.AnnotationParameterValueList.valueForKey(key: String): Any? =
        this[key]?.value
}
