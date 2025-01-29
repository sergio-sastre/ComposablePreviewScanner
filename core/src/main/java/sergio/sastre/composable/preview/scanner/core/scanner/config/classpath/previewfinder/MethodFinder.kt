package sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder

import io.github.classgraph.ClassInfo
import io.github.classgraph.MethodInfo
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.previewfinder.classloaders.ClassLoader
import java.lang.reflect.Method

/**
 * Finds a method (i.e. the Composable function) inside a class
 */
internal class MethodFinder(
    private val classInfo: ClassInfo,
    private val classLoader: ClassLoader,
) {
    fun find(methodInfo: MethodInfo): Method {
        val clazz = classLoader.loadClass(classInfo)
        val methods = if (methodInfo.isPrivate) clazz.declaredMethods else clazz.methods
        return methods.asSequence()
            .filter { it.name == methodInfo.name }
            .let { matchingMethods ->
                if (matchingMethods.count() == 1) {
                    matchingMethods.first()
                } else {
                    // several methods with the same name but different parameter types
                    // find the first method with matching parameter types
                    matchingMethods.firstOrNull { method ->
                        method.parameterTypes.map { it.canonicalName } ==
                                methodInfo.parameterInfo.map {
                                    it.typeSignatureOrTypeDescriptor?.toString()
                                        ?.substringBefore('<')
                                }
                    } ?: matchingMethods.first()
                }

            }.also { method ->
                if (methodInfo.isPrivate) {
                    method.isAccessible = true
                }
            }
    }
}