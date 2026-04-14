package com.emul8r.bizap.ui.landing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emul8r.bizap.R
/**
 * Landing screen that lets the user choose between GUI1 (legacy) and GUI2 (new).
 * Shown only when no GUI preference has been saved yet — or when the user explicitly
 * resets their choice from Settings.
 *
 * Features:
 * - Branded splash screen with Bizap logo
 * - Professional gradient background
 * - Clear GUI selection options
 *
 * @param onSelectGui1 Called when the user picks the legacy GUI1 experience.
 * @param onSelectGui2 Called when the user picks the new GUI2 experience.
 */
@Composable
fun LandingScreen(
    onSelectGui1: () -> Unit,
    onSelectGui2: () -> Unit,
    onSelectGui3: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ---- BRANDED HEADER SECTION ----
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                    )
                    .padding(vertical = 56.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Bizap Logo
                    androidx.compose.foundation.Image(
                        painter = painterResource(R.drawable.company_logo),
                        contentDescription = "Bizap Logo",
                        modifier = Modifier
                            .size(120.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                            ),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Bizap",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 48.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.text_smart_invoice_payment_mgmt),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // ---- CONTENT SECTION ----
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.surface
                    )
                    .padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Choose Your Experience",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Select the interface that works best for you",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // GUI2 — promoted as the recommended choice
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 4.dp),
                    onClick = onSelectGui2,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(28.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "✨ Modern Experience",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                            Badge(
                                modifier = Modifier.padding(start = 8.dp),
                                containerColor = MaterialTheme.colorScheme.tertiary
                            ) {
                                Text(
                                    "Recommended",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Latest features · Better performance · Intuitive interface",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                            lineHeight = 24.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onSelectGui2,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                stringResource(R.string.button_get_started),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // GUI3 — Matrix Experience (NEW!)
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 4.dp),
                    onClick = onSelectGui3,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = Color(0xFF111111)
                    )
                ) {
                    Column(modifier = Modifier.padding(28.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "🟢 Matrix Experience",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF00DD00),
                                modifier = Modifier.weight(1f)
                            )
                            Badge(
                                modifier = Modifier.padding(start = 8.dp),
                                containerColor = Color(0xFF00FF00)
                            ) {
                                Text(
                                    "NEW",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Cyberpunk elegance · Immersive UI · Premium feel",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 16.sp
                            ),
                            color = Color(0xFF00DD00).copy(alpha = 0.85f),
                            lineHeight = 24.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onSelectGui3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00DD00),
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                "Enter The Matrix",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // GUI1 — legacy option
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onSelectGui1,
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Column(modifier = Modifier.padding(28.dp)) {
                        Text(
                            text = stringResource(R.string.text_modern_interface_description),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.text_classic_experience_description),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            lineHeight = 24.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        OutlinedButton(
                            onClick = onSelectGui1,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text(
                                "Use Classic",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
