package com.omlakhera.kronoza.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omlakhera.kronoza.ui.theme.LocalKronozaColors

enum class LogoVariation(val label: String) {
    PRIMARY("Primary"),
    STANDALONE("Standalone Icon"),
    MONOCHROME("Monochrome"),
    DARK_BG("Dark Background"),
    LIGHT_BG("Light Background")
}

@Composable
fun KronozaLogoMark(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    variation: LogoVariation = LogoVariation.PRIMARY
) {
    val ext = LocalKronozaColors.current
    val primary = MaterialTheme.colorScheme.primary
    val bgColor: Color
    val frameColor: Color
    val arcColor: Color
    val accentDot: Color

    when (variation) {
        LogoVariation.PRIMARY -> {
            bgColor = primary
            frameColor = Color.White
            arcColor = ext.lavenderContainer
            accentDot = ext.mintContainer
        }
        LogoVariation.STANDALONE -> {
            bgColor = primary
            frameColor = Color.White
            arcColor = ext.skyBlueContainer
            accentDot = Color.White
        }
        LogoVariation.MONOCHROME -> {
            bgColor = ext.textPrimary
            frameColor = ext.card
            arcColor = ext.card.copy(alpha = 0.65f)
            accentDot = ext.card
        }
        LogoVariation.DARK_BG -> {
            bgColor = Color(0xFF222532)
            frameColor = Color(0xFFF4F5FA)
            arcColor = primary
            accentDot = ext.mintAccent
        }
        LogoVariation.LIGHT_BG -> {
            bgColor = ext.lavenderContainer
            frameColor = primary
            arcColor = primary.copy(alpha = 0.45f)
            accentDot = ext.mintAccent
        }
    }

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val corner = w * 0.27f

        // Base squircle tile
        drawRoundRect(
            color = bgColor,
            topLeft = Offset.Zero,
            size = this.size,
            cornerRadius = CornerRadius(corner, corner)
        )

        val pad = w * 0.21f
        val innerSize = w - (pad * 2f)
        val strokeW = w * 0.078f

        // Calendar architectural top bar & frame
        drawRoundRect(
            color = frameColor,
            topLeft = Offset(pad, pad + w * 0.03f),
            size = Size(innerSize, innerSize * 0.94f),
            cornerRadius = CornerRadius(w * 0.12f, w * 0.12f),
            style = Stroke(width = strokeW)
        )

        // Top calendar binding pins
        val pinYTop = pad - w * 0.025f
        val pinYBottom = pad + w * 0.08f
        drawLine(
            color = frameColor,
            start = Offset(w * 0.37f, pinYTop),
            end = Offset(w * 0.37f, pinYBottom),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
        drawLine(
            color = frameColor,
            start = Offset(w * 0.63f, pinYTop),
            end = Offset(w * 0.63f, pinYBottom),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )

        // Passage-of-time geometric progress arc inside the frame
        val arcPad = w * 0.34f
        val arcBox = w - (arcPad * 2f)
        drawArc(
            color = arcColor,
            startAngle = -65f,
            sweepAngle = 235f,
            useCenter = false,
            topLeft = Offset(arcPad, arcPad + w * 0.05f),
            size = Size(arcBox, arcBox),
            style = Stroke(width = strokeW * 0.88f, cap = StrokeCap.Round)
        )

        // Focal "now" node
        drawCircle(
            color = accentDot,
            radius = w * 0.068f,
            center = Offset(w * 0.50f, w * 0.56f)
        )
    }
}

@Composable
fun KronozaWordmark(
    modifier: Modifier = Modifier,
    variation: LogoVariation = LogoVariation.PRIMARY,
    iconSize: Dp = 36.dp,
    showTagline: Boolean = false
) {
    val ext = LocalKronozaColors.current
    val textColor = when (variation) {
        LogoVariation.DARK_BG -> Color(0xFFF4F5FA)
        LogoVariation.LIGHT_BG -> Color(0xFF202333)
        else -> ext.textPrimary
    }
    val subColor = when (variation) {
        LogoVariation.DARK_BG -> Color(0xFFA1A5B5)
        LogoVariation.LIGHT_BG -> Color(0xFF85899A)
        else -> ext.textSecondary
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        KronozaLogoMark(size = iconSize, variation = variation)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = "KRONOZA",
                style = MaterialTheme.typography.titleLarge.copy(
                    letterSpacing = 2.6.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = textColor
            )
            if (showTagline) {
                Text(
                    text = "Make time work for you.",
                    style = MaterialTheme.typography.bodySmall,
                    color = subColor
                )
            }
        }
    }
}

