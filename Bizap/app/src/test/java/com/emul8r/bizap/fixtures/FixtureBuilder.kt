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
        private var businessName: String = "Test Company"
        private var businessEmail: String = "test@company.com"
        private var businessPhone: String = "+1-555-0000"
        private var businessAddress: String = "123 Test Street"
        private var businessWebsite: String? = "www.test.com"
        private var taxId: String? = "ABN12345678901"
        private var taxRate: Double = 0.10
        private var taxName: String = "GST"
        private var paymentTermsDays: Int = 30
        private var defaultPaymentNotes: String = "Payment due within 30 days"
        private var footerMessage: String = "Thank you for your business"
        private var invoiceNumberPrefix: String = "INV-"
        private var selectedTheme: InvoiceTheme = InvoiceTheme.CANVAS
        private var primaryColor: String = "#0066CC"
        private var secondaryColor: String? = "#E6F2FF"
        private var bankName: String? = null
        private var accountNumber: String? = null
        private var routingCode: String? = null
        private var accountHolder: String? = null

        fun userId(userId: String) = apply { this.userId = userId }
        fun businessName(name: String) = apply { this.businessName = name }
        fun businessEmail(email: String) = apply { this.businessEmail = email }
        fun businessPhone(phone: String) = apply { this.businessPhone = phone }
        fun businessAddress(address: String) = apply { this.businessAddress = address }
        fun businessWebsite(website: String?) = apply { this.businessWebsite = website }
        fun taxId(taxId: String?) = apply { this.taxId = taxId }
        fun taxRate(rate: Double) = apply { this.taxRate = rate }
        fun taxName(name: String) = apply { this.taxName = name }
        fun paymentTermsDays(days: Int) = apply { this.paymentTermsDays = days }
        fun defaultPaymentNotes(notes: String) = apply { this.defaultPaymentNotes = notes }
        fun footerMessage(message: String) = apply { this.footerMessage = message }
        fun invoiceNumberPrefix(prefix: String) = apply { this.invoiceNumberPrefix = prefix }
        fun selectedTheme(theme: InvoiceTheme) = apply { this.selectedTheme = theme }
        fun primaryColor(color: String) = apply { this.primaryColor = color }
        fun secondaryColor(color: String?) = apply { this.secondaryColor = color }
        fun bankName(name: String?) = apply { this.bankName = name }
        fun accountNumber(number: String?) = apply { this.accountNumber = number }
        fun routingCode(code: String?) = apply { this.routingCode = code }
        fun accountHolder(holder: String?) = apply { this.accountHolder = holder }

        fun build(): InvoiceSettings {
            return InvoiceSettings(
                userId = userId,
                businessName = businessName,
                businessEmail = businessEmail,
                businessPhone = businessPhone,
                businessAddress = businessAddress,
                businessWebsite = businessWebsite,
                taxId = taxId,
                taxRate = taxRate,
                taxName = taxName,
                paymentTermsDays = paymentTermsDays,
                defaultPaymentNotes = defaultPaymentNotes,
                footerMessage = footerMessage,
                invoiceNumberPrefix = invoiceNumberPrefix,
                selectedTheme = selectedTheme,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                bankName = bankName,
                accountNumber = accountNumber,
                routingCode = routingCode,
                accountHolder = accountHolder
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
                businessName("Valid Test Company")
                businessEmail("valid@company.com")
                businessPhone("+1-555-0001")
                businessAddress("123 Valid Street, City, State 12345")
                businessWebsite("www.validcompany.com")
                taxId("ABN98765432100")
                taxRate(0.10)
                taxName("GST")
                paymentTermsDays(30)
                selectedTheme(InvoiceTheme.CANVAS)
                primaryColor("#0066CC")
                bankName("Valid Bank")
                accountNumber("123456789")
                accountHolder("Valid Company Inc")
            }
        }

        /**
         * Create invoice settings with minimal required fields
         */
        fun createMinimalSettings(): InvoiceSettings {
            return createSettings {
                userId("test_user_minimal")
                businessName("Minimal Company")
                businessEmail("minimal@company.com")
                businessPhone("+1-555-0002")
                businessAddress("456 Minimal Ave")
            }
        }

        /**
         * Create invoice settings for edge case testing
         */
        fun createEdgeCaseSettings(): InvoiceSettings {
            return createSettings {
                userId("test_user_edge")
                businessName("Edge Case Test Company With A Very Long Name That Might Break Formatting")
                businessEmail("very.long.email.address.with.many.parts@edge-case-company.com")
                businessPhone("+1-555-0003 ext. 12345")
                businessAddress("123 Very Long Address Street with Lots of Details, City, State 12345, Country")
                businessWebsite("www.verylongedgecasecompanywebsiteaddress.com")
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
                businessName("Café & Théâtre Société")
                businessEmail("contact@café-théâtre.com")
                businessPhone("+33-1-55-00-0004")
                businessAddress("123 Rue de l'Église, Paris, France")
                businessWebsite("www.café-théâtre.fr")
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
                businessName("Global Enterprises Ltd")
                businessEmail("billing@global-enterprises.co.uk")
                businessPhone("+44-20-7946-0958")
                businessAddress("123 Oxford Street, London, England, UK")
                businessWebsite("www.global-enterprises.co.uk")
                taxId("GB123456789")
                taxName("VAT")
                taxRate(0.20)
                selectedTheme(InvoiceTheme.HTML_PDF)
                primaryColor("#003399")
                bankName("HSBC Bank")
                accountNumber("GB82 WEST 1234 5698 7654 32")
                routingCode("21-00-02")
            }
        }
    }
}

