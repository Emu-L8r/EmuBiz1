package com.emul8r.bizap.fixtures

import com.emul8r.bizap.domain.model.InvoiceSettings
import com.emul8r.bizap.domain.model.InvoiceTheme

/**
 * Builder pattern for creating flexible test data
 * Allows dynamic creation of test fixtures with custom values
 */
class FixtureBuilder {

    // ============================================================================
    // INVOICE SETTINGS BUILDER
    // ============================================================================

    class InvoiceSettingsBuilder {
        private var userId: String = "test_user"
        private var taxRate: Double = 0.10
        private var taxName: String = "GST"
        private var paymentTermsDays: Int = 30
        private var defaultPaymentNotes: String = "Payment due within 30 days"
        private var footerMessage: String = "Thank you for your business"
        private var invoiceNumberPrefix: String = "INV-"
        private var selectedTheme: InvoiceTheme = InvoiceTheme.CANVAS
        private var primaryColor: String = "#0066CC"
        private var secondaryColor: String = "#E6F2FF"

        fun userId(userId: String) = apply { this.userId = userId }
        fun taxRate(rate: Double) = apply { this.taxRate = rate }
        fun taxName(name: String) = apply { this.taxName = name }
        fun paymentTermsDays(days: Int) = apply { this.paymentTermsDays = days }
        fun defaultPaymentNotes(notes: String) = apply { this.defaultPaymentNotes = notes }
        fun footerMessage(message: String) = apply { this.footerMessage = message }
        fun invoiceNumberPrefix(prefix: String) = apply { this.invoiceNumberPrefix = prefix }
        fun selectedTheme(theme: InvoiceTheme) = apply { this.selectedTheme = theme }
        fun primaryColor(color: String) = apply { this.primaryColor = color }
        fun secondaryColor(color: String) = apply { this.secondaryColor = color }

        fun build(): InvoiceSettings {
            return InvoiceSettings(
                userId = userId,
                taxRate = taxRate,
                taxName = taxName,
                paymentTermsDays = paymentTermsDays,
                defaultPaymentNotes = defaultPaymentNotes,
                footerMessage = footerMessage,
                invoiceNumberPrefix = invoiceNumberPrefix,
                selectedTheme = selectedTheme,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor
            )
        }
    }

    companion object {
        /**
         * Create a default invoice settings
         */
        fun createDefaultSettings(): InvoiceSettings {
            return InvoiceSettingsBuilder().build()
        }

        /**
         * Create invoice settings with custom values using builder
         */
        fun createSettings(block: InvoiceSettingsBuilder.() -> Unit): InvoiceSettings {
            return InvoiceSettingsBuilder().apply(block).build()
        }

        /**
         * Create invoice settings with Canvas theme
         */
        fun createCanvasThemeSettings(): InvoiceSettings {
            return createSettings {
                selectedTheme(InvoiceTheme.CANVAS)
                primaryColor("#0066CC")
            }
        }

        /**
         * Create invoice settings with HTML-to-PDF theme
         */
        fun createHtmlThemeSettings(): InvoiceSettings {
            return createSettings {
                selectedTheme(InvoiceTheme.HTML_PDF)
                primaryColor("#FF6600")
            }
        }

        /**
         * Create invoice settings with all required fields for validation
         */
        fun createValidSettings(): InvoiceSettings {
            return createSettings {
                userId("test_user_valid")
                taxRate(0.10)
                taxName("GST")
                paymentTermsDays(30)
                selectedTheme(InvoiceTheme.CANVAS)
                primaryColor("#0066CC")
            }
        }

        /**
         * Create invoice settings with minimal required fields
         */
        fun createMinimalSettings(): InvoiceSettings {
            return createSettings {
                userId("test_user_minimal")
            }
        }

        /**
         * Create invoice settings for edge case testing
         */
        fun createEdgeCaseSettings(): InvoiceSettings {
            return createSettings {
                userId("test_user_edge")
                taxRate(0.23456) // Odd decimal
                invoiceNumberPrefix("EDGE-CASE-PREFIX-")
                primaryColor("#FF00FF")
                selectedTheme(InvoiceTheme.HTML_PDF)
            }
        }

        /**
         * Create invoice settings with special characters
         */
        fun createSpecialCharacterSettings(): InvoiceSettings {
            return createSettings {
                userId("test_user_special")
                taxName("TVA")
                footerMessage("Merci pour votre confiance! © 2026")
            }
        }

        /**
         * Create invoice settings with international formatting
         */
        fun createInternationalSettings(): InvoiceSettings {
            return createSettings {
                userId("test_user_intl")
                taxName("VAT")
                taxRate(0.20)
                selectedTheme(InvoiceTheme.HTML_PDF)
                primaryColor("#003399")
            }
        }
    }
}




