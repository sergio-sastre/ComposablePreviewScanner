import java.io.File
import java.net.URI
import java.util.Locale
import org.gradle.api.specs.Spec

// ==============================================================================
// CONFIGURATION THRESHOLDS WITH PERCENTAGE TOLERANCE OFFSET
// ==============================================================================
fun readNumericProperty(name: String, default: Double): Double {
    val raw = project.findProperty(name) as? String ?: return default
    return raw.toDoubleOrNull()
        ?: throw GradleException("Property '$name' must be a number, but was '$raw'.")
}

// Baseline expected execution values for MainSourceSetBenchmark
val expectedScoreMs: Double = readNumericProperty("expectedScoreMs", 85.0)
val expectedRamMb: Double = readNumericProperty("expectedRamMb", 2.50)

// Baseline expected execution values for BuildTimeBenchmark
val expectedBuildTimeScoreMs: Double = readNumericProperty("expectedBuildTimeScoreMs", 1500.0)
val expectedBuildTimeRamMb: Double = readNumericProperty("expectedBuildTimeRamMb", 210.0)

// Allowed percentage tolerance / offset above the baseline (default: 5.0%)
val tolerancePercent: Double = readNumericProperty("tolerancePercent", 5.0)

// Computed maximum allowed threshold including tolerance offset
val maxAllowedScoreMs: Double = (project.findProperty("maxAllowedScoreMs") as? String)?.toDoubleOrNull()
    ?: (expectedScoreMs * (1.0 + tolerancePercent / 100.0))

val maxAllowedRamMb: Double = (project.findProperty("maxAllowedRamMb") as? String)?.toDoubleOrNull()
    ?: (expectedRamMb * (1.0 + tolerancePercent / 100.0))

val maxAllowedBuildTimeScoreMs: Double = (project.findProperty("maxAllowedBuildTimeScoreMs") as? String)?.toDoubleOrNull()
    ?: (expectedBuildTimeScoreMs * (1.0 + tolerancePercent / 100.0))

val maxAllowedBuildTimeRamMb: Double = (project.findProperty("maxAllowedBuildTimeRamMb") as? String)?.toDoubleOrNull()
    ?: (expectedBuildTimeRamMb * (1.0 + tolerancePercent / 100.0))

data class BenchmarkThresholdConfig(
    val expectedScoreMs: Double,
    val maxAllowedScoreMs: Double,
    val expectedRamMb: Double,
    val maxAllowedRamMb: Double
)

fun thresholdConfigFor(simpleClassName: String): BenchmarkThresholdConfig =
    when (simpleClassName) {
        "BuildTimeBenchmark" -> BenchmarkThresholdConfig(
            expectedScoreMs = expectedBuildTimeScoreMs,
            maxAllowedScoreMs = maxAllowedBuildTimeScoreMs,
            expectedRamMb = expectedBuildTimeRamMb,
            maxAllowedRamMb = maxAllowedBuildTimeRamMb
        )
        else -> BenchmarkThresholdConfig(
            expectedScoreMs = expectedScoreMs,
            maxAllowedScoreMs = maxAllowedScoreMs,
            expectedRamMb = expectedRamMb,
            maxAllowedRamMb = maxAllowedRamMb
        )
    }

data class BenchmarkEntryResult(
    val fullName: String,
    val simpleClassName: String,
    val methodName: String,
    val score: Double,
    val rawSpeedValues: List<Double>,
    val speedTrimmedMean: Double,
    val ramValues: List<Double>,
    val avgRam: Double,
    val ramTrimmedMean: Double,
    val thresholdConfig: BenchmarkThresholdConfig
) {
    val displayName: String get() = "$simpleClassName.$methodName"
    val speedPassed: Boolean get() = speedTrimmedMean <= thresholdConfig.maxAllowedScoreMs
    val ramPassed: Boolean get() = ramValues.isNotEmpty() && ramTrimmedMean <= thresholdConfig.maxAllowedRamMb
}

