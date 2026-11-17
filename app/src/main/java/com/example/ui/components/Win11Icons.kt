package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.DesktopShortcut
import com.example.model.DesktopWindowType
import com.example.model.InstalledApp
import com.example.model.ShortcutKind

@Composable
fun Win11StartLogo(modifier: Modifier = Modifier, size: Dp = 24.dp) {
    Canvas(modifier = modifier.size(size)) {
        val gap = this.size.width * 0.08f
        val tileW = (this.size.width - gap) / 2f
        val tileH = (this.size.height - gap) / 2f
        val radius = CornerRadius(this.size.width * 0.08f, this.size.width * 0.08f)

        val topLeftBrush = Brush.linearGradient(
            colors = listOf(Color(0xFF70D8FF), Color(0xFF2FA8F6)),
            start = Offset.Zero,
            end = Offset(tileW, tileH)
        )
        val topRightBrush = Brush.linearGradient(
            colors = listOf(Color(0xFF4FC3F7), Color(0xFF198BE6)),
            start = Offset(tileW + gap, 0f),
            end = Offset(this.size.width, tileH)
        )
        val bottomLeftBrush = Brush.linearGradient(
            colors = listOf(Color(0xFF3CB5F2), Color(0xFF0E74D1)),
            start = Offset(0f, tileH + gap),
            end = Offset(tileW, this.size.height)
        )
        val bottomRightBrush = Brush.linearGradient(
            colors = listOf(Color(0xFF2196F3), Color(0xFF005BB5)),
            start = Offset(tileW + gap, tileH + gap),
            end = Offset(this.size.width, this.size.height)
        )

        drawRoundRect(topLeftBrush, Offset(0f, 0f), Size(tileW, tileH), radius)
        drawRoundRect(topRightBrush, Offset(tileW + gap, 0f), Size(tileW, tileH), radius)
        drawRoundRect(bottomLeftBrush, Offset(0f, tileH + gap), Size(tileW, tileH), radius)
        drawRoundRect(bottomRightBrush, Offset(tileW + gap, tileH + gap), Size(tileW, tileH), radius)
    }
}

@Composable
fun Win11ThisPcIcon(modifier: Modifier = Modifier, size: Dp = 44.dp) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        // Stand base
        drawRoundRect(
            color = Color(0xFF9AA7B8),
            topLeft = Offset(w * 0.34f, h * 0.84f),
            size = Size(w * 0.32f, h * 0.07f),
            cornerRadius = CornerRadius(w * 0.03f, w * 0.03f)
        )
        // Stand neck
        drawRect(
            color = Color(0xFF7B8797),
            topLeft = Offset(w * 0.45f, h * 0.72f),
            size = Size(w * 0.10f, h * 0.13f)
        )
        // Monitor bezel
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(Color(0xFF424B5A), Color(0xFF2A313C))),
            topLeft = Offset(w * 0.08f, h * 0.14f),
            size = Size(w * 0.84f, h * 0.59f),
            cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
        )
        // Screen bloom wallpaper
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF60CDFF), Color(0xFF0078D4), Color(0xFF1A3A7A)),
                start = Offset(w * 0.12f, h * 0.18f),
                end = Offset(w * 0.88f, h * 0.68f)
            ),
            topLeft = Offset(w * 0.13f, h * 0.19f),
            size = Size(w * 0.74f, h * 0.49f),
            cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
        )
        // Subtle glow petal on screen
        drawCircle(
            color = Color(0x55FFFFFF),
            radius = w * 0.14f,
            center = Offset(w * 0.50f, h * 0.43f)
        )
    }
}

