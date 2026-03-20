package com.emul8r.bizap

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TestRule

/**
 * Base class for all unit tests.
 * Correctly handles Main Dispatcher overrides for ViewModel testing.
 */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseUnitTest {
    
    @get:Rule
    val instantTaskExecutorRule: TestRule = InstantTaskExecutorRule()
    
    protected val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setupBase() {
        Dispatchers.setMain(testDispatcher)
    }
    
    @After
    fun tearDownBase() {
        Dispatchers.resetMain()
    }

    protected fun runUnitTest(block: suspend TestScope.() -> Unit) = runTest(testDispatcher) {
        block()
    }
}
