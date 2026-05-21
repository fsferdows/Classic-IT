package com.omnishop.erp.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omnishop.erp.core.designsystem.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopOnboardingScreen(
    authViewModel: AuthViewModel,
    onComplete: (String) -> Unit
) {
    // Standard wizard wizard step counters
    var currentStep by remember { mutableStateOf(1) }
    
    // Core shop parameters state
    var companyName by remember { mutableStateOf("") }
    var businessType by remember { mutableStateOf("Classic IT") }
    var accentThemeColor by remember { mutableStateOf("#1565C0") } // default IT Royal Sapphire Blue
    var receiptFooterText by remember { mutableStateOf("Thank you for shopping!") }
    var invoiceTemplateStyle by remember { mutableStateOf("STANDARD") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LuxCard(
                modifier = Modifier
                    .widthIn(max = 500.dp)
                    .fillMaxWidth(),
                variant = LuxCardVariant.Glass,
                cornerRadius = 24.dp
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Wizard Step Indicator Bubble
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(1, 2, 3).forEach { step ->
                            val active = currentStep == step
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "$step",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    when (currentStep) {
                        1 -> {
                            // Step 1: Business Identity dynamic choices
                            Text(
                                text = "Enterprise Setup: Identity",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-0.5).sp
                                ),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Onboard a new isolated storefront tenant",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            OutlinedTextField(
                                value = companyName,
                                onValueChange = { companyName = it },
                                label = { Text("Store/Company Name *") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("onboard_company_name"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Primary Shop Industry Vertical:",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.align(Alignment.Start),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Classic IT", "Clothing", "General Mart").forEach { type ->
                                    val isSelected = businessType == type
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(
                                                1.5.dp,
                                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable { businessType = type }
                                            .padding(12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            type,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelSmall,
                                            textAlign = TextAlign.Center,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                        2 -> {
                            // Step 2: Dynamic Color Custom White labeling branding
                            Text(
                                text = "White-Label Brand Setup",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-0.5).sp
                                ),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Assign real-time dynamically rendered themes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                "Pick Theme Palette Extraction Core Color:",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.align(Alignment.Start),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf("#00E5FF", "#EC407A", "#FF8F00", "#4CAF50", "#9C27B0").forEach { hex ->
                                    val isPicked = accentThemeColor == hex
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .border(
                                                2.5.dp,
                                                if (isPicked) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                RoundedCornerShape(100.dp)
                                            )
                                            .padding(4.dp)
                                            .background(Color(android.graphics.Color.parseColor(hex)), RoundedCornerShape(100.dp))
                                            .clickable { accentThemeColor = hex }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            OutlinedTextField(
                                value = receiptFooterText,
                                onValueChange = { receiptFooterText = it },
                                label = { Text("Thermal Receipt Footer Message") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                )
                            )
                        }
                        3 -> {
                            // Step 3: Print / Invoice template styles review
                            Text(
                                text = "Invoice templates layout",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-0.5).sp
                                ),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Finalize print setups & configurations",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("STANDARD", "MINIMAL", "THERMAL").forEach { temp ->
                                    val sel = invoiceTemplateStyle == temp
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(
                                                1.5.dp,
                                                if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .background(
                                                if (sel) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable { invoiceTemplateStyle = temp }
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = when (temp) {
                                                    "STANDARD" -> Icons.Default.ReceiptLong
                                                    "MINIMAL" -> Icons.Default.Description
                                                    else -> Icons.Default.Print
                                                },
                                                contentDescription = null,
                                                tint = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                temp,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                if (currentStep > 1) {
                                    currentStep -= 1
                                }
                            },
                            enabled = currentStep > 1
                        ) {
                            Text("Back", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }

                        Button(
                            onClick = {
                                if (currentStep < 3) {
                                    currentStep += 1
                                } else {
                                    authViewModel.onboardNewShop(
                                        name = companyName,
                                        businessType = businessType,
                                        logoCategory = when(businessType) {
                                            "Classic IT" -> "it"
                                            else -> "clothing"
                                        },
                                        primaryHex = accentThemeColor,
                                        receiptFooterText = receiptFooterText,
                                        templateStyle = invoiceTemplateStyle,
                                        onComplete = onComplete
                                    )
                                }
                            },
                            enabled = currentStep < 3 || companyName.isNotEmpty(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(
                                text = if (currentStep == 3) "Initialize Enterprise" else "Next",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}