val generateBenchmarkHtmlReport = tasks.register("generateBenchmarkHtmlReport") {
    group = "Verification"
    description = "Generates an interactive Chart.js HTML report from the latest benchmark json results"

    dependsOn("mainBenchmark")

    onlyIf(Spec {
        val mainState = tasks.named("mainBenchmark").get().state
        mainState.executed && mainState.failure == null
    })

    doLast {
        val mainState = tasks.named("mainBenchmark").get().state
        if (!mainState.executed) {
            throw GradleException("Cannot generate report: 'mainBenchmark' task has not run in the current build.")
        }
        val reportsDir = layout.buildDirectory.dir("reports/benchmarks").get().asFile
        val jsonFile = findLatestJsonReport(reportsDir)
        if (jsonFile != null) {
            buildHtmlReportFile(reportsDir, jsonFile)
        }
    }
}

val verifyBenchmarkThreshold = tasks.register("verifyBenchmarkThreshold") {
    group = "Verification"
    description = "Fails the build if benchmark speed or allocated memory per scan exceeds configured thresholds"

    dependsOn("mainBenchmark")

    onlyIf(Spec {
        val mainState = tasks.named("mainBenchmark").get().state
        mainState.executed && mainState.failure == null
    })

    doLast {
        val mainState = tasks.named("mainBenchmark").get().state
        if (!mainState.executed) {
            throw GradleException("Cannot verify threshold: 'mainBenchmark' task has not run in the current build.")
        }
        val reportsDir = layout.buildDirectory.dir("reports/benchmarks").get().asFile
        val jsonFile = findLatestJsonReport(reportsDir)
            ?: throw GradleException("No benchmark JSON report found in ${reportsDir.absolutePath}")
        checkPerformanceThreshold(jsonFile, reportsDir)
    }
}

afterEvaluate {
    tasks.named("mainBenchmarkCompile").configure {
        dependsOn(":tests:compileDebugKotlin")
        dependsOn(":custompreviews:compileDebugKotlin")
    }

    tasks.named("mainBenchmark").configure {
        dependsOn(":tests:compileDebugKotlin")
        dependsOn(":custompreviews:compileDebugKotlin")

        doFirst {
            val reportsDir = layout.buildDirectory.dir("reports/benchmarks").get().asFile
            reportsDir.walkTopDown().filter { it.extension == "json" }.forEach { it.delete() }
        }

        // Run report generation and threshold verification after benchmark execution
        finalizedBy(generateBenchmarkHtmlReport, verifyBenchmarkThreshold)
    }
}

// ==============================================================================
// HELPER FUNCTIONS
// ==============================================================================
fun findLatestJsonReport(reportsDir: File): File? {
    val jsonFiles = reportsDir.walkTopDown().filter { it.extension == "json" && it.length() > 10 }.toList()
    return jsonFiles.maxByOrNull { it.lastModified() }
}

fun calculateTrimmedMean(values: List<Double>): Double {
    if (values.size <= 2) return values.average()
    val list = java.util.ArrayList(values)
    java.util.Collections.sort(list)
    val trimmed = list.subList(1, list.size - 1)
    var sum = 0.0
    for (v in trimmed) { sum += v }
    return sum / trimmed.size
}

fun calculatePercentDiff(actual: Double, baseline: Double): String {
    if (baseline <= 0.0) return "N/A"
    val diff = ((actual - baseline) / baseline) * 100.0
    val sign = if (diff >= 0.0) "+" else ""
    return "$sign${String.format(Locale.US, "%.2f", diff)}%"
}

