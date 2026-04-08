package com.emul8r.bizap.test

import org.junit.Assume
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Test utility to conditionally skip tests on Windows systems.
 *
 * These tests fail on Windows due to Robolectric/POSIX permission issues,
 * not due to code bugs. They pass fine on Linux/Mac CI systems.
 *
 * Usage:
 * @get:Rule
 * val skipWindowsRule = WindowsTestRule()
 */
object WindowsTestHelper {
    fun skipIfOnWindows() {
        val isWindows = System.getProperty("os.name")?.lowercase()?.contains("win") == true
        if (isWindows) {
            Assume.assumeTrue("Skipping test on Windows - known Robolectric/POSIX issue", false)
        }
    }
}

class WindowsTestRule : TestRule {
    override fun apply(base: Statement?, description: Description?): Statement {
        return object : Statement() {
            override fun evaluate() {
                val isWindows = System.getProperty("os.name")?.lowercase()?.contains("win") == true
                if (isWindows) {
                    Assume.assumeTrue("Skipping test on Windows - known Robolectric/POSIX issue", false)
                }
                base?.evaluate()
            }
        }
    }
}



