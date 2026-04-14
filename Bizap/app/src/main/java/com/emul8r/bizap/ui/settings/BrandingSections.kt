package com.emul8r.bizap.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.domain.model.*

/**
 * ✨ PHASE 3: BRANDING LAYER UI SECTIONS
 *
 * 7 comprehensive composable sections for all branding features:
 * - Logo placement and sizing
 * - Company motto/slogan styling
 * - Payment method icons
 * - Signature authorization areas
 * - QR code customization
 * - Company information & social media
 * - Preview of all branding elements
 */

/**
 * SECTION 1: Logo Customization
 * Upload logo and customize size, position, and visibility
 */
@Composable
fun LogoSection(
    enableLogo: Boolean,
    logoUri: String,
    logoWidthMm: Float,
    logoHeightMm: Float,
    logoPosition: LogoPosition,
    onLogoToggled: (Boolean) -> Unit,
    onLogoSelected: (String) -> Unit,
    onWidthChanged: (Float) -> Unit,
    onHeightChanged: (Float) -> Unit,
    onPositionChanged: (LogoPosition) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "📸 Logo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Switch(
                checked = enableLogo,
                onCheckedChange = onLogoToggled
            )
        }

        if (enableLogo) {
            // Logo file picker (simplified UI representation)
            Button(
                onClick = { /* File picker logic */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (logoUri.isEmpty()) "Upload Logo (PNG/JPG)" else "Change Logo")
            }

            if (logoUri.isNotEmpty()) {
                Text("Logo selected: ${logoUri.takeLast(30)}", style = MaterialTheme.typography.labelSmall)
            }

            // Width slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Width: ${logoWidthMm.toInt()}mm", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                Slider(
                    value = logoWidthMm,
                    onValueChange = onWidthChanged,
                    valueRange = 0f..100f,
                    modifier = Modifier.weight(2f)
                )
            }

            // Height slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Height: ${logoHeightMm.toInt()}mm", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                Slider(
                    value = logoHeightMm,
                    onValueChange = onHeightChanged,
                    valueRange = 0f..100f,
                    modifier = Modifier.weight(2f)
                )
            }

            // Position selector
            Text("Position:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(LogoPosition.values()) { position ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPositionChanged(position) }
                            .padding(8.dp)
                            .background(
                                if (logoPosition == position) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (logoPosition == position) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        Text(position.displayName, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

/**
 * SECTION 2: Motto/Slogan
 * Add and customize company motto text
 */
@Composable
fun MottoSection(
    enableMotto: Boolean,
    mottoText: String,
    mottoFontSize: Float,
    mottoColor: String,
    onMottoToggled: (Boolean) -> Unit,
    onMottoTextChanged: (String) -> Unit,
    onFontSizeChanged: (Float) -> Unit,
    onColorChanged: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "💬 Company Motto/Slogan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Switch(
                checked = enableMotto,
                onCheckedChange = onMottoToggled
            )
        }

        if (enableMotto) {
            TextField(
                value = mottoText,
                onValueChange = onMottoTextChanged,
                label = { Text("Motto Text") },
                placeholder = { Text("e.g., Your company motto here") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                singleLine = true,
                supportingText = { Text("${mottoText.length}/100 characters") }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Font Size: ${mottoFontSize.toInt()}pt", style = MaterialTheme.typography.bodySmall)
                Slider(
                    value = mottoFontSize,
                    onValueChange = onFontSizeChanged,
                    valueRange = 8f..18f,
                    modifier = Modifier.weight(1f)
                )
            }

            // Color preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(android.graphics.Color.parseColor(mottoColor)), RoundedCornerShape(4.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                )
                Text("Motto Color: $mottoColor", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

/**
 * SECTION 3: Payment Method Icons
 * Select which payment methods to display on invoice
 */
@Composable
fun PaymentIconsSection(
    enablePaymentIcons: Boolean,
    acceptedPaymentMethodsJson: String,
    paymentIconsSize: Float,
    onPaymentIconsToggled: (Boolean) -> Unit,
    onPaymentMethodsChanged: (List<PaymentMethod>) -> Unit,
    onSizeChanged: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "💳 Payment Method Icons",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Switch(
                checked = enablePaymentIcons,
                onCheckedChange = onPaymentIconsToggled
            )
        }

        if (enablePaymentIcons) {
            Text("Accepted Payment Methods:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(PaymentMethod.values()) { method ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Checkbox(
                            checked = acceptedPaymentMethodsJson.contains(method.name, ignoreCase = true),
                            onCheckedChange = { /* Update logic */ }
                        )
                        Text("${method.icon} ${method.displayName}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Icon Size: ${paymentIconsSize.toInt()}mm", style = MaterialTheme.typography.bodySmall)
                Slider(
                    value = paymentIconsSize,
                    onValueChange = onSizeChanged,
                    valueRange = 8f..24f,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * SECTION 4: Signature Area
 * Add signature lines for authorization
 */
@Composable
fun SignatureSection(
    enableSignatureArea: Boolean,
    signatureLabel: String,
    signatureLineLengthMm: Float,
    onSignatureToggled: (Boolean) -> Unit,
    onLabelChanged: (String) -> Unit,
    onLineLengthChanged: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "✍️ Signature Area",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Switch(
                checked = enableSignatureArea,
                onCheckedChange = onSignatureToggled
            )
        }

        if (enableSignatureArea) {
            TextField(
                value = signatureLabel,
                onValueChange = onLabelChanged,
                label = { Text("Label") },
                placeholder = { Text("e.g., Authorized By:") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Line Length: ${signatureLineLengthMm.toInt()}mm", style = MaterialTheme.typography.bodySmall)
                Slider(
                    value = signatureLineLengthMm,
                    onValueChange = onLineLengthChanged,
                    valueRange = 20f..80f,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * SECTION 5: QR Code
 * Add scannable QR code for payments or information
 */
@Composable
fun QrCodeSection(
    enableQrCode: Boolean,
    qrCodeContent: String,
    qrCodeSizeMm: Float,
    qrCodePosition: QrCodePosition,
    onQrToggled: (Boolean) -> Unit,
    onContentChanged: (String) -> Unit,
    onSizeChanged: (Float) -> Unit,
    onPositionChanged: (QrCodePosition) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "🔲 QR Code",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Switch(
                checked = enableQrCode,
                onCheckedChange = onQrToggled
            )
        }

        if (enableQrCode) {
            TextField(
                value = qrCodeContent,
                onValueChange = onContentChanged,
                label = { Text("QR Code Content") },
                placeholder = { Text("URL, payment link, or contact info") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Size: ${qrCodeSizeMm.toInt()}mm", style = MaterialTheme.typography.bodySmall)
                Slider(
                    value = qrCodeSizeMm,
                    onValueChange = onSizeChanged,
                    valueRange = 10f..50f,
                    modifier = Modifier.weight(1f)
                )
            }

            Text("Position:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                QrCodePosition.values().forEach { position ->
                    FilterChip(
                        selected = qrCodePosition == position,
                        onClick = { onPositionChanged(position) },
                        label = { Text(position.displayName.take(10), style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * SECTION 6: Company Information & Social Media
 * Add website, motto, and social media links
 */
@Composable
fun CompanyInfoSection(
    companyMotto: String,
    companyWebsite: String,
    companySocialMediaJson: String,
    onMottoChanged: (String) -> Unit,
    onWebsiteChanged: (String) -> Unit,
    onSocialMediaChanged: (String, String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "🏢 Company Information & Social Media",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        TextField(
            value = companyMotto,
            onValueChange = onMottoChanged,
            label = { Text("Company Motto/Tagline") },
            placeholder = { Text("Short company motto") },
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = companyWebsite,
            onValueChange = onWebsiteChanged,
            label = { Text("Company Website") },
            placeholder = { Text("https://example.com") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
        )

        Text("Social Media Handles:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(SocialMediaPlatform.values()) { platform ->
                TextField(
                    value = "", // Parse from JSON
                    onValueChange = { onSocialMediaChanged(platform.name, it) },
                    label = { Text("${platform.icon} ${platform.displayName}") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
    }
}

/**
 * SECTION 7: Branding Preview
 * Visual preview of all branding elements
 */
@Composable
fun BrandingPreviewSection(
    enableLogo: Boolean,
    enableMotto: Boolean,
    enablePaymentIcons: Boolean,
    enableQrCode: Boolean,
    enableSignatureArea: Boolean,
    mottoText: String,
    paymentIconsCount: Int,
    qrCodeContent: String,
    signatureLabel: String,
    companySocialMediaJson: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(
            "🎨 Branding Preview",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Logo preview
        if (enableLogo) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(Color.LightGray, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("[Company Logo]", style = MaterialTheme.typography.labelSmall)
            }
        }

        // Motto preview
        if (enableMotto && mottoText.isNotEmpty()) {
            Text(
                mottoText,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Payment icons preview
        if (enablePaymentIcons && paymentIconsCount > 0) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(minOf(paymentIconsCount, 5)) {
                    Text("💳", style = MaterialTheme.typography.headlineSmall)
                }
            }
        }

        // QR code preview
        if (enableQrCode && qrCodeContent.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color.LightGray, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("QR", style = MaterialTheme.typography.labelSmall)
            }
        }

        // Signature preview
        if (enableSignatureArea) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(signatureLabel, style = MaterialTheme.typography.labelSmall)
                HorizontalDivider(modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .padding(top = 4.dp))
            }
        }
    }
}


