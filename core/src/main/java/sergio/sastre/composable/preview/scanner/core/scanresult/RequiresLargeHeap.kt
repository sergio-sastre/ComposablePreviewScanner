package sergio.sastre.composable.preview.scanner.core.scanresult

@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "Increase the heap size to mitigate OOM Exceptions. " +
            "For Unit Tests, add the corresponding jvm args " +
            "either in the gradle file or via command line by adding -Xmx<value> e.g. -Xmx2g. " +
            "For Instrumentation Tests, add android:largeHeap = \"true\" " +
            "in the application section of your androidTest manifest"
)
annotation class RequiresLargeHeap
