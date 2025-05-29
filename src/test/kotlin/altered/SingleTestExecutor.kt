package altered

import kotlinx.coroutines.runBlocking
import org.example.altered.RunCheckerBase
import org.jetbrains.kotlinx.lincheck.ExperimentalModelCheckingAPI
import org.jetbrains.kotlinx.lincheck.runConcurrentTest
import java.util.concurrent.TimeoutException
import kotlin.reflect.full.createInstance
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalModelCheckingAPI::class)
class SingleTestExecutor {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            try {
                val nr = args[0].toInt()
                val kClass = Class.forName("org.example.altered.test$nr.RunChecker$nr").kotlin
                val instance = kClass.createInstance() as RunCheckerBase

                runCatching {
                    runBlocking {
                        withTimeout(20.seconds) {
                            runConcurrentTest {
                                runBlocking {
                                    withTimeout(20_000L) {  // 60 seconds timeout
                                        instance.block()
                                    }
                                }
                            }
                        }
                    }
                }.fold(
                    onSuccess = { println("SUCCESS") },
                    onFailure = { e ->
                        when {
                            e is TimeoutException -> println("TIMEOUT")
                            e.message?.contains("Concurrent test has hung") == true -> println("DEADLOCK")
                            else -> {
                                println("ERROR")
                                e.printStackTrace()
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                println("ERROR")
                e.printStackTrace()
            }
        }
    }
}