package com.omnishop.erp.core.designsystem

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- TABULAR NUMBERS UTILITY ---
// Forces the font to render numbers with fixed widths so tabular data doesn't jump
fun TextStyle.toTabular(): TextStyle {
    return this.copy(
        fontFeatureSettings = "tnum",
        fontFamily = FontFamily.Monospace // Monospace typography pairing
    )
}

// --- GLASSMORPHISM & DEPTH CARDS ---
enum class LuxCardVariant {
    Glass, Elevated, Outlined, GradientBorder
}

@Composable
fun LuxCard(
    modifier: Modifier = Modifier,
    variant: LuxCardVariant = LuxCardVariant.Glass,
    cornerRadius: Dp = 16.dp,
    borderColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    
    val bgModifier = when (variant) {
        LuxCardVariant.Glass -> {
            Modifier
                .shadow(
                    elevation = 8.dp,
                    shape = shape,
                    clip = false,
                    ambientColor = Color.Black.copy(alpha = 0.35f),
                    spotColor = Color.Black.copy(alpha = 0.5f)
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.White.copy(alpha = 0.02f)
                        )
                    ),
                    shape = shape
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.18f),
                            Color.White.copy(alpha = 0.03f)
                        )
                    ),
                    shape = shape
                )
        }
        LuxCardVariant.GradientBorder -> {
            Modifier
                .shadow(elevation = 6.dp, shape = shape)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shape = shape
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.tertiary
                        )
                    ),
                    shape = shape
                )
        }
        LuxCardVariant.Elevated -> {
            Modifier
                .shadow(
                    elevation = 12.dp,
                    shape = shape,
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    spotColor = Color.Black.copy(alpha = 0.3f)
                )
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = shape
                )
                .border(1.dp, borderColor, shape)
        }
        LuxCardVariant.Outlined -> {
            Modifier
                .background(
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.2f),
                    shape = shape
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    shape = shape
                )
        }
    }

    Box(
        modifier = modifier
            .then(bgModifier)
            .clip(shape),
        contentAlignment = Alignment.Center,
        content = content
    )
}

// --- GLASS SURFACE CONTAINER ---
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    glowColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    
    Box(
        modifier = modifier
            .shadow(
                elevation = 16.dp,
                shape = shape,
                ambientColor = glowColor,
                spotColor = Color.Black.copy(alpha = 0.45f)
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF141520).copy(alpha = 0.85f),
                        Color(0xFF0F1016).copy(alpha = 0.95f)
                    )
                ),
                shape = shape
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.02f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
                    )
                ),
                shape = shape
            )
            .clip(shape),
        content = content
    )
}

