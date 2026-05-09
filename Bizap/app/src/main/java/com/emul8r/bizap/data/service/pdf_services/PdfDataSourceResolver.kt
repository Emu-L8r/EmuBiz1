package com.emul8r.bizap.data.service.pdf_services

import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Phase 3D: PDF Data Source Resolver
 *
 * Implements the source-of-truth matrix for PDF-related data.
 * Defines which data comes from BusinessProfile vs InvoiceSettings vs defaults,
 * with clear priority ordering.
 *
 * Priority Matrix:
 * ┌─────────────────────────┬───────────┬──────────┬──────────┐
 * │ Field                   │ User      │ Business │ Invoice  │
 * ├─────────────────────────┼───────────┼──────────┼──────────┤
 * │ Company Logo            │ Secondary │ Primary  │ Override │
 * │ Company Name            │ ✓         │ Primary  │ ✓        │
 * │ Payment Terms           │ Primary   │ Fallback │ Override │
 * │ Bank Details            │ Secondary │ Primary  │ ✓        │
 * │ Tax Rate                │ Primary   │ Fallback │ ✓        │
 * │ Primary Color (Branding)│ Primary   │ N/A      │ N/A      │
 * │ Footer Message          │ Primary   │ Fallback │ N/A      │
 * │ Invoice Note            │ N/A       │ N/A      │ Primary  │
 * └─────────────────────────┴───────────┴──────────┴──────────┘
 *
 * All data resolution is logged for debugging and audit trails.
 */
@Singleton
class PdfDataSourceResolver @Inject constructor() {

    /**
     * Resolve company logo with fallback chain.
     *
     * Priority:
     * 1. Invoice-specific logo (if present)
     * 2. Business profile logo
     * 3. User settings logo
     * 4. Default/null
     */
    fun resolveLogo(
        invoiceLogo: String? = null,
        businessLogo: String? = null,
        userLogo: String? = null
    ): String? {
        return when {
            !invoiceLogo.isNullOrBlank() -> {
                Timber.d("✅ Logo: Using invoice-specific logo")
                invoiceLogo
            }
            !businessLogo.isNullOrBlank() -> {
                Timber.d("✅ Logo: Using business profile logo")
                businessLogo
            }
            !userLogo.isNullOrBlank() -> {
                Timber.d("✅ Logo: Using user settings logo")
                userLogo
            }
            else -> {
                Timber.d("⏭️  Logo: No logo configured")
                null
            }
        }
    }

    /**
     * Resolve payment terms with fallback chain.
     *
     * Priority:
     * 1. Invoice-specific terms (if present)
     * 2. User settings terms
     * 3. Business profile terms
     * 4. Default terms
     */
    fun resolvePaymentTerms(
        invoiceTerms: String? = null,
        userTerms: String? = null,
        businessTerms: String? = null
    ): String {
        return when {
            !invoiceTerms.isNullOrBlank() -> {
                Timber.d("✅ Terms: Using invoice-specific terms")
                invoiceTerms
            }
            !userTerms.isNullOrBlank() -> {
                Timber.d("✅ Terms: Using user settings terms")
                userTerms
            }
            !businessTerms.isNullOrBlank() -> {
                Timber.d("✅ Terms: Using business profile terms")
                businessTerms
            }
            else -> {
                Timber.d("⏭️  Terms: Using default terms")
                "Net 30 days"
            }
        }
    }

    /**
     * Resolve bank account details with fallback chain.
     *
     * Priority:
     * 1. Invoice-specific bank details (if present)
     * 2. Business profile bank details
     * 3. User settings bank details
     * 4. Default/null
     */
    fun resolveBankDetails(
        invoiceBankInfo: BankInfo? = null,
        businessBankInfo: BankInfo? = null,
        userBankInfo: BankInfo? = null
    ): BankInfo? {
        return when {
            invoiceBankInfo != null && invoiceBankInfo.isValid() -> {
                Timber.d("✅ Bank: Using invoice-specific bank details")
                invoiceBankInfo
            }
            businessBankInfo != null && businessBankInfo.isValid() -> {
                Timber.d("✅ Bank: Using business profile bank details")
                businessBankInfo
            }
            userBankInfo != null && userBankInfo.isValid() -> {
                Timber.d("✅ Bank: Using user settings bank details")
                userBankInfo
            }
            else -> {
                Timber.d("⏭️  Bank: No bank details configured")
                null
            }
        }
    }

