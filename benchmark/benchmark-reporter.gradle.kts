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

fun Double.fmt(): String = String.format(Locale.US, "%.2f", this)
fun Double.fmt1(): String = String.format(Locale.US, "%.1f", this)

val baselineDir: File = File(project.projectDir, "baseline")

data class BenchmarkThresholdConfig(
    val expectedScoreMs: Double,
    val improvementFloorScoreMs: Double,
    val maxAllowedScoreMs: Double,
    val expectedRamMb: Double,
    val improvementFloorRamMb: Double,
    val maxAllowedRamMb: Double,
    val tolerancePercent: Double
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

fun tolerancePercentFor(group: BenchmarkGroup, propKey: String): Double {
    val specific = (project.findProperty("tolerancePercent$propKey") as? String)?.toDoubleOrNull()
    if (specific != null) return specific

    val groupPropName = if (group == BenchmarkGroup.MAIN_SOURCE_SET) "tolerancePercentMainSourceSet" else "tolerancePercentBuildTime"
    val groupSpecific = (project.findProperty(groupPropName) as? String)?.toDoubleOrNull()
    if (groupSpecific != null) return groupSpecific

    val global = (project.findProperty("tolerancePercent") as? String)?.toDoubleOrNull()
    if (global != null) return global

    return when (group) {
        BenchmarkGroup.MAIN_SOURCE_SET -> 5.0
        BenchmarkGroup.BUILD_TIME -> 8.0
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
        BenchmarkGroup.BUILD_TIME -> readNumericProperty("expectedBuildTimeScoreMs", 1400.0)
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

    val tolerance = tolerancePercentFor(group, propKey)

    val floorScore = expScore * (1.0 - tolerance / 100.0)
    val maxScore = (project.findProperty("maxAllowed${propKey}ScoreMs") as? String)?.toDoubleOrNull()
        ?: (expScore * (1.0 + tolerance / 100.0))
    val floorRam = expRam * (1.0 - tolerance / 100.0)
    val maxRam = (project.findProperty("maxAllowed${propKey}RamMb") as? String)?.toDoubleOrNull()
        ?: (expRam * (1.0 + tolerance / 100.0))

    return BenchmarkThresholdConfig(expScore, floorScore, maxScore, expRam, floorRam, maxRam, tolerance)
}

data class BaselineComparison(
    val verdict: String,
    val badgeClass: String,
    val delta: Double,
    val percent: Double,
    val summary: String
)

data class MedianIqrResult(
    val median: Double,
    val q1: Double,
    val q3: Double,
    val iqr: Double
) {
    fun overlaps(other: MedianIqrResult): Boolean =
        maxOf(q1, other.q1) <= minOf(q3, other.q3)
}

data class BenchmarkEntryResult(
    val fullName: String,
    val simpleClassName: String,
    val methodName: String,
    val score: Double,
    val scoreError: Double,
    val rawSpeedValues: List<Double>,
    val speedMedian: Double,
    val speedIqr: MedianIqrResult,
    val ramValues: List<Double>,
    val avgRam: Double,
    val ramMedian: Double,
    val ramIqr: MedianIqrResult,
    val thresholdConfig: BenchmarkThresholdConfig
) {
    val group: BenchmarkGroup get() = BenchmarkGroup.fromClassName(simpleClassName)
    val variantLabel: String get() = variantLabelFor(simpleClassName)
    val displayName: String get() = "$simpleClassName.$methodName"
    val speedPassed: Boolean get() = speedMedian <= thresholdConfig.maxAllowedScoreMs
    val ramPassed: Boolean get() = ramValues.isNotEmpty() && ramMedian <= thresholdConfig.maxAllowedRamMb
}

val isSavingBaseline: Boolean
    get() = gradle.startParameter.taskNames.any { it.contains("saveBaseline") } ||
            project.providers.gradleProperty("saveBaseline").isPresent

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
        val reportsDir = layout.buildDirectory.dir("reports/benchmarks").get().asFile
        !isSavingBaseline && mainState.failure == null && findLatestJsonReport(reportsDir) != null
    }
    doLast {
        val reportsDir = layout.buildDirectory.dir("reports/benchmarks").get().asFile
        val jsonFile = findLatestJsonReport(reportsDir)
            ?: throw GradleException("No benchmark JSON report found in ${reportsDir.absolutePath}")
        checkPerformanceThreshold(jsonFile, reportsDir)
    }
}

