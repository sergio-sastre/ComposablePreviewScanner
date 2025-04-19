package sergio.sastre.composable.preview.scanner.utils

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class SystemOutputTestRule : TestRule {
    private val standardOut = System.out
    private val outputStreamCaptor = ByteArrayOutputStream()

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                clearOutput()
                try {
                    System.setOut(PrintStream(outputStreamCaptor))
                    base.evaluate()
                } finally {
                    System.setOut(standardOut)
                    clearOutput()
                }
            }
        }
    }

    val systemOutput: String
        get() = outputStreamCaptor.toString()

    private fun clearOutput() {
        outputStreamCaptor.reset()
    }
}