package org.example.altered

import org.jetbrains.kotlinx.lincheck.ExceptionResult
import org.jetbrains.kotlinx.lincheck.ExperimentalModelCheckingAPI
import org.jetbrains.kotlinx.lincheck.LincheckAssertionError
import org.jetbrains.kotlinx.lincheck.execution.threadsResults
import kotlin.test.Test
import org.jetbrains.kotlinx.lincheck.runConcurrentTest
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource


abstract class RunCheckerBase {
    abstract fun block()
    @OptIn(ExperimentalModelCheckingAPI::class)
    @Test
    fun GPMChecker() {
        val mark = TimeSource.Monotonic.markNow()
        // run test and catch any error or assertion
        val result = runCatching {
            runConcurrentTest {
                if (mark.elapsedNow() > 30.seconds) throw TimeExceededException()
                block()
                // inject timout by throwing exception 
            }
        }

        // If test passed (without timeout) return
        if (!result.isFailure) return
        val exception = result.exceptionOrNull()

        if (exception is LincheckAssertionError) {
            val actualResult = exception.failure.results.threadsResults[0][0]
            if (actualResult is ExceptionResult && actualResult.throwable is TimeExceededException) return
        }
//        
//        // If deadlock fail test
        if (exception?.message?.contains("Concurrent test has hung") == true) {
            throw AssertionError(exception.message)
        }
//
//        // else crash test
        throw IllegalStateException("Unexpected test result: ${exception?.message ?: "No message"}")
    }
}

class TimeExceededException : IllegalStateException()