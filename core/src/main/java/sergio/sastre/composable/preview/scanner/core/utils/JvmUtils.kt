package sergio.sastre.composable.preview.scanner.core.utils

import java.util.Locale

internal fun isRunningOnJvm(): Boolean =
    System.getProperty("java.runtime.name")
        ?.lowercase(Locale.getDefault())
        ?.contains("android") != true