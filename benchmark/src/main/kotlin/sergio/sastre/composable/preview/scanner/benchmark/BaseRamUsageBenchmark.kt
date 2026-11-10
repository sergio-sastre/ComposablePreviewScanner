package sergio.sastre.composable.preview.scanner.benchmark

import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.TearDown
import java.io.File
import java.lang.management.ManagementFactory
import java.util.Locale

abstract class BaseRamUsageBenchmark {

    protected open val benchmarkName: String
        get() = this::class.java.simpleName.substringBefore("_jmhType")

    private var bytesBeforeIteration = 0L
    private var scansInIteration = 0L

    protected fun recordScan() {
        scansInIteration++
    }

    @Setup(Level.Trial)
    fun setupTrial() {
        listOf(
            "ram_usage_$benchmarkName.txt",
            "ram_per_iteration_$benchmarkName.txt"
        ).forEach {
            File("build/reports/benchmarks/$it").delete()
        }
    }

    @Setup(Level.Iteration)
    fun setupIteration() {
        val mxBean = ManagementFactory.getThreadMXBean()
        val threadMXBean = mxBean as? com.sun.management.ThreadMXBean
        bytesBeforeIteration = threadMXBean?.getThreadAllocatedBytes(Thread.currentThread().id) ?: 0L
        scansInIteration = 0L
    }

    @TearDown(Level.Iteration)
    fun tearDownIteration() {
        val mxBean = ManagementFactory.getThreadMXBean()
        val threadMXBean = mxBean as? com.sun.management.ThreadMXBean
        val bytesAfterIteration = threadMXBean?.getThreadAllocatedBytes(Thread.currentThread().id) ?: 0L

        if (bytesBeforeIteration > 0L && bytesAfterIteration > 0L && scansInIteration > 0L) {
            val allocatedMbPerIteration = (bytesAfterIteration - bytesBeforeIteration) / (1024.0 * 1024.0)
            recordRamUsage("ram_usage_$benchmarkName.txt", allocatedMbPerIteration / scansInIteration)
            recordRamUsage("ram_per_iteration_$benchmarkName.txt", allocatedMbPerIteration)
        }
    }

    private fun recordRamUsage(fileName: String, memMb: Double) {
        val ramFile = File("build/reports/benchmarks/$fileName")
        ramFile.parentFile?.mkdirs()
        ramFile.appendText("${String.format(Locale.US, "%.2f", memMb)}\n")
    }
}
