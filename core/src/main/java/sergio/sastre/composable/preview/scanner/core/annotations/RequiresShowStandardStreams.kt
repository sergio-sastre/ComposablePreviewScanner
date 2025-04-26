package sergio.sastre.composable.preview.scanner.core.annotations

@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "Currently, logs are printed in the console via the standard output." +
            "In order to see them, standard streams must be enabled in jvm tests." +
            "However, when using Junit4, that is not the default."+
            "The easiest way to enable them is to add the following line to your build.gradle file:\n" +
            "testOptions.unitTests {\n" +
            "    all { test -> \n" +
            "        test.testLogging { showStandardStreams = true }\n" +
            "    }\n" +
            "}\n"
)
annotation class RequiresShowStandardStreams()