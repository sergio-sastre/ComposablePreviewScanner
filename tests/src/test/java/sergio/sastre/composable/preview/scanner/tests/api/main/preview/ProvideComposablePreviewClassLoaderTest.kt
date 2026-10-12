package sergio.sastre.composable.preview.scanner.tests.api.main.preview

import androidx.compose.runtime.Composer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreviewWithResult
import sergio.sastre.composable.preview.scanner.core.preview.ProvideComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.ProvideComposablePreviewWithResult
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapper
import sergio.sastre.composable.preview.scanner.core.preview.mappers.ComposablePreviewMapperWithResult

@RunWith(Parameterized::class)
class ProvideComposablePreviewClassLoaderTest(private val withResult: Boolean) {

    @Test
    fun `GIVEN context loader with duplicate preview interfaces WHEN providing preview THEN invocation succeeds`() {
        val classLoader = object : ClassLoader(ComposablePreviewWithResult::class.java.classLoader) {
            override fun loadClass(name: String, resolve: Boolean): Class<*> {
                if (name !in previewInterfaceNames) return super.loadClass(name, resolve)
                synchronized(getClassLoadingLock(name)) {
                    val loaded = findLoadedClass(name) ?: run {
                        val resource = name.replace('.', '/') + ".class"
                        val bytes = parent.getResourceAsStream(resource)!!.use { it.readBytes() }
                        defineClass(name, bytes, 0, bytes.size)
                    }
                    if (resolve) resolveClass(loaded)
                    return loaded
                }
            }
        }
        assertNotSame(ComposablePreviewWithResult::class.java, classLoader.loadClass(ComposablePreviewWithResult::class.java.name))
        assertNotSame(ComposablePreview::class.java, classLoader.loadClass(ComposablePreview::class.java.name))

        verifyPreview(classLoader)
    }

    @Test
    fun `GIVEN context loader unable to load preview interfaces WHEN providing preview THEN invocation succeeds`() {
        val classLoader = object : ClassLoader(ComposablePreviewWithResult::class.java.classLoader) {
            override fun loadClass(name: String, resolve: Boolean): Class<*> {
                if (name in previewInterfaceNames) throw ClassNotFoundException(name)
                return super.loadClass(name, resolve)
            }
        }
        previewInterfaceNames.forEach { name ->
            assertThrows(ClassNotFoundException::class.java) { classLoader.loadClass(name) }
        }

        verifyPreview(classLoader)
    }

    private fun verifyPreview(classLoader: ClassLoader) {
        val thread = Thread.currentThread()
        val originalClassLoader = thread.contextClassLoader
        try {
            thread.contextClassLoader = classLoader
            invocationCount = 0
            val methodName = if (withResult) "resultPreview" else "unitPreview"
            val method = javaClass.getMethod(methodName)
            val preview = if (withResult) {
                val mapper = object : ComposablePreviewMapperWithResult<String, String>(method, "info", null) {
                    override fun mapToComposablePreviews(): Sequence<ComposablePreviewWithResult<String, String>> = emptySequence()
                }
                ProvideComposablePreviewWithResult<String, String>()(mapper, 2, "display name")
            } else {
                val mapper = object : ComposablePreviewMapper<String>(method, "info", null) {
                    override fun mapToComposablePreviews(): Sequence<ComposablePreview<String>> = emptySequence()
                }
                val unitPreview: ComposablePreview<String> = ProvideComposablePreview<String>()(mapper, 2, "display name")
                unitPreview
            }
            assertEquals("info", preview.previewInfo)
            assertEquals(2, preview.previewIndex)
            assertEquals("display name", preview.previewIndexDisplayName)
            assertEquals(javaClass.canonicalName, preview.declaringClass)
            assertEquals(methodName, preview.methodName)
            assertEquals("", preview.methodParametersType)
            assertEquals("${javaClass.canonicalName}_${methodName}_2", preview.toString())

            // Invoke the compiler-added Composer/changed signature without needing a composition:
            // the fixture methods are non-composable previews and do not consume a Composer.
            val result = ComposablePreviewWithResult::class.java
                .getMethod("invoke", Composer::class.java, Int::class.javaPrimitiveType)
                .invoke(preview, null, 0)
            if (withResult) assertEquals("result", result)
            assertEquals(1, invocationCount)
        } finally {
            thread.contextClassLoader = originalClassLoader
        }
    }

    companion object {
        @JvmStatic
        fun unitPreview() {
            invocationCount++
        }

        @JvmStatic
        fun resultPreview(): String {
            invocationCount++
            return "result"
        }

        private var invocationCount = 0
        private val previewInterfaceNames = setOf(
            ComposablePreviewWithResult::class.java.name,
            ComposablePreview::class.java.name,
        )

        @JvmStatic
        @Parameterized.Parameters(name = "withResult={0}")
        fun data(): Collection<Array<Any>> = listOf(arrayOf(false), arrayOf(true))
    }
}