    /**
     * Resolve company name with fallback chain.
     *
     * Priority:
     * 1. Invoice-specific company name
     * 2. Business profile company name
     * 3. User settings company name
     * 4. Default
     */
    fun resolveCompanyName(
        invoiceName: String? = null,
        businessName: String? = null,
        userName: String? = null
    ): String {
        return when {
            !invoiceName.isNullOrBlank() -> {
                Timber.d("✅ Company: Using invoice-specific name")
                invoiceName
            }
            !businessName.isNullOrBlank() -> {
                Timber.d("✅ Company: Using business profile name")
                businessName
            }
            !userName.isNullOrBlank() -> {
                Timber.d("✅ Company: Using user settings name")
                userName
            }
            else -> {
                Timber.d("⏭️  Company: Using default name")
                "Your Company Name"
            }
        }
    }

    /**
     * Resolve tax rate with fallback chain.
     *
     * Priority:
     * 1. Invoice-specific tax rate
     * 2. User settings tax rate
     * 3. Business profile tax rate
     * 4. Default (10% for Australia)
     */
    fun resolveTaxRate(
        invoiceTaxRate: Double? = null,
        userTaxRate: Double? = null,
        businessTaxRate: Double? = null
    ): Double {
        return when {
            invoiceTaxRate != null && invoiceTaxRate >= 0 -> {
                Timber.d("✅ Tax: Using invoice-specific rate: $invoiceTaxRate%")
                invoiceTaxRate
            }
            userTaxRate != null && userTaxRate >= 0 -> {
                Timber.d("✅ Tax: Using user settings rate: $userTaxRate%")
                userTaxRate
            }
            businessTaxRate != null && businessTaxRate >= 0 -> {
                Timber.d("✅ Tax: Using business profile rate: $businessTaxRate%")
                businessTaxRate
            }
            else -> {
                Timber.d("⏭️  Tax: Using default rate: 10%")
                10.0
            }
        }
    }

    /**
     * Resolve footer message with fallback chain.
     *
     * Priority:
     * 1. User settings footer message
     * 2. Business profile footer message
     * 3. Default footer message
     */
    fun resolveFooterMessage(
        userFooter: String? = null,
        businessFooter: String? = null
    ): String {
        return when {
            !userFooter.isNullOrBlank() -> {
                Timber.d("✅ Footer: Using user settings message")
                userFooter
            }
            !businessFooter.isNullOrBlank() -> {
                Timber.d("✅ Footer: Using business profile message")
                businessFooter
            }
            else -> {
                Timber.d("⏭️  Footer: Using default message")
                "Thank you for your business"
            }
        }
    }

    /**
     * Resolve contact information with fallback chain.
     *
     * Priority:
     * 1. Business profile contact info
     * 2. User settings contact info
     * 3. Default/empty
     */
    fun resolveContactInfo(
        businessEmail: String? = null,
        businessPhone: String? = null,
        businessAddress: String? = null,
        userEmail: String? = null
    ): ContactInfo {
        return ContactInfo(
            email = businessEmail?.takeIf { it.isNotBlank() }
                ?: userEmail?.takeIf { it.isNotBlank() }
                ?: "contact@company.com",
            phone = businessPhone?.takeIf { it.isNotBlank() }
                ?: "",
            address = businessAddress?.takeIf { it.isNotBlank() }
                ?: ""
        ).also {
            Timber.d("✅ Contact: Resolved to email=${it.email}, phone=${it.phone.ifBlank { "none" }}")
        }
    }
}

/**
 * Resolved PDF data after applying source-of-truth matrix.
 */
data class ResolvedPdfData(
    val companyName: String = "Your Company Name",
    val logo: String? = null,
    val email: String = "contact@company.com",
    val phone: String = "",
    val address: String = "",
    val paymentTerms: String = "Net 30 days",
    val bankDetails: BankInfo? = null,
    val taxRate: Double = 10.0,
    val footerMessage: String = "Thank you for your business"
)

data class ContactInfo(
    val email: String = "contact@company.com",
    val phone: String = "",
    val address: String = ""
)

/**
 * Extension function to validate BankInfo
 */
fun BankInfo.isValid(): Boolean {
    return !this.bank.isNullOrBlank() ||
           !this.accountName.isNullOrBlank() ||
           !this.bsb.isNullOrBlank() ||
           !this.accountNumber.isNullOrBlank()
}

