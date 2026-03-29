package com.emul8r.bizap.ui.settings.invoice_theme

import freemarker.template.Configuration
import freemarker.template.Template
import timber.log.Timber
import java.io.StringWriter

/**
 * Processes HTML invoice templates using Freemarker.
 *
 * Handles:
 * - Loading templates from assets
 * - Binding data to template variables
 * - Rendering HTML output
 * - Error handling and logging
 */
class HtmlTemplateProcessor {

    private val config: Configuration by lazy {
        Configuration(Configuration.VERSION_2_3_32).apply {
            defaultEncoding = "UTF-8"
            setClassLoaderForTemplateLoading(
                this@HtmlTemplateProcessor.javaClass.classLoader,
                "invoices/html-theme/"
            )
        }
    }

    /**
     * Process HTML template with given data.
     *
     * @param templateName Name of template file (e.g., "invoice-template.html")
     * @param data Map of variables to bind to template
     * @return Processed HTML string
     * @throws Exception if template processing fails
     */
    fun processTemplate(
        templateName: String,
        data: Map<String, Any?>
    ): Result<String> {
        return try {
            Timber.d("Processing template: $templateName")

            val template: Template = config.getTemplate(templateName)
            val stringWriter = StringWriter()

            template.process(data, stringWriter)
            val html = stringWriter.toString()

            Timber.d("Template processed successfully: $templateName")
            Result.success(html)
        } catch (e: Exception) {
            Timber.e(e, "Failed to process template: $templateName")
            Result.failure(
                Exception("Failed to process template $templateName: ${e.message}", e)
            )
        }
    }

    /**
     * Clear template cache (useful for development/testing).
     */
    fun clearCache() {
        config.clearTemplateCache()
        Timber.d("Template cache cleared")
    }
}