@Composable
fun Win11FolderIcon(modifier: Modifier = Modifier, size: Dp = 44.dp) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        // Back folder tab
        val tabPath = Path().apply {
            moveTo(w * 0.08f, h * 0.28f)
            lineTo(w * 0.08f, h * 0.22f)
            quadraticTo(w * 0.08f, h * 0.16f, w * 0.16f, h * 0.16f)
            lineTo(w * 0.38f, h * 0.16f)
            lineTo(w * 0.46f, h * 0.24f)
            lineTo(w * 0.86f, h * 0.24f)
            quadraticTo(w * 0.92f, h * 0.24f, w * 0.92f, h * 0.31f)
            lineTo(w * 0.92f, h * 0.40f)
            lineTo(w * 0.08f, h * 0.40f)
            close()
        }
        drawPath(tabPath, color = Color(0xFFE09F24))

        // Front golden leaf
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFFD86F), Color(0xFFF6B93B))
            ),
            topLeft = Offset(w * 0.08f, h * 0.30f),
            size = Size(w * 0.84f, h * 0.54f),
            cornerRadius = CornerRadius(w * 0.09f, w * 0.09f)
        )
        // Top highlight line
        drawLine(
            color = Color(0x88FFF5CC),
            start = Offset(w * 0.13f, h * 0.32f),
            end = Offset(w * 0.87f, h * 0.32f),
            strokeWidth = h * 0.025f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun Win11RecycleBinIcon(modifier: Modifier = Modifier, size: Dp = 44.dp) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        // Bin body trapezoid
        val body = Path().apply {
            moveTo(w * 0.20f, h * 0.28f)
            lineTo(w * 0.80f, h * 0.28f)
            lineTo(w * 0.72f, h * 0.86f)
            quadraticTo(w * 0.71f, h * 0.90f, w * 0.65f, h * 0.90f)
            lineTo(w * 0.35f, h * 0.90f)
            quadraticTo(w * 0.29f, h * 0.90f, w * 0.28f, h * 0.86f)
            close()
        }
        drawPath(
            path = body,
            brush = Brush.verticalGradient(listOf(Color(0xFFD6E4F0), Color(0xFF9BB2C7)))
        )
        drawPath(
            path = body,
            color = Color(0xFF6E859B),
            style = Stroke(width = w * 0.03f)
        )
        // Top rim
        drawRoundRect(
            color = Color(0xFFE9F2FA),
            topLeft = Offset(w * 0.16f, h * 0.22f),
            size = Size(w * 0.68f, h * 0.09f),
            cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
        )
        // Subtle green recycle mark
        drawCircle(
            color = Color(0xFF2E9E5B),
            radius = w * 0.12f,
            center = Offset(w * 0.50f, h * 0.59f),
            style = Stroke(width = w * 0.045f)
        )
    }
}

@Composable
fun Win11SettingsIcon(modifier: Modifier = Modifier, size: Dp = 44.dp) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f
        // Outer gear teeth
        for (i in 0 until 8) {
            val angle = Math.toRadians(i * 45.0)
            val tx = cx + (w * 0.31f * kotlin.math.cos(angle)).toFloat()
            val ty = cy + (h * 0.31f * kotlin.math.sin(angle)).toFloat()
            drawCircle(
                color = Color(0xFF748498),
                radius = w * 0.11f,
                center = Offset(tx, ty)
            )
        }
        drawCircle(
            brush = Brush.linearGradient(listOf(Color(0xFFAAB8C8), Color(0xFF5D6D80))),
            radius = w * 0.34f,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = Color(0xFF1F2733),
            radius = w * 0.17f,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = Color(0xFF4CC2FF),
            radius = w * 0.10f,
            center = Offset(cx, cy)
        )
    }
}

@Composable
fun Win11EdgeIcon(modifier: Modifier = Modifier, size: Dp = 44.dp) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF4CD4A9), Color(0xFF1A96E8), Color(0xFF0E4D9C)),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            ),
            radius = w * 0.42f,
            center = Offset(w * 0.5f, h * 0.5f)
        )
        drawCircle(
            color = Color(0xFF0A2A5E),
            radius = w * 0.17f,
            center = Offset(w * 0.52f, h * 0.54f)
        )
        drawArc(
            color = Color(0xAA7CEBFF),
            startAngle = 190f,
            sweepAngle = 175f,
            useCenter = false,
            topLeft = Offset(w * 0.14f, h * 0.14f),
            size = Size(w * 0.72f, h * 0.72f),
            style = Stroke(width = w * 0.14f, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun Win11WidgetsIcon(modifier: Modifier = Modifier, size: Dp = 22.dp) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        drawRoundRect(
            color = Color(0xFF1D7BD8),
            topLeft = Offset(w * 0.06f, h * 0.10f),
            size = Size(w * 0.50f, h * 0.80f),
            cornerRadius = CornerRadius(w * 0.14f, w * 0.14f)
        )
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(Color(0xFF7BE0FF), Color(0xFF2FB1F5))),
            topLeft = Offset(w * 0.44f, h * 0.18f),
            size = Size(w * 0.50f, h * 0.64f),
            cornerRadius = CornerRadius(w * 0.14f, w * 0.14f)
        )
    }
}

