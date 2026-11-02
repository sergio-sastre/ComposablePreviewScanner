package sergio.sastre.composable.preview.scanner.wear

import io.github.classgraph.AnnotationParameterValueList

internal object WearScannerUtils {
    @Suppress("UNCHECKED_CAST")
    fun AnnotationParameterValueList.valueForKey(key: String): Any? =
        this[key]?.value
}
