package com.popalay.retrogamer

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkColorPalette = darkColors(
    primary = Color(0xFF242A2F),
    primaryVariant = Color(0xFF242A2F),
    background = Color(0xFF242A2F),
    onPrimary = Color.White,
    onBackground = Color.White,
)

val shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(0.dp),
)

val typography = Typography(
    body1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
)

@Composable
fun TetrisComposeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = DarkColorPalette,
        typography = typography,
        shapes = shapes,
        content = content
    )
}