@Composable
fun Win11TaskViewIcon(modifier: Modifier = Modifier, size: Dp = 22.dp, tint: Color) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        drawRoundRect(
            color = tint.copy(alpha = 0.65f),
            topLeft = Offset(w * 0.10f, h * 0.18f),
            size = Size(w * 0.52f, h * 0.54f),
            cornerRadius = CornerRadius(w * 0.10f, w * 0.10f),
            style = Stroke(width = w * 0.08f)
        )
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.36f, h * 0.30f),
            size = Size(w * 0.54f, h * 0.54f),
            cornerRadius = CornerRadius(w * 0.10f, w * 0.10f),
            style = Stroke(width = w * 0.09f)
        )
    }
}

@Composable
fun Win11TrayBatteryIcon(
    percent: Int,
    isCharging: Boolean,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 18.dp
) {
    val clamped = percent.coerceIn(0, 100)
    val fillColor = when {
        isCharging -> Color(0xFF4CD4A9)
        clamped <= 15 -> Color(0xFFFF6B6B)
        clamped <= 25 -> Color(0xFFF59E0B)
        else -> tint
    }
    Canvas(modifier = modifier.size(width = size * 1.25f, height = size * 0.78f)) {
        val w = this.size.width
        val h = this.size.height
        val strokeW = (h * 0.11f).coerceAtLeast(1.4f)
        val bodyW = w * 0.84f
        val bodyH = h * 0.84f
        val topY = (h - bodyH) / 2f

        // Outer horizontal battery shell
        drawRoundRect(
            color = tint.copy(alpha = 0.9f),
            topLeft = Offset(strokeW / 2f, topY),
            size = Size(bodyW - strokeW, bodyH),
            cornerRadius = CornerRadius(h * 0.20f, h * 0.20f),
            style = Stroke(width = strokeW)
        )

        // Right positive terminal nub
        drawRoundRect(
            color = tint.copy(alpha = 0.85f),
            topLeft = Offset(bodyW + w * 0.02f, h * 0.31f),
            size = Size(w * 0.09f, h * 0.38f),
            cornerRadius = CornerRadius(h * 0.10f, h * 0.10f)
        )

        // Inner charge level bar
        val pad = strokeW * 1.65f
        val maxInnerW = (bodyW - strokeW - pad * 2f).coerceAtLeast(2f)
        val innerW = (maxInnerW * (clamped / 100f)).coerceAtLeast(if (clamped > 0) 2.5f else 0f)
        val innerH = (bodyH - pad * 2f).coerceAtLeast(2f)
        if (innerW > 0f) {
            drawRoundRect(
                color = fillColor,
                topLeft = Offset(strokeW / 2f + pad, topY + pad),
                size = Size(innerW, innerH),
                cornerRadius = CornerRadius(h * 0.09f, h * 0.09f)
            )
        }

        // Small charging lightning bolt overlay
        if (isCharging) {
            val bolt = Path().apply {
                moveTo(w * 0.48f, h * 0.10f)
                lineTo(w * 0.33f, h * 0.54f)
                lineTo(w * 0.45f, h * 0.54f)
                lineTo(w * 0.38f, h * 0.92f)
                lineTo(w * 0.57f, h * 0.46f)
                lineTo(w * 0.45f, h * 0.46f)
                close()
            }
            drawPath(bolt, color = Color(0xFF0E1722))
            drawPath(bolt, color = Color.White, style = Stroke(width = strokeW * 0.45f))
        }
    }
}