fun currentGitInfo(): String = runCatching {
    val branch = java.lang.ProcessBuilder("git", "rev-parse", "--abbrev-ref", "HEAD")
        .directory(project.rootDir).start().inputStream.bufferedReader().readText().trim()
    val hash = java.lang.ProcessBuilder("git", "rev-parse", "--short", "HEAD")
        .directory(project.rootDir).start().inputStream.bufferedReader().readText().trim()
    "$branch@$hash"
}.getOrElse { "Saved Snapshot" }

val saveBaseline = tasks.register("saveBaseline") {
    group = "benchmark"
    description = "Saves the latest benchmark results and RAM metrics as the baseline for A/B comparison"
    dependsOn("benchmark")
    doLast {
        val reportsDir = layout.buildDirectory.dir("reports/benchmarks").get().asFile
        val jsonFile = findLatestJsonReport(reportsDir)
            ?: throw GradleException("No benchmark JSON report found to save as baseline.")
        baselineDir.mkdirs()
        jsonFile.copyTo(File(baselineDir, "main.json"), overwrite = true)
        reportsDir.listFiles { _, name -> name.startsWith("ram_") }?.forEach { file ->
            file.copyTo(File(baselineDir, file.name), overwrite = true)
        }
        val timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val gitInfo = currentGitInfo()
        File(baselineDir, "baseline-meta.txt").writeText("$gitInfo ($timestamp)")

        println("\n=======================================================")
        println("💾 Benchmark Baseline Snapshot saved successfully!")
        println("• Location: file://${baselineDir.absolutePath}")
        println("• Reference: $gitInfo ($timestamp)")
        println("• Run any benchmark now to see A/B comparison diffs.")
        println("=======================================================\n")
    }
}

