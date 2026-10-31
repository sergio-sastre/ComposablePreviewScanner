package sergio.sastre.composable.preview.scanner.benchmark

import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.BenchmarkMode
import kotlinx.benchmark.BenchmarkTimeUnit
import kotlinx.benchmark.Measurement
import kotlinx.benchmark.Mode
import kotlinx.benchmark.OutputTimeUnit
import kotlinx.benchmark.Scope
import kotlinx.benchmark.State
import kotlinx.benchmark.Warmup
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.TearDown
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.core.annotations.RequiresShowStandardStreams
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.Classpath
import sergio.sastre.composable.preview.scanner.core.scanner.config.classpath.SourceSet
import sergio.sastre.composable.preview.scanner.core.scanresult.RequiresLargeHeap
import java.io.File
import java.lang.management.ManagementFactory
import java.util.Locale

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
open class AndroidTestSourceSetBenchmark {

    private var bytesBeforeIteration = 0L
    private var invocationsInIteration = 0L

    @Setup(Level.Trial)
    fun setupTrial() {
        val ramFile = File("build/reports/benchmarks/ram_usage.txt")
        if (ramFile.exists()) {
            ramFile.delete()
        }
    }

    @Setup(Level.Iteration)
    fun setupIteration() {
        invocationsInIteration = 0L
        val mxBean = ManagementFactory.getThreadMXBean()
        val threadMXBean = mxBean as? com.sun.management.ThreadMXBean
        bytesBeforeIteration = threadMXBean?.getThreadAllocatedBytes(Thread.currentThread().id) ?: 0L
    }

    @OptIn(RequiresShowStandardStreams::class, RequiresLargeHeap::class)
    @Benchmark
    fun benchmarkScanner(): List<*> {
        invocationsInIteration++
        val testsModuleBuildDir = File(".").canonicalFile.parentFile.resolve("tests/build").absolutePath

        return AndroidComposablePreviewScanner()
            .enableScanningLogs()
            .setTargetSourceSet(
                sourceSetClasspath = Classpath(SourceSet.ANDROID_TEST).copy(rootDir = testsModuleBuildDir),
                packageTreesOfCrossModuleCustomPreviews = listOf("sergio.sastre.composable.preview.custompreviews")
            )
            .scanAllPackages()
            .includePrivatePreviews()
            .getPreviews()
    }

    @TearDown(Level.Iteration)
    fun tearDownIteration() {
        val mxBean = ManagementFactory.getThreadMXBean()
        val threadMXBean = mxBean as? com.sun.management.ThreadMXBean
        val bytesAfterIteration = threadMXBean?.getThreadAllocatedBytes(Thread.currentThread().id) ?: 0L

        if (bytesBeforeIteration > 0L && bytesAfterIteration > 0L) {
            val totalAllocatedBytes = bytesAfterIteration - bytesBeforeIteration
            if (totalAllocatedBytes > 0L && invocationsInIteration > 0L) {
                val bytesPerInvocation = totalAllocatedBytes.toDouble() / invocationsInIteration
                val allocatedMb = bytesPerInvocation / (1024.0 * 1024.0)
                recordRamUsage(allocatedMb)
            }
        }
    }

    private fun recordRamUsage(memMb: Double) {
        val ramFile = File("build/reports/benchmarks/ram_usage.txt")
        ramFile.parentFile?.mkdirs()
        ramFile.appendText("${String.format(Locale.US, "%.2f", memMb)}\n")
    }
}
