package com.emul8r.bizap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.emul8r.bizap.ui.theme.CardDefaults as CardDefaultsTokens
import com.emul8r.bizap.ui.theme.FormDefaults
import com.emul8r.bizap.ui.theme.Spacing

/**
 * ============================================================================
 * FORM SECTION COMPONENT - Groups related form fields
 * ============================================================================
 *
 * A container for grouping related form fields with:
 * - Light background for visual separation
 * - Consistent padding
 * - Organized spacing between fields
 * - Card elevation for depth
 *
 * Use inside SectionHeader for complete sections.
 * Example structure:
 *   SectionHeader(...)
 *   FormSection {
 *     FormField(...)
 *     FormField(...)
 *   }
 */

@Composable
fun FormSection(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(
            defaultElevation = CardDefaultsTokens.defaultElevation
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CardDefaultsTokens.defaultPadding),
            verticalArrangement = Arrangement.spacedBy(FormDefaults.fieldSpacing),
            content = content
        )
    }
}

