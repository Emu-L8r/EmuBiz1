package com.emul8r.bizap.ui.gui2.integration

import android.content.Intent
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.ui.gui2.navigation.DeepLinkDestination
import com.emul8r.bizap.ui.gui2.navigation.DeepLinks
import com.emul8r.bizap.ui.gui2.navigation.parseDeepLinkIntent
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Navigation integration tests verifying that:
 * - All GUI2 screens are reachable via the correct routes
 * - Deep links parse correctly to their destinations
 * - Navigation extensions produce the correct route objects
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(sdk = [28])
class NavigationIntegrationTest : BaseUnitTest() {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    // ── Deep link URI generation ──────────────────────────────────────────────

    @Test
    fun `dashboard URI has correct scheme and host`() {
        val uri = DeepLinks.dashboardUri()
        assertEquals("bizap", uri.scheme)
        assertEquals("dashboard", uri.host)
    }

    @Test
    fun `customer URI contains correct customerId segment`() {
        val customerId = 42L
        val uri = DeepLinks.customerUri(customerId)
        assertEquals("bizap", uri.scheme)
        assertEquals("gui2", uri.host)
        assertEquals("/customer/42", uri.path)
    }

    @Test
    fun `invoice URI contains correct invoiceId segment`() {
        val invoiceId = 99L
        val uri = DeepLinks.invoiceUri(invoiceId)
        assertEquals("bizap", uri.scheme)
        assertEquals("gui2", uri.host)
        assertEquals("/invoice/99", uri.path)
    }

    // ── Deep link intent parsing ──────────────────────────────────────────────

    @Test
    fun `null intent returns Unknown destination`() {
        val result = parseDeepLinkIntent(null)
        assertIs<DeepLinkDestination.Unknown>(result)
    }

    @Test
    fun `intent with no data returns Unknown destination`() {
        val intent = Intent(Intent.ACTION_VIEW)
        val result = parseDeepLinkIntent(intent)
        assertIs<DeepLinkDestination.Unknown>(result)
    }

    @Test
    fun `dashboard deep link parses to Dashboard destination`() {
        val intent = Intent(Intent.ACTION_VIEW, DeepLinks.dashboardUri())
        val result = parseDeepLinkIntent(intent)
        assertIs<DeepLinkDestination.Dashboard>(result)
    }

    @Test
    fun `customer deep link parses to Customer destination with correct id`() {
        val customerId = 7L
        val intent = Intent(Intent.ACTION_VIEW, DeepLinks.customerUri(customerId))
        val result = parseDeepLinkIntent(intent)
        assertIs<DeepLinkDestination.Customer>(result)
        assertEquals(customerId, result.customerId)
    }

    @Test
    fun `invoice deep link parses to Invoice destination with correct id`() {
        val invoiceId = 55L
        val intent = Intent(Intent.ACTION_VIEW, DeepLinks.invoiceUri(invoiceId))
        val result = parseDeepLinkIntent(intent)
        assertIs<DeepLinkDestination.Invoice>(result)
        assertEquals(invoiceId, result.invoiceId)
    }

    @Test
    fun `unknown scheme returns Unknown destination`() {
        val uri = Uri.parse("https://example.com/customer/1")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        val result = parseDeepLinkIntent(intent)
        assertIs<DeepLinkDestination.Unknown>(result)
    }

    @Test
    fun `malformed customer id returns Unknown destination`() {
        val uri = Uri.parse("bizap://gui2/customer/not-a-number")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        val result = parseDeepLinkIntent(intent)
        assertIs<DeepLinkDestination.Unknown>(result)
    }

    @Test
    fun `unknown gui2 path returns Unknown destination`() {
        val uri = Uri.parse("bizap://gui2/unknownpath/123")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        val result = parseDeepLinkIntent(intent)
        assertIs<DeepLinkDestination.Unknown>(result)
    }
}
