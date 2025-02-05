package sergio.sastre.composable.preview.scanner.core.scanresult.filter.exceptions

class RepeatableAnnotationNotSupportedException(
    methodName: String,
    repeatableAnnotations: List<Class<out Annotation>>
) : RuntimeException(
    "$methodName cannot be called with Repeatable annotations '${repeatableAnnotations.joinToString(",") { it.simpleName.toString() }}'"
)