@Composable
fun Win11TrayVolumeIcon(
    volume: Int,
    maxVolume: Int,
    isMuted: Boolean,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 16.dp
) {
    val ratio = if (isMuted || maxVolume <= 0) 0f else (volume.toFloat() / maxVolume.toFloat()).coerceIn(0f, 1f)
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeW = (w * 0.095f).coerceAtLeast(1.4f)

        // Speaker cone
        val speaker = Path().apply {
            moveTo(w * 0.10f, h * 0.38f)
            lineTo(w * 0.28f, h * 0.38f)
            lineTo(w * 0.50f, h * 0.19f)
            lineTo(w * 0.50f, h * 0.81f)
            lineTo(w * 0.28f, h * 0.62f)
            lineTo(w * 0.10f, h * 0.62f)
            close()
        }
        drawPath(speaker, color = tint)

        if (isMuted || ratio <= 0.01f) {
            // Muted X
            drawLine(
                color = Color(0xFFFF6B6B),
                start = Offset(w * 0.64f, h * 0.36f),
                end = Offset(w * 0.90f, h * 0.64f),
                strokeWidth = strokeW * 1.15f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFFFF6B6B),
                start = Offset(w * 0.90f, h * 0.36f),
                end = Offset(w * 0.64f, h * 0.64f),
                strokeWidth = strokeW * 1.15f,
                cap = StrokeCap.Round
            )
        } else {
            // 1st sound wave
            drawArc(
                color = tint,
                startAngle = -45f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(w * 0.44f, h * 0.31f),
                size = Size(w * 0.24f, h * 0.38f),
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
            // 2nd sound wave
            if (ratio > 0.33f) {
                drawArc(
                    color = tint.copy(alpha = 0.9f),
                    startAngle = -52f,
                    sweepAngle = 104f,
                    useCenter = false,
                    topLeft = Offset(w * 0.48f, h * 0.20f),
                    size = Size(w * 0.34f, h * 0.60f),
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )
            }
            // 3rd sound wave
            if (ratio > 0.68f) {
                drawArc(
                    color = tint.copy(alpha = 0.8f),
                    startAngle = -56f,
                    sweepAngle = 112f,
                    useCenter = false,
                    topLeft = Offset(w * 0.52f, h * 0.10f),
                    size = Size(w * 0.42f, h * 0.80f),
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )
            }
        }
    }
}

@Composable
fun Win11TrayWifiIcon(
    isWifiConnected: Boolean,
    isCellularConnected: Boolean,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 16.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeW = (w * 0.105f).coerceAtLeast(1.4f)

        if (isWifiConnected) {
            // Windows 11 crisp 3-arc Wi-Fi + bottom dot
            drawCircle(
                color = tint,
                radius = w * 0.085f,
                center = Offset(w * 0.50f, h * 0.80f)
            )
            drawArc(
                color = tint,
                startAngle = 225f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(w * 0.29f, h * 0.50f),
                size = Size(w * 0.42f, h * 0.44f),
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
            drawArc(
                color = tint,
                startAngle = 222f,
                sweepAngle = 96f,
                useCenter = false,
                topLeft = Offset(w * 0.16f, h * 0.33f),
                size = Size(w * 0.68f, h * 0.70f),
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
            drawArc(
                color = tint,
                startAngle = 218f,
                sweepAngle = 104f,
                useCenter = false,
                topLeft = Offset(w * 0.04f, h * 0.16f),
                size = Size(w * 0.92f, h * 0.96f),
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
        } else if (isCellularConnected) {
            // Crisp ascending signal bars
            val barW = w * 0.15f
            val heights = listOf(0.28f, 0.48f, 0.68f, 0.86f)
            heights.forEachIndexed { idx, hf ->
                val x = w * (0.10f + idx * 0.22f)
                val bh = h * hf
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(x, h * 0.90f - bh),
                    size = Size(barW, bh),
                    cornerRadius = CornerRadius(barW * 0.35f, barW * 0.35f)
                )
            }
        } else {
            // Windows 11 disconnected globe wireframe
            val r = w * 0.36f
            val cx = w * 0.5f
            val cy = h * 0.5f
            drawCircle(
                color = tint.copy(alpha = 0.75f),
                radius = r,
                center = Offset(cx, cy),
                style = Stroke(width = strokeW)
            )
            drawOval(
                color = tint.copy(alpha = 0.75f),
                topLeft = Offset(cx - r * 0.48f, cy - r),
                size = Size(r * 0.96f, r * 2f),
                style = Stroke(width = strokeW * 0.85f)
            )
            drawLine(
                color = tint.copy(alpha = 0.75f),
                start = Offset(cx - r, cy),
                end = Offset(cx + r, cy),
                strokeWidth = strokeW * 0.85f
            )
            // Small red/amber slash badge
            drawCircle(
                color = Color(0xFFFF6B6B),
                radius = w * 0.16f,
                center = Offset(w * 0.80f, h * 0.78f)
            )
        }
    }
}

@Composable
fun Win11NotificationBellIcon(
    hasBadge: Boolean,
    tint: Color,
    badgeColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 15.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeW = (w * 0.095f).coerceAtLeast(1.3f)

        val bell = Path().apply {
            moveTo(w * 0.50f, h * 0.14f)
            cubicTo(w * 0.30f, h * 0.14f, w * 0.24f, h * 0.32f, w * 0.24f, h * 0.52f)
            lineTo(w * 0.16f, h * 0.72f)
            lineTo(w * 0.84f, h * 0.72f)
            lineTo(w * 0.76f, h * 0.52f)
            cubicTo(w * 0.76f, h * 0.32f, w * 0.70f, h * 0.14f, w * 0.50f, h * 0.14f)
            close()
        }
        if (hasBadge) {
            drawPath(bell, color = tint.copy(alpha = 0.18f))
        }
        drawPath(bell, color = tint, style = Stroke(width = strokeW))

        // Clapper arc at bottom
        drawArc(
            color = tint,
            startAngle = 10f,
            sweepAngle = 160f,
            useCenter = false,
            topLeft = Offset(w * 0.38f, h * 0.70f),
            size = Size(w * 0.24f, h * 0.18f),
            style = Stroke(width = strokeW, cap = StrokeCap.Round)
        )

        if (hasBadge) {
            drawCircle(
                color = badgeColor,
                radius = w * 0.17f,
                center = Offset(w * 0.78f, h * 0.24f)
            )
        }
    }
}

