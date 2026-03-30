package com.emul8r.bizap.ui.invoices.html

import freemarker.template.Configuration
import freemarker.template.Template
import timber.log.Timber
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Processes Freemarker templates with invoice data
 *
 * Handles:
 * - Template loading and caching
 * - Data binding and variable substitution
 * - Date and currency formatting
 * - Error handling with meaningful messages
 */
class HtmlTemplateProcessor {
    private val config: Configuration = Configuration(Configuration.VERSION_2_3_31)

    init {
        // Configure Freemarker
        config.setClassForTemplateLoading(HtmlTemplateProcessor::class.java, "/templates/")
        config.defaultEncoding = "UTF-8"
        config.numberFormat = "0.00"
        config.dateFormat = "yyyy-MM-dd"
    }

    /**
     * Process template with invoice data
     *
     * @param templateName Name of the template file (e.g., "invoice-template.html")
     * @param data Map of variable names to values for template substitution
     * @return HTML string with all variables substituted, or empty string on error
     */
    fun processTemplate(templateName: String, data: Map<String, Any>): String {
        return try {
            if (!validateTemplateName(templateName)) {
                Timber.e("Invalid template name: $templateName")
                return ""
            }

            val template: Template = config.getTemplate(templateName)
            val output = StringWriter()

            // Create data model with formatting functions
            val dataModel = data.toMutableMap().apply {
                this["formatCurrency"] = CurrencyFormatter()
                this["formatDate"] = DateFormatter()
                this["formatPercentage"] = PercentageFormatter()
            }

            template.process(dataModel, output)

            Timber.d("Template $templateName processed successfully")
            output.toString()
        } catch (e: Exception) {
            Timber.e(e, "Error processing template: $templateName")
            ""
        }
    }

    /**
     * Validate template name for security
     */
    private fun validateTemplateName(templateName: String): Boolean {
        return templateName.isNotBlank() &&
                !templateName.contains("..") &&
                !templateName.contains("/") &&
                !templateName.contains("\\") &&
                (templateName.endsWith(".html") || templateName.endsWith(".ftl"))
    }

    /**
     * Custom Freemarker formatter for currency
     */
    class CurrencyFormatter : freemarker.template.TemplateMethodModelEx {
        override fun exec(arguments: List<*>?): String {
            return try {
                val amount = (arguments?.firstOrNull() as? Number)?.toDouble() ?: 0.0
                String.format(Locale.US, "$%.2f", amount)
            } catch (e: Exception) {
                Timber.e(e, "Error formatting currency")
                "$0.00"
            }
        }
    }

    /**
     * Custom Freemarker formatter for dates
     */
    class DateFormatter : freemarker.template.TemplateMethodModelEx {
        override fun exec(arguments: List<*>?): String {
            return try {
                val date = arguments?.firstOrNull()
                val format = arguments?.getOrNull(1) as? String ?: "MMM dd, yyyy"
                val sdf = SimpleDateFormat(format, Locale.US)
                when (date) {
                    is Date -> sdf.format(date)
                    is Long -> sdf.format(Date(date))
                    is String -> date
                    else -> ""
                }
            } catch (e: Exception) {
                Timber.e(e, "Error formatting date")
                ""
            }
        }
    }

    /**
     * Custom Freemarker formatter for percentages
     */
    class PercentageFormatter : freemarker.template.TemplateMethodModelEx {
        override fun exec(arguments: List<*>?): String {
            return try {
                val percentage = (arguments?.firstOrNull() as? Number)?.toDouble() ?: 0.0
                String.format(Locale.US, "%.1f%%", percentage)
            } catch (e: Exception) {
                Timber.e(e, "Error formatting percentage")
                "0.0%"
            }
        }
    }
}

