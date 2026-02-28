package com.emul8r.bizap.util

import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher

/**
 * Standard test dispatchers for unit tests
 */
object TestDispatchers {
    val testDispatcher: TestDispatcher = StandardTestDispatcher()
}