@Composable
fun AppIconImage(
    app: InstalledApp,
    size: Dp,
    modifier: Modifier = Modifier
) {
    if (app.icon != null) {
        Image(
            bitmap = app.icon,
            contentDescription = app.label,
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(10.dp)),
            contentScale = ContentScale.Fit
        )
    } else {
        Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Rounded.Apps,
                contentDescription = app.label,
                tint = Color(0xFF60CDFF),
                modifier = Modifier.fillMaxSize(0.8f)
            )
        }
    }
}

@Composable
fun ShortcutIconView(
    shortcut: DesktopShortcut,
    size: Dp,
    modifier: Modifier = Modifier
) {
    when (shortcut.kind) {
        ShortcutKind.THIS_PC -> Win11ThisPcIcon(modifier = modifier, size = size)
        ShortcutKind.USER_FILES -> Win11FolderIcon(modifier = modifier, size = size)
        ShortcutKind.BROWSER -> Win11EdgeIcon(modifier = modifier, size = size)
        ShortcutKind.SETTINGS -> Win11SettingsIcon(modifier = modifier, size = size)
        ShortcutKind.RECYCLE_BIN -> Win11RecycleBinIcon(modifier = modifier, size = size)
        ShortcutKind.APP -> {
            val app = shortcut.app
            if (app != null) {
                AppIconImage(app = app, size = size, modifier = modifier)
            } else {
                Win11FolderIcon(modifier = modifier, size = size)
            }
        }
    }
}

@Composable
fun WindowTypeIconView(
    type: DesktopWindowType,
    size: Dp,
    modifier: Modifier = Modifier
) {
    when (type) {
        DesktopWindowType.FILE_EXPLORER -> Win11FolderIcon(modifier = modifier, size = size)
        DesktopWindowType.THIS_PC -> Win11ThisPcIcon(modifier = modifier, size = size)
        DesktopWindowType.USER_FILES -> Win11FolderIcon(modifier = modifier, size = size)
        DesktopWindowType.SETTINGS_PERSONALIZE -> Win11SettingsIcon(modifier = modifier, size = size)
        DesktopWindowType.TASKBAR_PANEL_SETTINGS -> Win11SettingsIcon(modifier = modifier, size = size)
        DesktopWindowType.RECYCLE_BIN_APPS -> Win11RecycleBinIcon(modifier = modifier, size = size)
    }
}
