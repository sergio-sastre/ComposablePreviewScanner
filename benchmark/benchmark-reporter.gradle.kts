import java.io.File
import java.net.URI
import java.util.Locale

// ==============================================================================
// CONFIGURATION THRESHOLDS WITH PERCENTAGE TOLERANCE OFFSET
// ==============================================================================
fun readNumericProperty(name: String, default: Double): Double {
    val raw = project.findProperty(name) as? String ?: return default
    return raw.toDoubleOrNull() ?: throw GradleException("Property '$name' must be a number, but was '$raw'.")
}

val tolerancePercent: Double = readNumericProperty("tolerancePercent", 5.0)

fun Double.fmt(): String = String.format(Locale.US, "%.2f", this)
fun Double.fmt1(): String = String.format(Locale.US, "%.1f", this)

data class BenchmarkThresholdConfig(
    val expectedScoreMs: Double,
    val maxAllowedScoreMs: Double,
    val expectedRamMb: Double,
    val maxAllowedRamMb: Double
)

enum class BenchmarkGroup(
    val id: String,
    val title: String,
    val description: String,
    val reportFileName: String,
    val reportTitle: String,
    val order: Int
) {
    BUILD_TIME("build-time", "⚡ Build Time Scanner", "Full classpath scanning across all packages", "report-buildtime.html", "Build Time Scanner Benchmark Report", 1),
    MAIN_SOURCE_SET("main-source-set", "📁 Main Source Set Scanner", "Targeted scanning of the main source set classpath", "report-mainsourceset.html", "Main Source Set Benchmark Report", 2);

    companion object {
        fun fromClassName(simpleClassName: String): BenchmarkGroup =
            if (simpleClassName.startsWith("MainSourceSet")) MAIN_SOURCE_SET else BUILD_TIME
    }
}

fun variantLabelFor(simpleClassName: String): String {
    val clean = simpleClassName.removeSuffix("Benchmark")
    val variantPart = clean
        .removePrefix("BuildTime")
        .removePrefix("MainSourceSet")
    return if (variantPart.isEmpty()) {
        "Include Private Previews"
    } else {
        variantPart.replace(Regex("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])"), " ")
    }
}

fun thresholdConfigFor(simpleClassName: String): BenchmarkThresholdConfig {
    val group = BenchmarkGroup.fromClassName(simpleClassName)
    val defaultScore = when (group) {
        BenchmarkGroup.MAIN_SOURCE_SET -> readNumericProperty("expectedScoreMs", 85.0)
        BenchmarkGroup.BUILD_TIME -> readNumericProperty("expectedBuildTimeScoreMs", 1500.0)
    }
    val defaultRam = when (group) {
        BenchmarkGroup.MAIN_SOURCE_SET -> readNumericProperty("expectedRamMb", 2.50)
        BenchmarkGroup.BUILD_TIME -> readNumericProperty("expectedBuildTimeRamMb", 210.0)
    }

    val propKey = simpleClassName.removeSuffix("Benchmark")
    val scorePropNames = buildList {
        add("expected${propKey}ScoreMs")
        if (simpleClassName == "MainSourceSetBenchmark") add("expectedScoreMs")
        if (simpleClassName.contains("Filter")) add("expectedBuildTimePreviewIterationScoreMs")
        if (simpleClassName.contains("Iteration")) add("expectedBuildTimePreviewFiltersScoreMs")
    }
    val ramPropNames = buildList {
        add("expected${propKey}RamMb")
        if (simpleClassName == "MainSourceSetBenchmark") add("expectedRamMb")
        if (simpleClassName.contains("Filter")) add("expectedBuildTimePreviewIterationRamMb")
        if (simpleClassName.contains("Iteration")) add("expectedBuildTimePreviewFiltersRamMb")
    }

    val expScore = scorePropNames.firstNotNullOfOrNull { (project.findProperty(it) as? String)?.toDoubleOrNull() }
        ?: defaultScore
    val expRam = ramPropNames.firstNotNullOfOrNull { (project.findProperty(it) as? String)?.toDoubleOrNull() }
        ?: defaultRam

    val maxScore = (project.findProperty("maxAllowed${propKey}ScoreMs") as? String)?.toDoubleOrNull()
        ?: (expScore * (1.0 + tolerancePercent / 100.0))
    val maxRam = (project.findProperty("maxAllowed${propKey}RamMb") as? String)?.toDoubleOrNull()
        ?: (expRam * (1.0 + tolerancePercent / 100.0))

    return BenchmarkThresholdConfig(expScore, maxScore, expRam, maxRam)
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
    val group: BenchmarkGroup get() = BenchmarkGroup.fromClassName(simpleClassName)
    val variantLabel: String get() = variantLabelFor(simpleClassName)
    val displayName: String get() = "$simpleClassName.$methodName"
    val speedPassed: Boolean get() = speedTrimmedMean <= thresholdConfig.maxAllowedScoreMs
    val ramPassed: Boolean get() = ramValues.isNotEmpty() && ramTrimmedMean <= thresholdConfig.maxAllowedRamMb
}

