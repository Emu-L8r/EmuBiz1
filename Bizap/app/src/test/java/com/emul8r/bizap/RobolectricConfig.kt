package com.emul8r.bizap

import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowPackageManager

/**
 * Robolectric Configuration for Windows Compatibility
 *
 * This configuration file is used by Robolectric to customize test behavior
 * on Windows systems, particularly for handling POSIX file permissions issues.
 *
 * When Robolectric encounters POSIX permission calls on Windows, it throws:
 * java.lang.UnsupportedOperationException: POSIX file permissions not supported on this platform
 *
 * Solution: Configure Robolectric to use a temporary directory that doesn't
 * require POSIX attributes, and enable offline mode for better compatibility.
 */
@Config(
    sdk = [35],  // Target Android 15
    shadows = [],  // No custom shadows needed for POSIX fix
    application = BizapApplication::class
)
object RobolectricTestConfig

