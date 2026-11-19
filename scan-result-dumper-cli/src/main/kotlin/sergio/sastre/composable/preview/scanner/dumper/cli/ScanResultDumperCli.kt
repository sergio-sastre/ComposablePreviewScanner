package sergio.sastre.composable.preview.scanner.dumper.cli

import sergio.sastre.composable.preview.scanner.core.annotations.RequiresShowStandardStreams
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.Classpath
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.SourceSet.MAIN
import sergio.sastre.composable.preview.scanner.core.scanresult.dump.ScanResultDumper
import java.io.File

private data class DumpSpec(
    val packageTree: String,
    val scanFileName: String,
    val customPreviewPackages: List<String>,
    val customPreviewsFileName: String,
)

private data class CliArgs(
    val specs: List<DumpSpec>,
    val customPreviewsClasspath: List<File>,
)

@OptIn(RequiresShowStandardStreams::class)
fun main(args: Array<String>) {
    val cliArgs = parseArgs(args)
    require(cliArgs.specs.isNotEmpty()) {
        "At least one --spec <packageTree>|<scanFileName>|<commaSeparatedCustomPackages>|<customPreviewsFileName> argument is required"
    }

    cliArgs.specs.forEach { spec ->
        ScanResultDumper()
            .enableScanningLogs()
            .setCustomPreviewsClasspath(cliArgs.customPreviewsClasspath)
            .setTargetSourceSet(Classpath(MAIN))
            .scanPackageTrees(spec.packageTree)
            .dumpScanResultToFileInAssets(
                scanFileName = spec.scanFileName,
                packageTreesOfCustomPreviews = spec.customPreviewPackages,
                customPreviewsFileName = spec.customPreviewsFileName,
            )
    }
}

private fun parseArgs(args: Array<String>): CliArgs {
    val specs = mutableListOf<DumpSpec>()
    var customPreviewsClasspath: List<File> = emptyList()
    var index = 0
    while (index < args.size) {
        when (args[index]) {
            "--spec" -> {
                require(index + 1 < args.size) { "Missing value after --spec" }
                val parts = args[index + 1].split("|")
                require(parts.size == 4) {
                    "Invalid --spec format: '${args[index + 1]}'. Expected <packageTree>|<scanFileName>|<commaSeparatedCustomPackages>|<customPreviewsFileName>"
                }
                val customPackages = parts[2]
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                specs += DumpSpec(
                    packageTree = parts[0].trim(),
                    scanFileName = parts[1].trim(),
                    customPreviewPackages = customPackages,
                    customPreviewsFileName = parts[3].trim(),
                )
                index += 2
            }

            "--custom-previews-classpath" -> {
                require(index + 1 < args.size) { "Missing value after --custom-previews-classpath" }
                customPreviewsClasspath = args[index + 1]
                    .split(File.pathSeparator)
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .map(::File)
                index += 2
            }

            else -> throw IllegalArgumentException("Unknown argument: ${args[index]}")
        }
    }
    return CliArgs(
        specs = specs,
        customPreviewsClasspath = customPreviewsClasspath,
    )
}