fun parseBenchmarkEntries(jsonText: String, reportsDir: File): List<BenchmarkEntryResult> {
    val entryRegex = Regex(
        """"benchmark"\s*:\s*"([^"]+)"[\s\S]*?"primaryMetric"\s*:\s*\{[\s\S]*?"score"\s*:\s*([^,\s{}]+)[\s\S]*?"rawData"\s*:\s*\[\s*\[([\d.,\s]+)\]\s*\]"""
    )
    val matches = entryRegex.findAll(jsonText).toList()
    if (matches.isEmpty()) {
        throw GradleException("Invalid benchmark report: no benchmark entries with 'primaryMetric.score' found.")
    }

    return matches.map { match ->
        val fullName = match.groupValues[1]
        val parts = fullName.split(".")
        val methodName = parts.lastOrNull() ?: fullName
        val simpleClassName = if (parts.size >= 2) parts[parts.size - 2] else fullName

        val rawToken = match.groupValues[2].trim()
        if (rawToken.startsWith("\"") || rawToken.endsWith("\"")) {
            throw GradleException("Invalid benchmark report: 'primaryMetric.score' must be a JSON number, but was quoted: $rawToken")
        }
        val score = rawToken.toDoubleOrNull()
            ?: throw GradleException("Invalid benchmark report: 'primaryMetric.score' is not a valid number: $rawToken")
        if (!score.isFinite()) {
            throw GradleException("Invalid benchmark report: 'primaryMetric.score' must be a finite number, but was: $score")
        }

        val rawSpeedValues = match.groupValues[3]
            .split(",")
            .mapNotNull { it.trim().toDoubleOrNull() }

        val speedTrimmedMean = if (rawSpeedValues.isNotEmpty()) calculateTrimmedMean(rawSpeedValues) else score

        val classSpecificRamFile = File(reportsDir, "ram_usage_$simpleClassName.txt")
        val legacyRamFile = File(reportsDir, "ram_usage.txt")
        val ramFile = if (classSpecificRamFile.exists()) classSpecificRamFile else legacyRamFile

        val ramValues: List<Double> = if (ramFile.exists()) {
            ramFile.readLines()
                .mapNotNull { it.trim().toDoubleOrNull() }
                .takeLast(rawSpeedValues.size.coerceAtLeast(1))
        } else emptyList()

        val avgRam = if (ramValues.isNotEmpty()) ramValues.average() else 0.0
        val ramTrimmedMean = if (ramValues.isNotEmpty()) calculateTrimmedMean(ramValues) else 0.0

        BenchmarkEntryResult(
            fullName = fullName,
            simpleClassName = simpleClassName,
            methodName = methodName,
            score = score,
            rawSpeedValues = rawSpeedValues,
            speedTrimmedMean = speedTrimmedMean,
            ramValues = ramValues,
            avgRam = avgRam,
            ramTrimmedMean = ramTrimmedMean,
            thresholdConfig = thresholdConfigFor(simpleClassName)
        )
    }
}