val generateBenchmarkHtmlReport = tasks.register("generateBenchmarkHtmlReport") {
    group = "Verification"
    description = "Generates an interactive Chart.js HTML report from the latest benchmark json results"
    dependsOn("mainBenchmark")
    onlyIf {
        val mainState = tasks.named("mainBenchmark").get().state
        val reportsDir = layout.buildDirectory.dir("reports/benchmarks").get().asFile
        (mainState.executed && mainState.failure == null) || findLatestJsonReport(reportsDir) != null
    }
    doLast {
        val reportsDir = layout.buildDirectory.dir("reports/benchmarks").get().asFile
        val jsonFile = findLatestJsonReport(reportsDir)
            ?: throw GradleException("No benchmark JSON report found in ${reportsDir.absolutePath}")
        buildHtmlReportFile(reportsDir, jsonFile)
    }
}

val verifyBenchmarkThreshold = tasks.register("verifyBenchmarkThreshold") {
    group = "Verification"
    description = "Fails the build if benchmark speed or allocated memory per scan exceeds configured thresholds"
    dependsOn("mainBenchmark")
    onlyIf {
        val mainState = tasks.named("mainBenchmark").get().state
        mainState.executed && mainState.failure == null
    }
    doLast {
        val reportsDir = layout.buildDirectory.dir("reports/benchmarks").get().asFile
        val jsonFile = findLatestJsonReport(reportsDir)
            ?: throw GradleException("No benchmark JSON report found in ${reportsDir.absolutePath}")
        checkPerformanceThreshold(jsonFile, reportsDir)
    }
}

afterEvaluate {
    listOf("mainBenchmarkCompile", "mainBenchmark").forEach { taskName ->
        tasks.named(taskName).configure {
            dependsOn(":tests:compileDebugKotlin", ":custompreviews:compileDebugKotlin")
        }
    }

    tasks.named("mainBenchmark").configure {
        doFirst {
            val reportsDir = layout.buildDirectory.dir("reports/benchmarks").get().asFile
            reportsDir.walkTopDown().filter { it.extension == "json" || it.name.startsWith("ram_") }.forEach { it.delete() }
        }
        finalizedBy(generateBenchmarkHtmlReport, verifyBenchmarkThreshold)
    }
}

// ==============================================================================
// HELPER FUNCTIONS
// ==============================================================================
fun findLatestJsonReport(reportsDir: File): File? =
    reportsDir.walkTopDown().filter { it.extension == "json" && it.length() > 10 }.maxByOrNull { it.lastModified() }

fun calculateTrimmedMean(values: List<Double>): Double =
    if (values.size <= 2) values.average() else values.sorted().subList(1, values.size - 1).average()

fun calculatePercentDiff(actual: Double, baseline: Double): String {
    if (baseline <= 0.0) return "N/A"
    val diff = ((actual - baseline) / baseline) * 100.0
    return "${if (diff >= 0.0) "+" else ""}${diff.fmt()}%"
}

