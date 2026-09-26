package com.tigstaking.natalia

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal object NataliaPalette {
    val Background = Color(0xFFFFF9F3)
    val Surface = Color(0xFFF6EFE6)
    val Coral = Color(0xFFFF786B)
    val Teal = Color(0xFF4BC4BF)
    val Gold = Color(0xFFF5B942)
    val Violet = Color(0xFF9A8CFF)
    val Ink = Color(0xFF263247)
    val Muted = Color(0xFF667085)
    val Success = Color(0xFF56B881)
}

private val NataliaColors = lightColorScheme(
    primary = NataliaPalette.Coral,
    onPrimary = Color.White,
    secondary = NataliaPalette.Teal,
    onSecondary = NataliaPalette.Ink,
    secondaryContainer = Color(0xFFFFE2DE),
    onSecondaryContainer = NataliaPalette.Ink,
    tertiary = NataliaPalette.Gold,
    background = NataliaPalette.Background,
    onBackground = NataliaPalette.Ink,
    surface = Color.White,
    onSurface = NataliaPalette.Ink,
    surfaceVariant = NataliaPalette.Surface,
    onSurfaceVariant = NataliaPalette.Muted,
    outline = Color(0xFFD9D3CD),
)

private val NataliaTypography = Typography(
    displayLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.ExtraBold, fontSize = 36.sp, lineHeight = 42.sp, color = NataliaPalette.Ink),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp, color = NataliaPalette.Ink),
    headlineSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp, color = NataliaPalette.Ink),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp, color = NataliaPalette.Ink),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp, color = NataliaPalette.Ink),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, color = NataliaPalette.Ink),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, color = NataliaPalette.Ink),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp),
)

@Composable
internal fun NataliaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NataliaColors,
        typography = NataliaTypography,
        shapes = Shapes(
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(16.dp),
            large = RoundedCornerShape(24.dp),
            extraLarge = RoundedCornerShape(28.dp),
        ),
        content = content,
    )
}
