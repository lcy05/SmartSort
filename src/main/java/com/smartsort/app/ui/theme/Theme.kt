package com.smartsort.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** 与 UI 设计稿一致的色板（浅色）。 */
object Palette {
    val Primary = Color(0xFF0F6E56)
    val PrimaryContainer = Color(0xFFE1F5EE)
    val Bg = Color(0xFFF4F6F3)
    val Card = Color(0xFFFFFFFF)
    val Line = Color(0xFFE4E9E4)
    val Ink = Color(0xFF14211A)
    val Ink2 = Color(0xFF5C6B62)
    val Ink3 = Color(0xFF8A968E)

    val OkBg = Color(0xFFEAF3DE)
    val OkFg = Color(0xFF3B6D11)
    val FullBg = Color(0xFFFCEBEB)
    val FullFg = Color(0xFFA32D2D)
    val FullCard = Color(0xFFFFF5F5)
    val FullLine = Color(0xFFFF3B30)
    /** 满溢荧光红：边框 + 外发光呼吸用 */
    val FullGlow = Color(0xFFFF3B30)
    val ActBg = Color(0xFFE6F1FB)
    val ActFg = Color(0xFF185FA5)
    val WarnBg = Color(0xFFFAEEDA)
    val WarnFg = Color(0xFF854F0B)
    val OffBg = Color(0xFFF1EFE8)
    val OffFg = Color(0xFF6B6A64)

    private val BinK = Color(0xFF1D9E75)
    private val BinKDark = Color(0xFF0F6E56)
    private val BinY = Color(0xFFD85A30)
    private val BinYDark = Color(0xFF993C1D)
    private val BinQ = Color(0xFF888780)
    private val BinQDark = Color(0xFF5F5E5A)

    fun binBody(index: Int): Color = when (index) {
        0 -> BinK
        1 -> BinY
        else -> BinQ
    }

    fun binLid(index: Int): Color = when (index) {
        0 -> BinKDark
        1 -> BinYDark
        else -> BinQDark
    }
}

private val LightColors = lightColorScheme(
    primary = Palette.Primary,
    onPrimary = Color.White,
    primaryContainer = Palette.PrimaryContainer,
    onPrimaryContainer = Palette.Primary,
    background = Palette.Bg,
    onBackground = Palette.Ink,
    surface = Palette.Card,
    onSurface = Palette.Ink,
    surfaceVariant = Palette.OffBg,
    onSurfaceVariant = Palette.Ink2,
    outline = Palette.Line
)

private val AppTypography = Typography(
    titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium),
    titleMedium = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Medium),
    bodyMedium = TextStyle(fontSize = 12.sp),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium)
)

@Composable
fun SmartSortTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        content = content
    )
}