fun parseBenchmarkEntries(jsonText: String, reportsDir: File): List<BenchmarkEntryResult> {
    val entryRegex = Regex(
        """"benchmark"\s*:\s*"([^"]+)"[\s\S]*?"primaryMetric"\s*:\s*\{[\s\S]*?"score"\s*:\s*([^,\s{}]+)[\s\S]*?"rawData"\s*:\s*\[\s*\[([\d.,\s]+)\]\s*\]"""
    )
    val matches = entryRegex.findAll(jsonText).toList().ifEmpty {
        throw GradleException("Invalid benchmark report: no benchmark entries with 'primaryMetric.score' found.")
    }

    return matches.map { match ->
        val fullName = match.groupValues[1]
        val parts = fullName.split(".")
        val methodName = parts.lastOrNull() ?: fullName
        val simpleClassName = if (parts.size >= 2) parts[parts.size - 2] else fullName

        val rawToken = match.groupValues[2].trim()
        val score = rawToken.takeUnless { it.startsWith("\"") }?.toDoubleOrNull()
            ?: throw GradleException("Invalid benchmark report: 'primaryMetric.score' is invalid: $rawToken")
        if (!score.isFinite()) {
            throw GradleException("Invalid benchmark report: 'primaryMetric.score' must be finite: $score")
        }

        val rawSpeedValues = match.groupValues[3].split(",").mapNotNull { it.trim().toDoubleOrNull() }
        val speedTrimmedMean = if (rawSpeedValues.isNotEmpty()) calculateTrimmedMean(rawSpeedValues) else score

        val ramFile = listOf("ram_usage_$simpleClassName.txt", "ram_usage.txt")
            .map { File(reportsDir, it) }
            .firstOrNull { it.exists() }

        val ramValues = ramFile?.readLines()
            ?.mapNotNull { it.trim().toDoubleOrNull() }
            ?.takeLast(rawSpeedValues.size.coerceAtLeast(1))
            ?: emptyList()

        BenchmarkEntryResult(
            fullName = fullName,
            simpleClassName = simpleClassName,
            methodName = methodName,
            score = score,
            rawSpeedValues = rawSpeedValues,
            speedTrimmedMean = speedTrimmedMean,
            ramValues = ramValues,
            avgRam = ramValues.takeIf { it.isNotEmpty() }?.average() ?: 0.0,
            ramTrimmedMean = if (ramValues.isNotEmpty()) calculateTrimmedMean(ramValues) else 0.0,
            thresholdConfig = thresholdConfigFor(simpleClassName)
        )
    }
}

fun checkPerformanceThreshold(jsonFile: File, reportsDir: File) {
    val entries = parseBenchmarkEntries(jsonFile.readText(), reportsDir)
    val errors = mutableListOf<String>()

    println("\n=======================================================")
    println("🔍 Performance Threshold Verification (Trimmed Mean - Baseline + ${tolerancePercent.fmt1()}% Offset):")

    entries.groupBy { it.group }.toSortedMap(compareBy { it.order }).forEach { (group, groupEntries) ->
        println("\n=======================================================")
        println("${group.title} (${group.description})")
        println("=======================================================")

        groupEntries.forEachIndexed { index, entry ->
            if (index > 0) println("-------------------------------------------------------")
            val cfg = entry.thresholdConfig
            if (entry.ramValues.isEmpty()) {
                throw GradleException("No valid allocated memory samples found for ${entry.displayName} in ${reportsDir.absolutePath}")
            }

            val speedDiff = calculatePercentDiff(entry.speedTrimmedMean, cfg.expectedScoreMs)
            val ramDiff = calculatePercentDiff(entry.ramTrimmedMean, cfg.expectedRamMb)

            println("📌 Benchmark: ${entry.displayName} [${entry.variantLabel}]")
            println("• Baseline Expected Speed: ${cfg.expectedScoreMs.fmt()} ms | Max Allowed (+${tolerancePercent.fmt1()}%): ${cfg.maxAllowedScoreMs.fmt()} ms")
            println("• Trimmed Actual Speed:   ${entry.speedTrimmedMean.fmt()} ms (Raw Avg: ${entry.score.fmt()} ms) [$speedDiff vs baseline] -> ${if (entry.speedPassed) "✅ PASSED" else "❌ FAILED"}")
            println("• Baseline Expected Alloc:${cfg.expectedRamMb.fmt()} MB | Max Allowed (+${tolerancePercent.fmt1()}%): ${cfg.maxAllowedRamMb.fmt()} MB")
            println("• Trimmed Actual Alloc:   ${entry.ramTrimmedMean.fmt()} MB (Raw Avg: ${entry.avgRam.fmt()} MB) [$ramDiff vs baseline] -> ${if (entry.ramPassed) "✅ PASSED" else "❌ FAILED"}")

            if (!entry.speedPassed) {
                errors.add("[${entry.displayName}] Trimmed speed (${entry.speedTrimmedMean.fmt()} ms, $speedDiff vs baseline ${cfg.expectedScoreMs.fmt()} ms) exceeded max allowed threshold of ${cfg.maxAllowedScoreMs.fmt()} ms")
            }
            if (!entry.ramPassed) {
                errors.add("[${entry.displayName}] Trimmed allocated memory per scan (${entry.ramTrimmedMean.fmt()} MB, $ramDiff vs baseline ${cfg.expectedRamMb.fmt()} MB) exceeded max allowed threshold of ${cfg.maxAllowedRamMb.fmt()} MB")
            }
        }
    }

    if (errors.isNotEmpty()) {
        println("\n=======================================================")
        println("❌ Benchmark FAILED! Threshold exceeded.")
        println("=======================================================")
        println(errors.joinToString("\n• ", prefix = "• "))
        println()
        throw GradleException("Benchmark Failed! ${errors.joinToString(" & ")}")
    } else {
        println("\n=======================================================")
        println("✅ All Performance Checks Passed!")
        println("=======================================================\n")
    }
}

