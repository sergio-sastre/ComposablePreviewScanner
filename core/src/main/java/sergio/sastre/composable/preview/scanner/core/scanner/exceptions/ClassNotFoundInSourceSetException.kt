package sergio.sastre.composable.preview.scanner.core.scanner.exceptions

class ClassNotFoundInSourceSetException: Exception(
    "You need to include all classes of the target sourceSet when running Instrumentation tests. \n In order to do that modify the gradle build file accordingly. For instance, for the 'screenshotTest SourceSet:\n" +
            "\n" +
            "> val includeScreenshotTests = project.hasProperty(\"includeSourceSetScreenshotTest\")\n" +
            "> if (includeScreenshotTests) {\n" +
            ">     sourceSets {\n" +
            ">        getByName(\"androidTest\") {\n" +
            ">           java.srcDir(\"src/screenshotTest/java\") //or kotlin\n" +
            ">           res.srcDir(\"src/screenshotTest/res\")\n" +
            ">        }\n" +
            ">     }\n" +
            ">}\n" +
            ">\n" +
            "> And pass that gradle property when executing the screenshot tests via command-line, e.g.:\n" +
            "> ./gradlew :mymodule:screenshotTestLibRecordCommand -PincludeSourceSetScreenshotTest\n"
)