val clearBaseline = tasks.register("clearBaseline") {
    group = "benchmark"
    description = "Clears the saved benchmark reference baseline"
    doLast {
        if (baselineDir.exists()) {
            baselineDir.deleteRecursively()
            println("\n🗑️ Benchmark baseline snapshot cleared.\n")
        } else {
            println("\nNo benchmark baseline snapshot found to clear.\n")
        }
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
fun calculateMedianAndIqr(values: List<Double>): MedianIqrResult {
    if (values.isEmpty()) return MedianIqrResult(0.0, 0.0, 0.0, 0.0)
    val sorted = values.sorted()
    fun percentile(p: Double): Double {
        val index = p * (sorted.size - 1)
        val lower = Math.floor(index).toInt()
        val upper = Math.ceil(index).toInt()
        val weight = index - lower
        return if (lower == upper) sorted[lower] else sorted[lower] * (1.0 - weight) + sorted[upper] * weight
    }
    val median = percentile(0.50)
    val q1 = percentile(0.25)
    val q3 = percentile(0.75)
    return MedianIqrResult(median, q1, q3, q3 - q1)
}

fun evaluateBaselineComparison(
    candidateIqr: MedianIqrResult,
    baselineIqr: MedianIqrResult,
    noiseTolerancePercent: Double,
    unit: String,
    lowerLabel: String,
    higherLabel: String
): BaselineComparison {
    val candidateMedian = candidateIqr.median
    val baselineMedian = baselineIqr.median
    val delta = candidateMedian - baselineMedian
    val percent = if (baselineMedian > 0.0) (delta / baselineMedian) * 100.0 else 0.0
    val absPercent = Math.abs(percent)
    val sign = if (percent >= 0.0) "+" else ""
    val iqrOverlaps = candidateIqr.overlaps(baselineIqr)

    return when {
        absPercent <= noiseTolerancePercent -> BaselineComparison(
            verdict = "➖ NEUTRAL ($sign${percent.fmt()}%, within ±${noiseTolerancePercent.fmt1()}% noise)",
            badgeClass = "diff-neutral",
            delta = delta,
            percent = percent,
            summary = "➖ $sign${percent.fmt()}% vs baseline ${baselineMedian.fmt()}$unit (Within ±${noiseTolerancePercent.fmt1()}% noise)"
        )
        delta < 0.0 && !iqrOverlaps -> BaselineComparison(
            verdict = "🚀 CLEAR IMPROVEMENT ($lowerLabel by ${percent.fmt()}% | IQR [${candidateIqr.q1.fmt()}..${candidateIqr.q3.fmt()}] < base [${baselineIqr.q1.fmt()}..${baselineIqr.q3.fmt()}])",
            badgeClass = "diff-pass",
            delta = delta,
            percent = percent,
            summary = "🚀 Clear Improvement: ${percent.fmt()}% vs baseline ${baselineMedian.fmt()}$unit (IQR [${candidateIqr.q1.fmt()}..${candidateIqr.q3.fmt()}] < [${baselineIqr.q1.fmt()}..${baselineIqr.q3.fmt()}])"
        )
        delta < 0.0 -> BaselineComparison(
            verdict = "🌱 POSSIBLE IMPROVEMENT ($lowerLabel by ${percent.fmt()}%, but IQRs overlap [${candidateIqr.q1.fmt()}..${candidateIqr.q3.fmt()}] ∩ [${baselineIqr.q1.fmt()}..${baselineIqr.q3.fmt()}])",
            badgeClass = "diff-possible-pass",
            delta = delta,
            percent = percent,
            summary = "🌱 Possible Improvement: ${percent.fmt()}% vs baseline ${baselineMedian.fmt()}$unit (IQRs overlap)"
        )
        !iqrOverlaps -> BaselineComparison(
            verdict = "❌ CLEAR REGRESSION ($higherLabel by +${percent.fmt()}% | IQR [${candidateIqr.q1.fmt()}..${candidateIqr.q3.fmt()}] > base [${baselineIqr.q1.fmt()}..${baselineIqr.q3.fmt()}])",
            badgeClass = "diff-fail",
            delta = delta,
            percent = percent,
            summary = "❌ Clear Regression: +${percent.fmt()}% vs baseline ${baselineMedian.fmt()}$unit (IQR [${candidateIqr.q1.fmt()}..${candidateIqr.q3.fmt()}] > [${baselineIqr.q1.fmt()}..${baselineIqr.q3.fmt()}])"
        )
        else -> BaselineComparison(
            verdict = "⚠️ POSSIBLE REGRESSION ($higherLabel by +${percent.fmt()}%, but IQRs overlap [${candidateIqr.q1.fmt()}..${candidateIqr.q3.fmt()}] ∩ [${baselineIqr.q1.fmt()}..${baselineIqr.q3.fmt()}])",
            badgeClass = "diff-possible-fail",
            delta = delta,
            percent = percent,
            summary = "⚠️ Possible Regression: +${percent.fmt()}% vs baseline ${baselineMedian.fmt()}$unit (IQRs overlap)"
        )
    }
}

fun findLatestJsonReport(reportsDir: File): File? =
    reportsDir.walkTopDown().filter { it.extension == "json" && it.length() > 10 }.maxByOrNull { it.lastModified() }

fun calculatePercentDiff(actual: Double, baseline: Double): String {
    if (baseline <= 0.0) return "N/A"
    val diff = ((actual - baseline) / baseline) * 100.0
    return "${if (diff >= 0.0) "+" else ""}${diff.fmt()}%"
}

fun loadBaselineEntries(): Pair<String?, Map<String, BenchmarkEntryResult>> {
    val jsonFile = File(baselineDir, "main.json")
    if (!jsonFile.exists() || (project.findProperty("compareBaseline") as? String) == "false") {
        return null to emptyMap()
    }
    val metaFile = File(baselineDir, "baseline-meta.txt")
    val meta = if (metaFile.exists()) metaFile.readText().trim() else "Saved Baseline"
    val entries = runCatching {
        parseBenchmarkEntries(jsonFile.readText(), baselineDir).associateBy { it.simpleClassName }
    }.getOrElse { emptyMap() }
    return meta to entries
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

        val scoreError = Regex(""""scoreError"\s*:\s*([^,\s{}]+)""").find(match.value)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0

        val rawSpeedValues = match.groupValues[3].split(",").mapNotNull { it.trim().toDoubleOrNull() }
        val speedIqrResult = calculateMedianAndIqr(rawSpeedValues)
        val speedMedian = if (rawSpeedValues.isNotEmpty()) speedIqrResult.median else score

        val ramFile = listOf("ram_usage_$simpleClassName.txt", "ram_usage.txt")
            .map { File(reportsDir, it) }
            .firstOrNull { it.exists() }

        val ramValues = ramFile?.readLines()
            ?.mapNotNull { it.trim().toDoubleOrNull() }
            ?.takeLast(rawSpeedValues.size.coerceAtLeast(1))
            ?: emptyList()

        val ramIqrResult = calculateMedianAndIqr(ramValues)
        val ramMedian = if (ramValues.isNotEmpty()) ramIqrResult.median else 0.0

        BenchmarkEntryResult(
            fullName = fullName,
            simpleClassName = simpleClassName,
            methodName = methodName,
            score = score,
            scoreError = scoreError,
            rawSpeedValues = rawSpeedValues,
            speedMedian = speedMedian,
            speedIqr = speedIqrResult,
            ramValues = ramValues,
            avgRam = ramValues.takeIf { it.isNotEmpty() }?.average() ?: 0.0,
            ramMedian = ramMedian,
            ramIqr = ramIqrResult,
            thresholdConfig = thresholdConfigFor(simpleClassName)
        )
    }
}

fun checkPerformanceThreshold(jsonFile: File, reportsDir: File) {
    val entries = parseBenchmarkEntries(jsonFile.readText(), reportsDir)
    val (baselineMeta, baselineMap) = loadBaselineEntries()
    val errors = mutableListOf<String>()

    val speedNoisePercent = readNumericProperty("speedNoisePercent", 5.0)
    val ramNoisePercent = readNumericProperty("ramNoisePercent", 3.0)

    println("\n=======================================================")
    println("🔍 Performance Threshold Verification (Median & IQR vs Static Ceiling):")
    if (baselineMeta != null) {
        println("⚖️ Active A/B Comparison vs Saved Baseline Snapshot: $baselineMeta")
    }

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

            val speedDiff = calculatePercentDiff(entry.speedMedian, cfg.expectedScoreMs)
            val ramDiff = calculatePercentDiff(entry.ramMedian, cfg.expectedRamMb)
            val speedMargin = cfg.maxAllowedScoreMs - entry.speedMedian
            val ramMargin = cfg.maxAllowedRamMb - entry.ramMedian

            val speedStatus = when {
                !entry.speedPassed -> "❌ FAILS BUILD (exceeded ceiling of ${cfg.maxAllowedScoreMs.fmt()} ms by +${(-speedMargin).fmt()} ms)"
                entry.speedMedian < cfg.improvementFloorScoreMs -> "🚀 POSSIBLE IMPROVEMENT (< ${cfg.improvementFloorScoreMs.fmt()} ms floor | safe margin: +${speedMargin.fmt()} ms)"
                else -> "✅ WITHIN TARGET BAND (fails if > ${cfg.maxAllowedScoreMs.fmt()} ms | safe margin: +${speedMargin.fmt()} ms)"
            }
            val ramStatus = when {
                !entry.ramPassed -> "❌ FAILS BUILD (exceeded ceiling of ${cfg.maxAllowedRamMb.fmt()} MB by +${(-ramMargin).fmt()} MB)"
                entry.ramMedian < cfg.improvementFloorRamMb -> "🚀 POSSIBLE IMPROVEMENT (< ${cfg.improvementFloorRamMb.fmt()} MB floor | safe margin: +${ramMargin.fmt()} MB)"
                else -> "✅ WITHIN TARGET BAND (fails if > ${cfg.maxAllowedRamMb.fmt()} MB | safe margin: +${ramMargin.fmt()} MB)"
            }

            println("📌 Benchmark: ${entry.displayName} [${entry.variantLabel}]")
            println("• Speed Threshold: Target = ${cfg.expectedScoreMs.fmt()} ms (±${cfg.tolerancePercent.fmt1()}%) | Improvement < ${cfg.improvementFloorScoreMs.fmt()} ms | Fail > ${cfg.maxAllowedScoreMs.fmt()} ms")
            println("• Actual Speed:    ${entry.speedMedian.fmt()} ms (IQR: [${entry.speedIqr.q1.fmt()}..${entry.speedIqr.q3.fmt()}] ms | Raw Avg: ${entry.score.fmt()} ms ± ${entry.scoreError.fmt()} ms) [$speedDiff vs target] -> $speedStatus")
            println("• Alloc Threshold: Target = ${cfg.expectedRamMb.fmt()} MB (±${cfg.tolerancePercent.fmt1()}%) | Improvement < ${cfg.improvementFloorRamMb.fmt()} MB | Fail > ${cfg.maxAllowedRamMb.fmt()} MB")
            println("• Actual Alloc:    ${entry.ramMedian.fmt()} MB (IQR: [${entry.ramIqr.q1.fmt()}..${entry.ramIqr.q3.fmt()}] MB | Raw Avg: ${entry.avgRam.fmt()} MB) [$ramDiff vs target] -> $ramStatus")

            val baselineEntry = baselineMap[entry.simpleClassName]
            if (baselineEntry != null) {
                val speedComp = evaluateBaselineComparison(
                    candidateIqr = entry.speedIqr,
                    baselineIqr = baselineEntry.speedIqr,
                    noiseTolerancePercent = speedNoisePercent,
                    unit = " ms",
                    lowerLabel = "FASTER",
                    higherLabel = "SLOWER"
                )

                val ramComp = evaluateBaselineComparison(
                    candidateIqr = entry.ramIqr,
                    baselineIqr = baselineEntry.ramIqr,
                    noiseTolerancePercent = ramNoisePercent,
                    unit = " MB",
                    lowerLabel = "LOWER",
                    higherLabel = "HIGHER"
                )

                println("  ↳ ⚖️ A/B Diff vs Baseline ($baselineMeta):")
                println("     Speed: ${baselineEntry.speedMedian.fmt()} ms ──> ${entry.speedMedian.fmt()} ms  [${speedComp.verdict}]")
                println("     Alloc: ${baselineEntry.ramMedian.fmt()} MB ──> ${entry.ramMedian.fmt()} MB  [${ramComp.verdict}]")
            }

            if (!entry.speedPassed) {
                errors.add("[${entry.displayName}] Median speed (${entry.speedMedian.fmt()} ms, $speedDiff vs static target ${cfg.expectedScoreMs.fmt()} ms) exceeded max allowed ceiling of ${cfg.maxAllowedScoreMs.fmt()} ms")
            }
            if (!entry.ramPassed) {
                errors.add("[${entry.displayName}] Median allocated memory per scan (${entry.ramMedian.fmt()} MB, $ramDiff vs static target ${cfg.expectedRamMb.fmt()} MB) exceeded max allowed ceiling of ${cfg.maxAllowedRamMb.fmt()} MB")
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
    filterGroupId: String? = null,
    baselineMap: Map<String, BenchmarkEntryResult> = emptyMap(),
    baselineMeta: String? = null
): String {
    val displayedEntries = if (filterGroupId != null) entries.filter { it.group.id == filterGroupId } else entries
    val grouped = displayedEntries.groupBy { it.group }.toList().sortedBy { it.first.order }
    val allPassed = displayedEntries.all { it.speedPassed && it.ramPassed }

    val speedNoisePercent = readNumericProperty("speedNoisePercent", 5.0)
    val ramNoisePercent = readNumericProperty("ramNoisePercent", 3.0)

    val sectionsHtml = StringBuilder()
    val chartsJs = StringBuilder()

    grouped.forEach { (group, groupEntries) ->
        val groupBannerClass = if (group == BenchmarkGroup.MAIN_SOURCE_SET) "group-main-source-set" else "group-build-time"
        sectionsHtml.append(
            """
            <section class="benchmark-group-section card $groupBannerClass" data-group="${group.id}">
                <div class="group-header">
                    <div>
                        <h2 class="group-title">${group.title}</h2>
                        <div class="group-desc">${group.description}</div>
                    </div>
                    <span class="group-count-badge">${groupEntries.size} ${if (groupEntries.size == 1) "benchmark" else "benchmarks"}</span>
                </div>
            """.trimIndent()
        ).append("\n")

        groupEntries.forEach { entry ->
            val idx = entries.indexOf(entry)
            val cfg = entry.thresholdConfig
            val speedDiff = calculatePercentDiff(entry.speedMedian, cfg.expectedScoreMs)
            val ramDiff = if (entry.ramValues.isEmpty()) "N/A" else calculatePercentDiff(entry.ramMedian, cfg.expectedRamMb)
            val ramFormatted = if (entry.ramValues.isEmpty()) "N/A" else "${entry.ramMedian.fmt()} MB"
            val labels = entry.rawSpeedValues.indices.map { "\"Iteration ${it + 1}\"" }.joinToString(", ")

            val variantClass = when {
                entry.variantLabel.contains("Filter", ignoreCase = true) || entry.variantLabel.contains("Iteration", ignoreCase = true) -> "variant-tag variant-iteration"
                else -> "variant-tag"
            }

            val baselineEntry = baselineMap[entry.simpleClassName]

            val speedComp = baselineEntry?.let {
                evaluateBaselineComparison(
                    candidateIqr = entry.speedIqr,
                    baselineIqr = it.speedIqr,
                    noiseTolerancePercent = speedNoisePercent,
                    unit = " ms",
                    lowerLabel = "FASTER",
                    higherLabel = "SLOWER"
                )
            }
            val ramComp = baselineEntry?.let {
                evaluateBaselineComparison(
                    candidateIqr = entry.ramIqr,
                    baselineIqr = it.ramIqr,
                    noiseTolerancePercent = ramNoisePercent,
                    unit = " MB",
                    lowerLabel = "LOWER",
                    higherLabel = "HIGHER"
                )
            }

            val speedMargin = cfg.maxAllowedScoreMs - entry.speedMedian
            val ramMargin = cfg.maxAllowedRamMb - entry.ramMedian

            val speedCeilingSubtext = when {
                !entry.speedPassed -> "❌ Exceeded failure limit of ${cfg.maxAllowedScoreMs.fmt()} ms by +${(-speedMargin).fmt()} ms"
                entry.speedMedian < cfg.improvementFloorScoreMs -> "🚀 Faster than ${cfg.improvementFloorScoreMs.fmt()} ms improvement floor (fails if > ${cfg.maxAllowedScoreMs.fmt()} ms)"
                else -> "Target: ${cfg.expectedScoreMs.fmt()} ms | Improvement < ${cfg.improvementFloorScoreMs.fmt()} ms | Fails if > ${cfg.maxAllowedScoreMs.fmt()} ms (margin: +${speedMargin.fmt()} ms)"
            }

            val ramCeilingSubtext = when {
                !entry.ramPassed -> "❌ Exceeded failure limit of ${cfg.maxAllowedRamMb.fmt()} MB by +${(-ramMargin).fmt()} MB"
                entry.ramMedian < cfg.improvementFloorRamMb -> "🚀 Lower than ${cfg.improvementFloorRamMb.fmt()} MB improvement floor (fails if > ${cfg.maxAllowedRamMb.fmt()} MB)"
                else -> "Target: ${cfg.expectedRamMb.fmt()} MB | Improvement < ${cfg.improvementFloorRamMb.fmt()} MB | Fails if > ${cfg.maxAllowedRamMb.fmt()} MB (margin: +${ramMargin.fmt()} MB)"
            }

            val speedStaticBadge = when {
                !entry.speedPassed -> "diff-fail" to "❌ $speedDiff vs static target (${cfg.expectedScoreMs.fmt()} ms)"
                entry.speedMedian < cfg.improvementFloorScoreMs -> "diff-pass" to "🚀 Possible Improvement: $speedDiff vs static target (${cfg.expectedScoreMs.fmt()} ms)"
                else -> "diff-neutral" to "✅ $speedDiff vs static target (${cfg.expectedScoreMs.fmt()} ms)"
            }

            val ramStaticBadge = when {
                !entry.ramPassed -> "diff-fail" to "❌ $ramDiff vs static target (${cfg.expectedRamMb.fmt()} MB)"
                entry.ramMedian < cfg.improvementFloorRamMb -> "diff-pass" to "🚀 Possible Improvement: $ramDiff vs static target (${cfg.expectedRamMb.fmt()} MB)"
                else -> "diff-neutral" to "✅ $ramDiff vs static target (${cfg.expectedRamMb.fmt()} MB)"
            }

            val speedBadgeHtml = if (baselineEntry != null && speedComp != null) {
                """
                <div class="diff-tag ${speedComp.badgeClass}">${speedComp.summary}</div>
                <div class="threshold-subtext">$speedCeilingSubtext</div>
                """.trimIndent()
            } else {
                """
                <div class="diff-tag ${speedStaticBadge.first}">${speedStaticBadge.second}</div>
                <div class="threshold-subtext">$speedCeilingSubtext</div>
                """.trimIndent()
            }

            val ramBadgeHtml = if (baselineEntry != null && ramComp != null) {
                """
                <div class="diff-tag ${ramComp.badgeClass}">${ramComp.summary}</div>
                <div class="threshold-subtext">$ramCeilingSubtext</div>
                """.trimIndent()
            } else {
                """
                <div class="diff-tag ${ramStaticBadge.first}">${ramStaticBadge.second}</div>
                <div class="threshold-subtext">$ramCeilingSubtext</div>
                """.trimIndent()
            }

            sectionsHtml.append(
                """
                <div class="benchmark-item">
                    <div class="benchmark-header">
                        <div><h3 class="benchmark-name">${entry.displayName}</h3></div>
                        <span class="$variantClass">${entry.variantLabel}</span>
                    </div>
                    <div class="grid">
                        <div class="metric-card">
                            <h4>⚡ Execution Speed</h4>
                            <div class="metric-summary">
                                <div class="label">Median Speed (IQR: [${entry.speedIqr.q1.fmt()}..${entry.speedIqr.q3.fmt()}] ms)</div>
                                <div class="score-badge">${entry.speedMedian.fmt()} ms/op</div>
                                $speedBadgeHtml
                            </div>
                            <canvas id="speedChart_$idx"></canvas>
                        </div>
                        <div class="metric-card">
                            <h4>💾 Allocated Memory per scan</h4>
                            <div class="metric-summary">
                                <div class="label">Median Allocated Memory (IQR: [${entry.ramIqr.q1.fmt()}..${entry.ramIqr.q3.fmt()}] MB)</div>
                                <div class="ram-badge">$ramFormatted</div>
                                $ramBadgeHtml
                            </div>
                            <canvas id="ramChart_$idx"></canvas>
                        </div>
                    </div>
                </div>
                """.trimIndent()
            ).append("\n")

            if (baselineEntry != null) {
                chartsJs.append("makeBarChartWithBaseline('speedChart_$idx', 'Speed (ms)', [$labels], [${entry.rawSpeedValues.joinToString(", ")}], 'rgba(37, 99, 235, 0.6)', 'rgba(37, 99, 235, 1)', 'Time (ms)', ${baselineEntry.speedMedian.fmt()});\n")
                chartsJs.append("makeBarChartWithBaseline('ramChart_$idx', 'Allocated per scan (MB)', [$labels], [${entry.ramValues.joinToString(", ")}], 'rgba(5, 150, 105, 0.6)', 'rgba(5, 150, 105, 1)', 'Allocation (MB)', ${baselineEntry.ramMedian.fmt()});\n")
            } else {
                chartsJs.append("makeBarChart('speedChart_$idx', 'Speed (ms)', [$labels], [${entry.rawSpeedValues.joinToString(", ")}], 'rgba(37, 99, 235, 0.6)', 'rgba(37, 99, 235, 1)', 'Time (ms)');\n")
                chartsJs.append("makeBarChart('ramChart_$idx', 'Allocated per scan (MB)', [$labels], [${entry.ramValues.joinToString(", ")}], 'rgba(5, 150, 105, 0.6)', 'rgba(5, 150, 105, 1)', 'Allocation (MB)');\n")
            }
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

    val baselineHeaderBanner = if (baselineMeta != null) {
        """<div class="baseline-banner">⚖️ A/B Comparison Active — Baseline Reference: <strong>$baselineMeta</strong></div>"""
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
            .baseline-banner { margin-top: 12px; font-size: 13px; color: #3730a3; background: #e0e7ff; padding: 8px 14px; border-radius: 8px; border: 1px solid #c7d2fe; display: inline-block; }
            .filter-tabs { display: flex; gap: 8px; flex-wrap: wrap; margin-top: 20px; border-top: 1px solid #f1f5f9; padding-top: 16px; }
            .filter-btn { background: #f1f5f9; border: 1px solid #cbd5e1; color: #475569; padding: 8px 14px; border-radius: 8px; cursor: pointer; font-weight: 600; font-size: 13px; transition: all 0.2s; }
            .filter-btn:hover { background: #e2e8f0; color: #0f172a; }
            .filter-btn.active { background: #2563eb; color: white; border-color: #2563eb; }
            .benchmark-group-section { margin-top: 32px; margin-bottom: 36px; border-radius: 16px; border-top: 4px solid #2563eb; }
            .benchmark-group-section.group-main-source-set { border-top-color: #059669; }
            .group-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; padding-bottom: 18px; border-bottom: 1px solid #e2e8f0; margin-bottom: 24px; }
            .group-title { margin: 0; font-size: 20px; font-weight: 700; color: #0f172a; }
            .group-desc { margin-top: 4px; font-size: 13px; color: #64748b; }
            .group-count-badge { background: #f1f5f9; color: #334155; font-size: 12px; font-weight: 600; padding: 4px 12px; border-radius: 9999px; border: 1px solid #e2e8f0; }
            .benchmark-item { margin-bottom: 8px; }
            .benchmark-item + .benchmark-item { margin-top: 36px; padding-top: 28px; border-top: 1px dashed #cbd5e1; }
            .benchmark-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 8px; margin-bottom: 18px; }
            .benchmark-name { margin: 0; font-size: 17px; font-weight: 700; color: #0f172a; font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace; }
            .variant-tag { display: inline-block; font-size: 12px; font-weight: 600; padding: 4px 12px; border-radius: 9999px; background: #eff6ff; color: #1d4ed8; border: 1px solid #bfdbfe; }
            .variant-tag.variant-iteration { background: #f3e8ff; color: #6b21a8; border-color: #d8b4fe; }
            .metric-card { background: #f8fafc; padding: 20px; border-radius: 12px; border: 1px solid #e2e8f0; display: flex; flex-direction: column; justify-content: space-between; margin-bottom: 0; transition: box-shadow 0.2s ease; }
            .metric-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.04); }
            .metric-summary { margin-top: 14px; margin-bottom: 16px; }
            .score-badge { font-size: 28px; font-weight: 800; color: #2563eb; margin-top: 4px; letter-spacing: -0.5px; }
            .ram-badge { font-size: 28px; font-weight: 800; color: #059669; margin-top: 4px; letter-spacing: -0.5px; }
            .diff-tag { display: inline-block; font-size: 12px; font-weight: 600; padding: 4px 10px; border-radius: 6px; margin-top: 8px; line-height: 1.4; }
            .diff-pass { background: #dcfce7; color: #166534; border: 1px solid #bbf7d0; }
            .diff-possible-pass { background: #ecfdf5; color: #047857; border: 1px solid #a7f3d0; }
            .diff-possible-fail { background: #fef3c7; color: #92400e; border: 1px solid #fde68a; }
            .diff-fail { background: #fee2e2; color: #991b1b; border: 1px solid #fecaca; }
            .diff-neutral { background: #ffffff; color: #334155; border: 1px solid #cbd5e1; }
            .threshold-subtext { font-size: 11px; color: #64748b; margin-top: 6px; font-weight: 500; }
            .status-pill { font-size: 12px; font-weight: 700; padding: 6px 14px; border-radius: 9999px; }
            .status-pass { background: #dcfce7; color: #15803d; border: 1px solid #bbf7d0; }
            .status-fail { background: #fee2e2; color: #b91c1c; border: 1px solid #fecaca; }
            .label { font-size: 11px; color: #64748b; text-transform: uppercase; letter-spacing: 0.6px; font-weight: 700; }
            canvas { max-width: 100%; margin-top: 16px; background: white; padding: 12px; border-radius: 10px; border: 1px solid #e2e8f0; box-shadow: 0 1px 3px rgba(0,0,0,0.02); }
            .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 0px; }
            @media (max-width: 700px) { .grid { grid-template-columns: 1fr; } }
            h4 { margin: 0; font-size: 15px; font-weight: 700; color: #1e293b; border-bottom: 1px solid #e2e8f0; padding-bottom: 10px; display: flex; align-items: center; gap: 6px; }
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
            $baselineHeaderBanner
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

            function makeBarChartWithBaseline(id, label, labels, data, bg, border, yTitle, baselineVal) {
                new Chart(document.getElementById(id).getContext('2d'), {
                    type: 'bar',
                    data: {
                        labels: labels,
                        datasets: [
                            { label: label, data: data, backgroundColor: bg, borderColor: border, borderWidth: 2 },
                            { label: 'Baseline Median (' + baselineVal + ')', data: Array(labels.length).fill(baselineVal), type: 'line', borderColor: 'rgba(220, 38, 38, 0.85)', borderDash: [6, 4], pointRadius: 0, borderWidth: 2 }
                        ]
                    },
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
    val (baselineMeta, baselineMap) = loadBaselineEntries()
    ensureLocalChartJs(reportsDir)
    val chartJsScriptSrc = if (File(reportsDir, "chart.js").exists()) "chart.js" else "https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"

    val mainReport = File(reportsDir, "report.html").apply {
        writeText(renderReportHtml(entries, chartJsScriptSrc, "ComposablePreviewScanner Benchmark Report", baselineMap = baselineMap, baselineMeta = baselineMeta))
    }

    println("\n=======================================================")
    println("📊 Local HTML Benchmark Report generated at:\nfile://${mainReport.absolutePath}")

    BenchmarkGroup.values().forEach { group ->
        if (entries.any { it.group == group }) {
            val groupReport = File(reportsDir, group.reportFileName).apply {
                writeText(renderReportHtml(entries, chartJsScriptSrc, group.reportTitle, filterGroupId = group.id, baselineMap = baselineMap, baselineMeta = baselineMeta))
            }
            println("file://${groupReport.absolutePath}")
        }
    }
    println("=======================================================\n")
}