fun renderReportHtml(
    entries: List<BenchmarkEntryResult>,
    chartJsScriptSrc: String,
    reportTitle: String,
    filterGroupId: String? = null
): String {
    val displayedEntries = if (filterGroupId != null) entries.filter { it.group.id == filterGroupId } else entries
    val grouped = displayedEntries.groupBy { it.group }.toList().sortedBy { it.first.order }
    val allPassed = displayedEntries.all { it.speedPassed && it.ramPassed }

    val sectionsHtml = StringBuilder()
    val chartsJs = StringBuilder()

    grouped.forEach { (group, groupEntries) ->
        val groupBannerClass = if (group == BenchmarkGroup.MAIN_SOURCE_SET) "group-banner group-main-source-set" else "group-banner"
        sectionsHtml.append(
            """
            <section class="benchmark-group-section" data-group="${group.id}">
                <div class="$groupBannerClass card">
                    <div class="group-banner-content">
                        <div>
                            <h2 class="group-title">${group.title}</h2>
                            <div class="group-desc">${group.description}</div>
                        </div>
                        <span class="group-count-badge">${groupEntries.size} ${if (groupEntries.size == 1) "benchmark" else "benchmarks"}</span>
                    </div>
                </div>
            """.trimIndent()
        ).append("\n")

        groupEntries.forEach { entry ->
            val idx = entries.indexOf(entry)
            val cfg = entry.thresholdConfig
            val speedDiff = calculatePercentDiff(entry.speedTrimmedMean, cfg.expectedScoreMs)
            val ramDiff = if (entry.ramValues.isEmpty()) "N/A" else calculatePercentDiff(entry.ramTrimmedMean, cfg.expectedRamMb)
            val ramFormatted = if (entry.ramValues.isEmpty()) "N/A" else "${entry.ramTrimmedMean.fmt()} MB"
            val labels = entry.rawSpeedValues.indices.map { "\"Iteration ${it + 1}\"" }.joinToString(", ")

            val variantClass = when {
                entry.variantLabel.contains("Without", ignoreCase = true) -> "variant-tag variant-no-private"
                entry.variantLabel.contains("Filter", ignoreCase = true) || entry.variantLabel.contains("Iteration", ignoreCase = true) -> "variant-tag variant-iteration"
                else -> "variant-tag"
            }

            sectionsHtml.append(
                """
                <div class="benchmark-entry">
                    <div class="card">
                        <div class="benchmark-header">
                            <div><h3 class="benchmark-name">${entry.displayName}</h3></div>
                            <span class="$variantClass">${entry.variantLabel}</span>
                        </div>
                        <div class="score-container">
                            <div>
                                <div class="label">Trimmed Speed (Olympic Mean)</div>
                                <div class="score-badge">${entry.speedTrimmedMean.fmt()} ms/op</div>
                                <div class="diff-tag ${if (entry.speedPassed) "diff-pass" else "diff-fail"}">$speedDiff vs baseline (${cfg.expectedScoreMs.fmt()} ms)</div>
                            </div>
                            <div>
                                <div class="label">Trimmed Allocated Memory (per scan)</div>
                                <div class="ram-badge">$ramFormatted</div>
                                <div class="diff-tag ${if (entry.ramPassed) "diff-pass" else "diff-fail"}">$ramDiff vs baseline (${cfg.expectedRamMb.fmt()} MB)</div>
                            </div>
                        </div>
                    </div>
                    <div class="grid">
                        <div class="card">
                            <h4>⚡ Execution Speed (${entry.simpleClassName})</h4>
                            <canvas id="speedChart_$idx"></canvas>
                        </div>
                        <div class="card">
                            <h4>💾 Allocated Memory per scan (${entry.simpleClassName})</h4>
                            <canvas id="ramChart_$idx"></canvas>
                        </div>
                    </div>
                </div>
                """.trimIndent()
            ).append("\n")

            chartsJs.append("makeBarChart('speedChart_$idx', 'Speed (ms)', [$labels], [${entry.rawSpeedValues.joinToString(", ")}], 'rgba(37, 99, 235, 0.6)', 'rgba(37, 99, 235, 1)', 'Time (ms)');\n")
            chartsJs.append("makeBarChart('ramChart_$idx', 'Allocated per scan (MB)', [$labels], [${entry.ramValues.joinToString(", ")}], 'rgba(5, 150, 105, 0.6)', 'rgba(5, 150, 105, 1)', 'Allocation (MB)');\n")
        }
        sectionsHtml.append("</section>\n")
    }

    val filterTabsHtml = if (filterGroupId == null) {
        val tabButtons = grouped.joinToString("\n") { (group, groupList) ->
            """<button class="filter-btn" onclick="filterGroup('${group.id}')">${group.title} (${groupList.size})</button>"""
        }
        """
        <div class="filter-tabs">
            <button class="filter-btn active" onclick="filterGroup('all')">All Benchmarks (${entries.size})</button>
            $tabButtons
        </div>
        """.trimIndent()
    } else ""

    return """
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>$reportTitle</title>
        <script src="$chartJsScriptSrc"></script>
        <style>
            body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; margin: 40px auto; background: #f8fafc; color: #1e293b; max-width: 900px; padding: 0 16px; }
            .card { background: white; padding: 24px 28px; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); border: 1px solid #e2e8f0; margin-bottom: 24px; }
            .header-card { border-top: 5px solid #2563eb; }
            .header-top { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; }
            h1 { margin: 0; color: #0f172a; font-size: 24px; font-weight: 700; }
            .header-subtitle { color: #64748b; font-size: 14px; margin: 6px 0 0 0; }
            .filter-tabs { display: flex; gap: 8px; flex-wrap: wrap; margin-top: 20px; border-top: 1px solid #f1f5f9; padding-top: 16px; }
            .filter-btn { background: #f1f5f9; border: 1px solid #cbd5e1; color: #475569; padding: 8px 14px; border-radius: 8px; cursor: pointer; font-weight: 600; font-size: 13px; transition: all 0.2s; }
            .filter-btn:hover { background: #e2e8f0; color: #0f172a; }
            .filter-btn.active { background: #2563eb; color: white; border-color: #2563eb; }
            .group-banner { margin-top: 36px; margin-bottom: 20px; border-left: 6px solid #2563eb; background: #f8faff; }
            .group-banner.group-main-source-set { border-left-color: #059669; background: #f0fdf4; }
            .group-banner-content { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; }
            .group-title { margin: 0; font-size: 19px; color: #0f172a; }
            .group-desc { margin-top: 4px; font-size: 13px; color: #64748b; }
            .group-count-badge { background: #e2e8f0; color: #334155; font-size: 12px; font-weight: 600; padding: 4px 10px; border-radius: 9999px; }
            .benchmark-entry { margin-bottom: 32px; }
            .benchmark-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 8px; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px solid #f1f5f9; }
            .benchmark-name { margin: 0; font-size: 16px; font-weight: 600; color: #1e293b; font-family: monospace; }
            .variant-tag { display: inline-block; font-size: 12px; font-weight: 600; padding: 3px 9px; border-radius: 6px; background: #eff6ff; color: #1d4ed8; border: 1px solid #bfdbfe; }
            .variant-tag.variant-no-private { background: #fef3c7; color: #92400e; border-color: #fde68a; }
            .variant-tag.variant-iteration { background: #f3e8ff; color: #6b21a8; border-color: #d8b4fe; }
            .score-container { display: flex; gap: 32px; flex-wrap: wrap; margin-top: 8px; }
            .score-badge { font-size: 26px; font-weight: 700; color: #2563eb; }
            .ram-badge { font-size: 26px; font-weight: 700; color: #059669; }
            .diff-tag { display: inline-block; font-size: 13px; font-weight: 600; padding: 4px 10px; border-radius: 6px; margin-top: 6px; }
            .diff-pass { background: #e6f4ea; color: #137333; }
            .diff-fail { background: #fce8e6; color: #c5221f; }
            .status-pill { font-size: 12px; font-weight: 700; padding: 6px 12px; border-radius: 9999px; }
            .status-pass { background: #dcfce7; color: #15803d; }
            .status-fail { background: #fee2e2; color: #b91c1c; }
            .label { font-size: 12px; color: #64748b; text-transform: uppercase; letter-spacing: 0.5px; font-weight: 600; }
            canvas { max-width: 100%; margin-top: 12px; }
            .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 24px; }
            @media (max-width: 700px) { .grid { grid-template-columns: 1fr; } }
            h4 { margin: 0; font-size: 14px; font-weight: 600; color: #475569; }
        </style>
    </head>
    <body>
        <div class="header-card card">
            <div class="header-top">
                <h1>🚀 $reportTitle</h1>
                <span class="status-pill ${if (allPassed) "status-pass" else "status-fail"}">
                    ${if (allPassed) "✅ ALL CHECKS PASSED" else "❌ THRESHOLDS EXCEEDED"}
                </span>
            </div>
            <p class="header-subtitle">Performance & Memory Allocation Verification</p>
            $filterTabsHtml
        </div>

        $sectionsHtml

        <script>
            function makeBarChart(id, label, labels, data, bg, border, yTitle) {
                new Chart(document.getElementById(id).getContext('2d'), {
                    type: 'bar',
                    data: { labels: labels, datasets: [{ label: label, data: data, backgroundColor: bg, borderColor: border, borderWidth: 2 }] },
                    options: { responsive: true, scales: { y: { beginAtZero: true, title: { display: true, text: yTitle } } } }
                });
            }

            $chartsJs

            function filterGroup(groupId) {
                document.querySelectorAll('.filter-btn').forEach(function(btn) { btn.classList.remove('active'); });
                if (window.event && window.event.target) window.event.target.classList.add('active');
                document.querySelectorAll('.benchmark-group-section').forEach(function(section) {
                    section.style.display = (groupId === 'all' || section.getAttribute('data-group') === groupId) ? 'block' : 'none';
                });
            }
        </script>
    </body>
    </html>
    """.trimIndent()
}

