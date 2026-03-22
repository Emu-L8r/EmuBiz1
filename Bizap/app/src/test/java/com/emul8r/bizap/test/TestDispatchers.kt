package com.emul8r.bizap.test

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher

/**
 * Provides test dispatchers for coroutine testing.
 * Use these in ViewModel tests to ensure deterministic behavior.
 */
object TestDispatchers {
    val Main: TestDispatcher = StandardTestDispatcher()
    val IO: TestDispatcher = StandardTestDispatcher()
    val Default: TestDispatcher = StandardTestDispatcher()
}

