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
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.core.annotations.RequiresShowStandardStreams
import sergio.sastre.composable.preview.scanner.core.scanresult.RequiresLargeHeap

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 2, timeUnit = BenchmarkTimeUnit.SECONDS)
@Measurement(iterations = 10, time = 2, timeUnit = BenchmarkTimeUnit.SECONDS)
open class BuildTimeWithoutPrivatePreviewsBenchmark : BaseRamUsageBenchmark() {

    override val benchmarkName: String = "BuildTimeWithoutPrivatePreviewsBenchmark"

    @OptIn(RequiresShowStandardStreams::class, RequiresLargeHeap::class)
    @Benchmark
    fun benchmarkScanner(): List<*> {
        recordScan()

        return AndroidComposablePreviewScanner()
            .enableScanningLogs()
            .scanAllPackages()
            .getPreviews()
    }
}
