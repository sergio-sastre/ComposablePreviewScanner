package sergio.sastre.composable.preview.scanner.core.preview.screenshotid

import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview
import sergio.sastre.composable.preview.scanner.core.preview.screenshotid.PreviewScreenshotIdBuilder.ParameterIndex.DISPLAY_NAME
import sergio.sastre.composable.preview.scanner.core.preview.screenshotid.PreviewScreenshotIdBuilder.ParameterIndex.INDEX

/**
 * Provides a base class to generate unique screenshot ids based on [ComposablePreview]
 */
open class PreviewScreenshotIdBuilder<T>(
    private val composablePreview: ComposablePreview<T>,
    private val defaultPreviewInfoIdProvider: () -> LinkedHashMap<String, String?>
) {

    private enum class ParameterIndex {
        INDEX {
            override fun valueFor(preview: ComposablePreview<*>): String? =
                preview.previewIndex?.toString()

        },
        DISPLAY_NAME {
            override fun valueFor(preview: ComposablePreview<*>): String? =
                (preview.previewIndexDisplayName?.replace(" ", "_"))
                    ?: INDEX.valueFor(preview)
        };

        abstract fun valueFor(preview: ComposablePreview<*>): String?
    }

    private var ignoreClassName: Boolean = false
    private var ignoreMethodName: Boolean = false
    private var ignoreMethodParametersType: Boolean = true

    private var encodeUnsafeCharactersIsEnabled: Boolean = false

    private var parameterIndexName: ParameterIndex = DISPLAY_NAME

    private val defaultPreviewInfoId = defaultPreviewInfoIdProvider()

    fun overrideDefaultIdFor(
        previewInfoName: String,
        applyInfoValue: (T) -> String?
    ) = apply {
        defaultPreviewInfoId[previewInfoName] = applyInfoValue(composablePreview.previewInfo)
    }

    fun ignoreIdFor(
        previewInfoName: String,
    ) = apply {
        defaultPreviewInfoId[previewInfoName] = null
    }

    fun ignoreClassName() = apply {
        ignoreClassName = true
    }

    fun ignoreMethodName() = apply {
        ignoreMethodName = true
    }

    fun replaceIndexDisplayNameWithIndex() = apply {
        parameterIndexName = INDEX
    }

    /**
     * When using @PreviewParameters, this includes the parameter type in the generated screenshotId.
     *
     * The use case for this is, for Composable Preview Methods inside a class or file that
     * have the same name, but different signature (i.e. the arguments differ).
     * Without this option, both methods would get the same screenshot file name resulting in file overriding
     */
    fun doNotIgnoreMethodParametersType() = apply {
        ignoreMethodParametersType = false
    }

    /**
     * Encodes unsafe characters that could be problematic when using
     * certain Screenshot testing libraries like Paparazzi or Android-Testify
     *
     * Any of the following are considered an unsafe character:
     * <>:\"/\\|?*
     */
    fun encodeUnsafeCharacters() = apply {
        encodeUnsafeCharactersIsEnabled = true
    }

    private val unsafeFileNameChars = setOf('<', '>', ':', '"', '/', '\\', '|', '?', '*')

    private fun encodeUnsafeCharactersIn(fileName: String): String =
        buildString {
            for (ch in fileName) {
                when (ch in unsafeFileNameChars) {
                    true -> append("%${ch.code.toString(16).uppercase()}")
                    false -> append(ch)
                }
            }
        }

    fun build(): String {
        val fileName = buildList {
            val previewInfoId =
                defaultPreviewInfoId.values.filterNot { it.isNullOrBlank() }.joinToString("_")
            val declaringClass = when (ignoreClassName) {
                true -> null
                false -> composablePreview.declaringClass
            }
            val methodName = when (ignoreMethodName) {
                true -> null
                false -> composablePreview.methodName
            }
            add(
                listOfNotNull(
                    declaringClass,
                    methodName,
                    previewInfoId
                )
                    .filter { it.isNotBlank() }
                    .joinToString(".")
            )
            // DO these separately
            if (!ignoreMethodParametersType && composablePreview.methodParametersType.isNotBlank()) {
                add("_${composablePreview.methodParametersType}")
            }
            if (composablePreview.previewIndex != null) {
                this.add("_${parameterIndexName.valueFor(composablePreview)}")
            }
        }
            .joinToString("")
            .trim { it == '_'}

        return when (encodeUnsafeCharactersIsEnabled) {
            true -> encodeUnsafeCharactersIn(fileName)
            false -> fileName
        }
    }
}