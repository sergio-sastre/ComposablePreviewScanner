package sergio.sastre.composable.preview.scanner.core.utils

import java.io.File
import java.util.Locale

/**
 * Returns a file with the give fileName located in src/test
 */
fun testFilePath(fileName: String): File {
    val path = System.getProperty("user.dir")
    return File("$path/src/test", fileName)
}

/**
 * Returns a file with the give fileName from assets located in src/androidTest$flavourName
 */
fun assetsFilePath(fileName: String, flavourName: String = ""): File {
    val path = System.getProperty("user.dir")
    return File("$path/src/androidTest${flavourName.capitalize(Locale.ROOT)}/assets", fileName)
}