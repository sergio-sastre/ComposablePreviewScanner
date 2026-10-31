import java.io.File
import java.net.URI
import java.util.Locale
import org.gradle.api.specs.Spec

// ==============================================================================
// CONFIGURATION THRESHOLDS
// ==============================================================================
// Maximum allowed speed threshold in milliseconds (ms/op).
// Override via CLI: ./gradlew :benchmark:benchmark -PmaxAllowedScoreMs=20.0
val maxAllowedScoreMs: Double = (project.findProperty("maxAllowedScoreMs") as? String)?.toDoubleOrNull() ?: 20.0

// Maximum allowed RAM allocation threshold in megabytes (MB).
// Override via CLI: ./gradlew :benchmark:benchmark -PmaxAllowedRamMb=35.0
val maxAllowedRamMb: Double = (project.findProperty("maxAllowedRamMb") as? String)?.toDoubleOrNull() ?: 35.0

val generateBenchmarkHtmlReport = tasks.register("generateBenchmarkHtmlReport") {
    group = "Verification"
    description = "Generates an interactive Chart.js HTML report from the latest benchmark json results"

    onlyIf(Spec {
        tasks.named("mainBenchmark").get().state.failure == null
    })

    doLast {
        val reportsDir = layout.buildDirectory.dir("reports/benchmarks").get().asFile
        val jsonFile = findLatestJsonReport(reportsDir)
        if (jsonFile != null) {
            buildHtmlReportFile(reportsDir, jsonFile)
        }
    }
}

val verifyBenchmarkThreshold = tasks.register("verifyBenchmarkThreshold") {
    group = "Verification"
    description = "Fails the build if benchmark speed or RAM allocation exceeds configured thresholds"

    onlyIf(Spec {
        tasks.named("mainBenchmark").get().state.failure == null
    })

    doLast {
        val reportsDir = layout.buildDirectory.dir("reports/benchmarks").get().asFile
        val jsonFile = findLatestJsonReport(reportsDir)
            ?: throw GradleException("No benchmark JSON report found in ${reportsDir.absolutePath}")
        checkPerformanceThreshold(jsonFile, reportsDir, maxAllowedScoreMs, maxAllowedRamMb)
    }
}

