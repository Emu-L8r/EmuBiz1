package com.emul8r.bizap

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.rules.TestRule

/**
 * Base class for all unit tests
 * Provides common test utilities and rules
 */
abstract class BaseUnitTest {
    
    @get:Rule
    val instantTaskExecutorRule: TestRule = InstantTaskExecutorRule()
    
    protected val testDispatcher = StandardTestDispatcher()
    
    protected fun runUnitTest(block: suspend () -> Unit) = runTest {
        block()
    }
}