fun ensureLocalChartJs(reportsDir: File) {
    val file = File(reportsDir, "chart.js")
    if (!file.exists()) {
        runCatching {
            val conn = URI("https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js").toURL().openConnection()
            conn.connectTimeout = 10_000
            conn.readTimeout = 10_000
            file.writeText(conn.getInputStream().bufferedReader().use { it.readText() })
        }.onFailure { println("Warning: Could not download chart.js locally: ${it.message}") }
    }
}

fun buildHtmlReportFile(reportsDir: File, jsonFile: File) {
    val entries = parseBenchmarkEntries(jsonFile.readText(), reportsDir)
    ensureLocalChartJs(reportsDir)
    val chartJsScriptSrc = if (File(reportsDir, "chart.js").exists()) "chart.js" else "https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"

    val mainReport = File(reportsDir, "report.html").apply {
        writeText(renderReportHtml(entries, chartJsScriptSrc, "ComposablePreviewScanner Benchmark Report"))
    }

    println("\n=======================================================")
    println("📊 Local HTML Benchmark Report generated at:\nfile://${mainReport.absolutePath}")

    BenchmarkGroup.values().forEach { group ->
        if (entries.any { it.group == group }) {
            val groupReport = File(reportsDir, group.reportFileName).apply {
                writeText(renderReportHtml(entries, chartJsScriptSrc, group.reportTitle, filterGroupId = group.id))
            }
            println("file://${groupReport.absolutePath}")
        }
    }
    println("=======================================================\n")
}
