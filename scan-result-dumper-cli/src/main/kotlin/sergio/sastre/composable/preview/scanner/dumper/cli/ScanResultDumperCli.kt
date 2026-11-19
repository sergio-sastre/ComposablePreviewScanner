package sergio.sastre.composable.preview.scanner.dumper.cli

import sergio.sastre.composable.preview.scanner.core.annotations.RequiresShowStandardStreams
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.Classpath
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.SourceSet.MAIN
import sergio.sastre.composable.preview.scanner.core.scanresult.dump.ScanResultDumper

private data class DumpSpec(
    val packageTree: String,
    val scanFileName: String,
    val customPreviewPackages: List<String>,
    val customPreviewsFileName: String,
)

@OptIn(RequiresShowStandardStreams::class)
fun main(args: Array<String>) {
    val specs = parseSpecs(args)
    require(specs.isNotEmpty()) {
        "At least one --spec <packageTree>|<scanFileName>|<commaSeparatedCustomPackages>|<customPreviewsFileName> argument is required"
    }

    specs.forEach { spec ->
        ScanResultDumper()
            .enableScanningLogs()
            .setTargetSourceSet(Classpath(MAIN))
            .scanPackageTrees(spec.packageTree)
            .dumpScanResultToFileInAssets(
                scanFileName = spec.scanFileName,
                packageTreesOfCustomPreviews = spec.customPreviewPackages,
                customPreviewsFileName = spec.customPreviewsFileName,
            )
    }
}

private fun parseSpecs(args: Array<String>): List<DumpSpec> {
    val specs = mutableListOf<DumpSpec>()
    var index = 0
    while (index < args.size) {
        if (args[index] == "--spec") {
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
        } else {
            throw IllegalArgumentException("Unknown argument: ${args[index]}")
        }
    }
    return specs
}