afterEvaluate {
    tasks.named("mainBenchmarkCompile").configure {
        dependsOn(":tests:compileDebugKotlin")
        dependsOn(":tests:compileDebugAndroidTestKotlin")
        dependsOn(":custompreviews:compileDebugKotlin")
    }

    tasks.named("mainBenchmark").configure {
        dependsOn(":tests:compileDebugKotlin")
        dependsOn(":tests:compileDebugAndroidTestKotlin")
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

fun extractPrimaryMetricScore(jsonText: String): Double {
    val primaryMetricRegex = Regex(""""primaryMetric"\s*:\s*\{[^}]*?"score"\s*:\s*([^,\s{}]+)""")
    val match = primaryMetricRegex.find(jsonText)
        ?: throw GradleException("Invalid benchmark report: 'primaryMetric.score' is missing.")

    val rawGroup = match.groups[1]?.value ?: ""
    val rawToken = rawGroup.trim()

    if (rawToken.startsWith("\"") || rawToken.endsWith("\"")) {
        throw GradleException("Invalid benchmark report: 'primaryMetric.score' must be a JSON number, but was quoted: $rawToken")
    }

    val score = rawToken.toDoubleOrNull()
        ?: throw GradleException("Invalid benchmark report: 'primaryMetric.score' is not a valid number: $rawToken")

    if (!score.isFinite()) {
        throw GradleException("Invalid benchmark report: 'primaryMetric.score' must be a finite number, but was: $score")
    }

    return score
}

fun checkPerformanceThreshold(jsonFile: File, reportsDir: File, thresholdMs: Double, thresholdRamMb: Double) {
    val jsonText = jsonFile.readText()
    val score = extractPrimaryMetricScore(jsonText)

    val rawDataMatch = Regex(""""rawData"\s*:\s*\[\s*\[([\d.,\s]+)\]\s*\]""").find(jsonText)
    val rawValuesCount = rawDataMatch?.groupValues?.get(1)
        ?.split(",")
        ?.mapNotNull { it.trim().toDoubleOrNull() }?.size ?: 5

    // Read recorded RAM allocation data
    val ramFile = File(reportsDir, "ram_usage.txt")
    val ramValues: List<Double> = if (ramFile.exists()) {
        ramFile.readLines()
            .mapNotNull { it.trim().toDoubleOrNull() }
            .takeLast(rawValuesCount)
    } else emptyList()

    if (ramValues.isEmpty()) {
        throw GradleException("No valid RAM allocation samples found in ${ramFile.absolutePath}")
    }

    val avgRam = ramValues.average()

    val speedPassed = score <= thresholdMs
    val ramPassed = avgRam <= thresholdRamMb

    println("\n=======================================================")
    println("🔍 Performance Threshold Verification:")
    println("• Average Speed: ${String.format(Locale.US, "%.2f", score)} ms | Max Allowed: $thresholdMs ms -> ${if (speedPassed) "✅ PASSED" else "❌ FAILED"}")
    println("• Average RAM:   ${String.format(Locale.US, "%.2f", avgRam)} MB | Max Allowed: $thresholdRamMb MB -> ${if (ramPassed) "✅ PASSED" else "❌ FAILED"}")

    if (!speedPassed || !ramPassed) {
        println("❌ Benchmark FAILED! Threshold exceeded.")
        println("=======================================================\n")
        val errors = java.util.ArrayList<String>()
        if (!speedPassed) errors.add("Average speed (${String.format(Locale.US, "%.2f", score)} ms) exceeded threshold of $thresholdMs ms")
        if (!ramPassed) errors.add("Average RAM allocation (${String.format(Locale.US, "%.2f", avgRam)} MB) exceeded threshold of $thresholdRamMb MB")
        throw GradleException("Benchmark Failed! ${java.lang.String.join(" & ", errors)}")
    } else {
        println("✅ All Performance Checks Passed!")
        println("=======================================================\n")
    }
}

fun buildHtmlReportFile(reportsDir: File, jsonFile: File) {
    val jsonText = jsonFile.readText()
    val reportFile = File(reportsDir, "report.html")

    val score = extractPrimaryMetricScore(jsonText)

    val rawDataMatch = Regex(""""rawData"\s*:\s*\[\s*\[([\d.,\s]+)\]\s*\]""").find(jsonText)
    val rawValues = rawDataMatch?.groupValues?.get(1)
        ?.split(",")
        ?.mapNotNull { it.trim().toDoubleOrNull() } ?: emptyList()

    val labels = rawValues.indices.map { "\"Iteration ${it + 1}\"" }
    val scoreFormatted = String.format(Locale.US, "%.2f", score)

    // Read recorded RAM allocation data
    val ramFile = File(reportsDir, "ram_usage.txt")
    val ramValues: List<Double> = if (ramFile.exists()) {
        ramFile.readLines()
            .mapNotNull { it.trim().toDoubleOrNull() }
            .takeLast(rawValues.size)
    } else emptyList()

    val avgRam = if (ramValues.isNotEmpty()) ramValues.average() else 0.0
    val avgRamFormatted = String.format(Locale.US, "%.2f", avgRam)

    // Ensure pinned local chart.js script is included alongside report.html
    val chartJsFile = File(reportsDir, "chart.js")
    if (!chartJsFile.exists()) {
        try {
            val chartJsContent = URI("https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js")
                .toURL()
                .openStream()
                .bufferedReader()
                .use { it.readText() }
            chartJsFile.writeText(chartJsContent)
        } catch (e: Exception) {
            println("Warning: Could not download chart.js locally: ${e.message}")
        }
    }

    val html = """
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="UTF-8">
        <title>Benchmark HTML Report</title>
        <script src="chart.js"></script>
        <style>
            body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; margin: 40px; background: #f8f9fa; color: #333; }
            .card { background: white; padding: 28px; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.08); max-width: 850px; margin: 0 auto 24px auto; }
            h1 { margin-top: 0; color: #111; font-size: 24px; }
            .score-container { display: flex; gap: 32px; margin: 16px 0; }
            .score-badge { font-size: 28px; font-weight: bold; color: #2563eb; }
            .ram-badge { font-size: 28px; font-weight: bold; color: #059669; }
            .label { font-size: 13px; color: #666; text-transform: uppercase; letter-spacing: 0.5px; }
            .sub { color: #666; font-size: 14px; margin-bottom: 24px; }
            canvas { max-width: 100%; margin-top: 16px; }
            .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; max-width: 850px; margin: 0 auto; }
        </style>
    </head>
    <body>
        <div class="card">
            <h1>🚀 ComposablePreviewScanner Benchmark Report</h1>
            <div class="sub">Benchmark: AndroidTestSourceSetBenchmark.benchmarkScanner</div>
            
            <div class="score-container">
                <div>
                    <div class="label">Average Execution Speed</div>
                    <div class="score-badge">${scoreFormatted} ms/op</div>
                </div>
                <div>
                    <div class="label">Average RAM Allocation</div>
                    <div class="ram-badge">${avgRamFormatted} MB</div>
                </div>
            </div>
        </div>

        <div class="grid">
            <div class="card">
                <h2>⚡ Execution Speed</h2>
                <canvas id="speedChart"></canvas>
            </div>
            <div class="card">
                <h2>💾 RAM Allocation</h2>
                <canvas id="ramChart"></canvas>
            </div>
        </div>

        <script>
            const labels = [${labels.joinToString(", ")}];

            // Speed Chart
            new Chart(document.getElementById('speedChart').getContext('2d'), {
                type: 'bar',
                data: {
                    labels: labels,
                    datasets: [{
                        label: 'Speed (ms)',
                        data: [${rawValues.joinToString(", ")}],
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

            // RAM Chart
            new Chart(document.getElementById('ramChart').getContext('2d'), {
                type: 'bar',
                data: {
                    labels: labels,
                    datasets: [{
                        label: 'RAM Allocated (MB)',
                        data: [${ramValues.joinToString(", ")}],
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