// --- HAPTIC BUTTON ---
@Composable
fun HapticButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    hapticType: HapticFeedbackType = HapticFeedbackType.LongPress,
    rippleColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable RowScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth real-time fluid spring transitions matching CSS/React hover animations
    val scale by animateFloatAsState(
        targetValue = when {
            !enabled -> 1.0f
            isPressed -> 0.94f
            isHovered -> 1.05f
            else -> 1.0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "hapticButtonScale"
    )

    val elevation by animateFloatAsState(
        targetValue = if (isHovered && enabled) 12f else 4f,
        animationSpec = tween(durationMillis = 200),
        label = "hapticButtonElevation"
    )

    Row(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shadowElevation = elevation
            }
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(color = rippleColor),
                enabled = enabled,
                onClick = {
                    haptic.performHapticFeedback(hapticType)
                    onClick()
                }
            ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

// --- PREMIUM METRIC CARD WITH ENHANCED ANIMATION ---
@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    trend: String? = null,
    trendIsUpward: Boolean = true,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val entryProgress = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        entryProgress.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    // High performance spring scale feedback
    val interactScale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.97f
            isHovered -> 1.03f
            else -> 1.0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "metricCardScale"
    )

    val borderAlpha by animateFloatAsState(
        targetValue = if (isHovered) 0.60f else 0.15f,
        animationSpec = tween(durationMillis = 200),
        label = "metricCardBorder"
    )

    val containerModifier = modifier
        .offset(y = Dp((1f - entryProgress.value) * 24f))
        .graphicsLayer {
            scaleX = interactScale
            scaleY = interactScale
        }
        .fillMaxWidth()
        .height(115.dp)

    val finalModifier = if (onClick != null) {
        containerModifier.clickable(
            interactionSource = interactionSource,
            indication = androidx.compose.material3.ripple(color = accentColor),
            onClick = onClick
        )
    } else {
        containerModifier
    }

    LuxCard(
        modifier = finalModifier,
        variant = if (isHovered) LuxCardVariant.GradientBorder else LuxCardVariant.Glass,
        cornerRadius = 16.dp,
        borderColor = accentColor.copy(alpha = borderAlpha)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isHovered) accentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.toTabular().copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                if (trend != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        val trendColor = if (trendIsUpward) Color(0xFF4CAF50) else Color(0xFFF44336)
                        val trendSign = if (trendIsUpward) "↑" else "↓"
                        
                        Text(
                            text = "$trendSign $trend",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = trendColor
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "vs last month",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.25f),
                                accentColor.copy(alpha = 0.05f)
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(1.dp, accentColor.copy(alpha = if (isHovered) 0.8f else 0.35f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// --- SHIMMER EFFORT WITH METRIC MATRIX GRADIENT ---
@Composable
fun LuxuryShimmerBox(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp
) {
    val transition = rememberInfiniteTransition()
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val shimmerColors = listOf(
        Color.White.copy(alpha = 0.05f),
        Color.White.copy(alpha = 0.16f),
        Color.White.copy(alpha = 0.05f)
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = androidx.compose.ui.geometry.Offset(translateAnim.value - 200f, translateAnim.value - 200f),
        end = androidx.compose.ui.geometry.Offset(translateAnim.value, translateAnim.value)
    )

    Box(
        modifier = modifier
            .background(brush = brush, shape = RoundedCornerShape(cornerRadius))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(cornerRadius)
            )
    )
}

// --- REVOLUTIONARY 3D INTERACTIVE TILT & DYNAMIC GLARE HOVER MODIFIER ---
// High-fidelity physical interactive touch mechanics translating CSS / React 3D tilt
fun Modifier.tilt3d(
    maxRotationX: Float = 14f,
    maxRotationY: Float = 14f,
    scaleOnTouch: Float = 1.04f
): Modifier = composed {
    var layoutSize by remember { mutableStateOf(IntSize.Zero) }
    var rotationX by remember { mutableStateOf(0f) }
    var rotationY by remember { mutableStateOf(0f) }
    var scale by remember { mutableStateOf(1f) }
    var glareOffset by remember { mutableStateOf<Offset?>(null) }

    val animatedRotationX by animateFloatAsState(
        targetValue = rotationX,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessLow),
        label = "3dTiltX"
    )
    val animatedRotationY by animateFloatAsState(
        targetValue = rotationY,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessLow),
        label = "3dTiltY"
    )
    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMedium),
        label = "3dScale"
    )

    this
        .onSizeChanged { layoutSize = it }
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    if (layoutSize.width > 0 && layoutSize.height > 0) {
                        val firstPosition = event.changes.firstOrNull()?.position
                        
                        // Check if pointer is currently pressed or hovering inside bounds
                        val hasActivePointer = event.changes.any { it.pressed }
                        
                        if (hasActivePointer && firstPosition != null) {
                            scale = scaleOnTouch
                            // Relative offset (-1f to 1f) from center
                            val centerX = layoutSize.width / 2f
                            val centerY = layoutSize.height / 2f
                            rotationX = -((firstPosition.y - centerY) / centerY).coerceIn(-1f, 1f) * maxRotationX
                            rotationY = ((firstPosition.x - centerX) / centerX).coerceIn(-1f, 1f) * maxRotationY
                            glareOffset = firstPosition
                        } else {
                            // Reset when touch/drag released
                            scale = 1f
                            rotationX = 0f
                            rotationY = 0f
                            glareOffset = null
                        }
                    }
                }
            }
        }
        .graphicsLayer {
            rotationX = animatedRotationX
            rotationY = animatedRotationY
            scaleX = animatedScale
            scaleY = animatedScale
            cameraDistance = 15f * density
        }
        .drawWithContent {
            drawContent()
            // Metallic premium diagonal gloss flare that glides across the 3D surface
            glareOffset?.let { offset ->
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.22f),
                            Color.White.copy(alpha = 0.06f),
                            Color.Transparent
                        ),
                        center = offset,
                        radius = this.size.minDimension * 0.85f
                    ),
                    radius = this.size.minDimension * 0.85f,
                    center = offset
                )
            }
        }
}