fun checkPerformanceThreshold(jsonFile: File, reportsDir: File) {
    val jsonText = jsonFile.readText()
    val entries = parseBenchmarkEntries(jsonText, reportsDir)
    val errors = java.util.ArrayList<String>()

    println("\n=======================================================")
    println("🔍 Performance Threshold Verification (Trimmed Mean - Baseline + ${String.format(Locale.US, "%.1f", tolerancePercent)}% Offset):")

    entries.forEachIndexed { index, entry ->
        if (index > 0) {
            println("=======================================================")
        }
        val cfg = entry.thresholdConfig
        if (entry.ramValues.isEmpty()) {
            throw GradleException("No valid allocated memory samples found for ${entry.displayName} in ${reportsDir.absolutePath}")
        }

        val speedDiffStr = calculatePercentDiff(entry.speedTrimmedMean, cfg.expectedScoreMs)
        val ramDiffStr = calculatePercentDiff(entry.ramTrimmedMean, cfg.expectedRamMb)

        println("📌 Benchmark: ${entry.displayName}")
        println("• Baseline Expected Speed: ${String.format(Locale.US, "%.2f", cfg.expectedScoreMs)} ms | Max Allowed (+${String.format(Locale.US, "%.1f", tolerancePercent)}%): ${String.format(Locale.US, "%.2f", cfg.maxAllowedScoreMs)} ms")
        println("• Trimmed Actual Speed:   ${String.format(Locale.US, "%.2f", entry.speedTrimmedMean)} ms (Raw Avg: ${String.format(Locale.US, "%.2f", entry.score)} ms) [$speedDiffStr vs baseline] -> ${if (entry.speedPassed) "✅ PASSED" else "❌ FAILED"}")
        println("-------------------------------------------------------")
        println("• Baseline Expected Alloc:${String.format(Locale.US, "%.2f", cfg.expectedRamMb)} MB | Max Allowed (+${String.format(Locale.US, "%.1f", tolerancePercent)}%): ${String.format(Locale.US, "%.2f", cfg.maxAllowedRamMb)} MB")
        println("• Trimmed Actual Alloc:   ${String.format(Locale.US, "%.2f", entry.ramTrimmedMean)} MB (Raw Avg: ${String.format(Locale.US, "%.2f", entry.avgRam)} MB) [$ramDiffStr vs baseline] -> ${if (entry.ramPassed) "✅ PASSED" else "❌ FAILED"}")

        if (!entry.speedPassed) {
            errors.add("[${entry.displayName}] Trimmed speed (${String.format(Locale.US, "%.2f", entry.speedTrimmedMean)} ms, $speedDiffStr vs baseline ${String.format(Locale.US, "%.2f", cfg.expectedScoreMs)} ms) exceeded max allowed threshold of ${String.format(Locale.US, "%.2f", cfg.maxAllowedScoreMs)} ms")
        }
        if (!entry.ramPassed) {
            errors.add("[${entry.displayName}] Trimmed allocated memory per scan (${String.format(Locale.US, "%.2f", entry.ramTrimmedMean)} MB, $ramDiffStr vs baseline ${String.format(Locale.US, "%.2f", cfg.expectedRamMb)} MB) exceeded max allowed threshold of ${String.format(Locale.US, "%.2f", cfg.maxAllowedRamMb)} MB")
        }
    }

    if (errors.isNotEmpty()) {
        println("❌ Benchmark FAILED! Threshold exceeded.")
        println("=======================================================\n")
        throw GradleException("Benchmark Failed! ${java.lang.String.join(" & ", errors)}")
    } else {
        println("✅ All Performance Checks Passed!")
        println("=======================================================\n")
    }
}

