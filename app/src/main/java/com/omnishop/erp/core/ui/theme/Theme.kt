package com.omnishop.erp.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle

// Luxury-tier color tokens
val DefaultTeal = Color(0xFF00ADB5)
val DefaultLightBg = Color(0xFF090A0F) // Obsidian deep luxury canvas
val GlassSurfaceColor = Color(0xFF141520)

// Safe parse hex color helper
fun parseHexColor(hexString: String, fallback: Color): Color {
    return try {
        val cleanHex = hexString.replace("#", "").trim()
        if (cleanHex.length == 6) {
            Color(android.graphics.Color.parseColor("#$cleanHex"))
        } else if (cleanHex.length == 8) {
            Color(android.graphics.Color.parseColor("#$cleanHex"))
        } else {
            fallback
        }
    } catch (e: Exception) {
        fallback
    }
}

@Composable
fun OmniShopTheme(
    shopPrimaryHex: String = "",
    darkTheme: Boolean = true, // Default to stunning dark theme for premium luxury feel
    content: @Composable () -> Unit
) {
    // Dynamic Primary Brand Color Extraction
    val activeBrandColor = if (shopPrimaryHex.isNotEmpty()) {
        parseHexColor(shopPrimaryHex, DefaultTeal)
    } else {
        DefaultTeal
    }

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = activeBrandColor,
            secondary = activeBrandColor.copy(alpha = 0.85f),
            tertiary = Color(0xFFFFB300),
            background = Color(0xFF07080B),     // Ultra deep black
            surface = Color(0xFF0F1016),        // Obsidian surface
            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onBackground = Color(0xFFF1F2F6),
            onSurface = Color(0xFFF1F2F6),
            surfaceVariant = Color(0xFF161722),  // Frosted depth level 1
            onSurfaceVariant = Color(0xFFB5B7C3)
        )
    } else {
        lightColorScheme(
            primary = activeBrandColor,
            secondary = activeBrandColor.copy(alpha = 0.8f),
            tertiary = Color(0xFFFFB300),
            background = Color(0xFFF4F6F9),     // Clean corporate light slate background
            surface = Color(0xFFFFFFFF),        // Card background
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFF0F172A),     // Dark text
            onSurface = Color(0xFF0F172A),
            surfaceVariant = Color(0xFFEDF2F7),  // Soft grey secondary surface
            onSurfaceVariant = Color(0xFF4A5568)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

val Typography = Typography(
    displayMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = (-0.5).sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        letterSpacing = 0.2.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        letterSpacing = 1.sp
    )
)