@Composable
fun KronozaLogoVariationsSheetContent(modifier: Modifier = Modifier) {
    val ext = LocalKronozaColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Kronoza Brand Identity",
            style = MaterialTheme.typography.headlineSmall,
            color = ext.textPrimary
        )
        Text(
            text = "Geometric wordmark & icon variations across light, dark, and monochrome surfaces.",
            style = MaterialTheme.typography.bodySmall,
            color = ext.textSecondary
        )
        LogoVariation.entries.forEach { variation ->
            val cardBg = when (variation) {
                LogoVariation.DARK_BG -> Color(0xFF101116)
                LogoVariation.LIGHT_BG -> Color(0xFFFFFFFF)
                else -> ext.mutedSurface
            }
            val labelColor = if (variation == LogoVariation.DARK_BG) Color(0xFFA1A5B5) else ext.textSecondary
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = cardBg,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ext.cardBorder, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (variation == LogoVariation.STANDALONE) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            KronozaLogoMark(size = 40.dp, variation = variation)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "App Icon",
                                style = MaterialTheme.typography.titleSmall,
                                color = ext.textPrimary
                            )
                        }
                    } else {
                        KronozaWordmark(
                            variation = variation,
                            iconSize = 36.dp,
                            showTagline = variation == LogoVariation.PRIMARY
                        )
                    }
                    Text(
                        text = variation.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = labelColor
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
fun KronozaWelcomeIllustration(modifier: Modifier = Modifier) {
    val ext = LocalKronozaColors.current
    val primary = MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(ext.lavenderContainer.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                color = ext.skyBlueContainer,
                radius = h * 0.42f,
                center = Offset(w * 0.22f, h * 0.28f)
            )
            drawCircle(
                color = ext.mintContainer,
                radius = h * 0.32f,
                center = Offset(w * 0.80f, h * 0.76f)
            )
            // Floating abstract schedule cards
            drawRoundRect(
                color = Color.White.copy(alpha = 0.92f),
                topLeft = Offset(w * 0.16f, h * 0.22f),
                size = Size(w * 0.68f, h * 0.56f),
                cornerRadius = CornerRadius(28f, 28f)
            )
            // Accent bars inside floating card
            drawRoundRect(
                color = primary,
                topLeft = Offset(w * 0.22f, h * 0.32f),
                size = Size(8f, h * 0.14f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawRoundRect(
                color = ext.lavenderContainer,
                topLeft = Offset(w * 0.27f, h * 0.32f),
                size = Size(w * 0.36f, h * 0.14f),
                cornerRadius = CornerRadius(12f, 12f)
            )
            drawRoundRect(
                color = ext.mintAccent,
                topLeft = Offset(w * 0.22f, h * 0.54f),
                size = Size(8f, h * 0.14f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawRoundRect(
                color = ext.mintContainer,
                topLeft = Offset(w * 0.27f, h * 0.54f),
                size = Size(w * 0.48f, h * 0.14f),
                cornerRadius = CornerRadius(12f, 12f)
            )
            drawCircle(
                color = ext.coralContainer,
                radius = 18f,
                center = Offset(w * 0.74f, h * 0.39f)
            )
        }
    }
}

@Composable
fun KronozaCalendarIllustration(modifier: Modifier = Modifier) {
    val ext = LocalKronozaColors.current
    val primary = MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(ext.skyBlueContainer.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            drawRoundRect(
                color = Color.White.copy(alpha = 0.94f),
                topLeft = Offset(w * 0.14f, h * 0.16f),
                size = Size(w * 0.72f, h * 0.68f),
                cornerRadius = CornerRadius(26f, 26f)
            )
            val cols = 5
            val rows = 3
            val startX = w * 0.22f
            val startY = h * 0.30f
            val stepX = (w * 0.56f) / (cols - 1)
            val stepY = (h * 0.40f) / (rows - 1)
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val cx = startX + c * stepX
                    val cy = startY + r * stepY
                    val dotColor = when {
                        r == 1 && c == 2 -> primary
                        r == 0 && c == 4 -> ext.coralAccent
                        r == 2 && c == 1 -> ext.mintAccent
                        else -> ext.lavenderContainer
                    }
                    val radius = if (r == 1 && c == 2) 17f else 11f
                    drawCircle(color = dotColor, radius = radius, center = Offset(cx, cy))
                }
            }
        }
    }
}

@Composable
fun KronozaTasksIllustration(modifier: Modifier = Modifier) {
    val ext = LocalKronozaColors.current
    val primary = MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(ext.mintContainer.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val rowColors = listOf(ext.lavenderContainer, ext.skyBlueContainer, ext.coralContainer)
            val dotColors = listOf(primary, ext.skyBlueAccent, ext.coralAccent)
            rowColors.forEachIndexed { index, bg ->
                val topY = h * (0.20f + index * 0.22f)
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.94f),
                    topLeft = Offset(w * 0.14f, topY),
                    size = Size(w * 0.72f, h * 0.16f),
                    cornerRadius = CornerRadius(18f, 18f)
                )
                drawCircle(
                    color = dotColors[index],
                    radius = 11f,
                    center = Offset(w * 0.21f, topY + h * 0.08f)
                )
                drawRoundRect(
                    color = bg,
                    topLeft = Offset(w * 0.27f, topY + h * 0.045f),
                    size = Size(w * (0.46f - index * 0.06f), h * 0.07f),
                    cornerRadius = CornerRadius(10f, 10f)
                )
            }
        }
    }
}