fun buildHtmlReportFile(reportsDir: File, jsonFile: File) {
    val jsonText = jsonFile.readText()
    val reportFile = File(reportsDir, "report.html")
    val entries = parseBenchmarkEntries(jsonText, reportsDir)

    // Ensure pinned local chart.js script is included alongside report.html
    val chartJsFile = File(reportsDir, "chart.js")
    if (!chartJsFile.exists()) {
        try {
            val connection = URI("https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js")
                .toURL()
                .openConnection()
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            val chartJsContent = connection.getInputStream()
                .bufferedReader()
                .use { it.readText() }
            chartJsFile.writeText(chartJsContent)
        } catch (e: Exception) {
            println("Warning: Could not download chart.js locally: ${e.message}")
        }
    }

    val chartJsScriptSrc = if (chartJsFile.exists()) "chart.js" else "https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"

    val sectionsHtml = StringBuilder()
    val chartsJs = StringBuilder()

    entries.forEachIndexed { idx, entry ->
        val cfg = entry.thresholdConfig
        val speedTrimmedFormatted = String.format(Locale.US, "%.2f", entry.speedTrimmedMean)
        val ramTrimmedFormatted = if (entry.ramValues.isEmpty()) "N/A" else "${String.format(Locale.US, "%.2f", entry.ramTrimmedMean)} MB"
        val speedDiffStr = calculatePercentDiff(entry.speedTrimmedMean, cfg.expectedScoreMs)
        val ramDiffStr = if (entry.ramValues.isEmpty()) "N/A" else calculatePercentDiff(entry.ramTrimmedMean, cfg.expectedRamMb)
        val labels = entry.rawSpeedValues.indices.map { "\"Iteration ${it + 1}\"" }

        sectionsHtml.append(
            """
            <div class="card">
                <h1>🚀 ComposablePreviewScanner Benchmark Report</h1>
                <div class="sub">Benchmark: ${entry.displayName}</div>
                
                <div class="score-container">
                    <div>
                        <div class="label">Trimmed Speed (Olympic Mean)</div>
                        <div class="score-badge">${speedTrimmedFormatted} ms/op</div>
                        <div class="diff-tag ${if (entry.speedPassed) "diff-pass" else "diff-fail"}">${speedDiffStr} vs baseline (${String.format(Locale.US, "%.2f", cfg.expectedScoreMs)} ms)</div>
                    </div>
                    <div>
                        <div class="label">Trimmed Allocated Memory (per scan)</div>
                        <div class="ram-badge">${ramTrimmedFormatted}</div>
                        <div class="diff-tag ${if (entry.ramPassed) "diff-pass" else "diff-fail"}">${ramDiffStr} vs baseline (${String.format(Locale.US, "%.2f", cfg.expectedRamMb)} MB)</div>
                    </div>
                </div>
            </div>

            <div class="grid">
                <div class="card">
                    <h2>⚡ Execution Speed (${entry.simpleClassName})</h2>
                    <canvas id="speedChart_$idx"></canvas>
                </div>
                <div class="card">
                    <h2>💾 Allocated Memory per scan (${entry.simpleClassName})</h2>
                    <canvas id="ramChart_$idx"></canvas>
                </div>
            </div>
            """.trimIndent()
        )
        sectionsHtml.append("\n")

        chartsJs.append(
            """
            new Chart(document.getElementById('speedChart_$idx').getContext('2d'), {
                type: 'bar',
                data: {
                    labels: [${labels.joinToString(", ")}],
                    datasets: [{
                        label: 'Speed (ms)',
                        data: [${entry.rawSpeedValues.joinToString(", ")}],
                        backgroundColor: 'rgba(37, 99, 235, 0.6)',
                        borderColor: 'rgba(37, 99, 235, 1)',
                        borderWidth: 2
                    }]
                },
                options: {
                    responsive: true,
                    scales: {
                        y: { beginAtZero: true, title: { display: true, text: 'Time (ms)' } }
                    }
                }
            });

            new Chart(document.getElementById('ramChart_$idx').getContext('2d'), {
                type: 'bar',
                data: {
                    labels: [${labels.joinToString(", ")}],
                    datasets: [{
                        label: 'Allocated per scan (MB)',
                        data: [${entry.ramValues.joinToString(", ")}],
                        backgroundColor: 'rgba(5, 150, 105, 0.6)',
                        borderColor: 'rgba(5, 150, 105, 1)',
                        borderWidth: 2
                    }]
                },
                options: {
                    responsive: true,
                    scales: {
                        y: { beginAtZero: true, title: { display: true, text: 'Allocation (MB)' } }
                    }
                }
            });
            """.trimIndent()
        )
        chartsJs.append("\n")
    }

    val html = """
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="UTF-8">
        <title>Benchmark HTML Report</title>
        <script src="$chartJsScriptSrc"></script>
        <style>
            body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; margin: 40px; background: #f8f9fa; color: #333; }
            .card { background: white; padding: 28px; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.08); max-width: 850px; margin: 0 auto 24px auto; }
            h1 { margin-top: 0; color: #111; font-size: 24px; }
            .score-container { display: flex; gap: 32px; margin: 16px 0; }
            .score-badge { font-size: 28px; font-weight: bold; color: #2563eb; }
            .ram-badge { font-size: 28px; font-weight: bold; color: #059669; }
            .diff-tag { display: inline-block; font-size: 13px; font-weight: 600; padding: 4px 10px; border-radius: 6px; margin-top: 6px; }
            .diff-pass { background: #e6f4ea; color: #137333; }
            .diff-fail { background: #fce8e6; color: #c5221f; }
            .label { font-size: 13px; color: #666; text-transform: uppercase; letter-spacing: 0.5px; }
            .sub { color: #666; font-size: 14px; margin-bottom: 24px; }
            canvas { max-width: 100%; margin-top: 16px; }
            .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; max-width: 850px; margin: 0 auto 36px auto; }
        </style>
    </head>
    <body>
        $sectionsHtml
        <script>
            $chartsJs
        </script>
    </body>
    </html>
    """.trimIndent()

    reportFile.writeText(html)
    println("\n=======================================================")
    println("📊 Local HTML Benchmark Report generated at:")
    println("file://${reportFile.absolutePath}")
    println("=======================================================\n")
}
