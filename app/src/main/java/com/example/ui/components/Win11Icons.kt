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
        DesktopWindowType.RECYCLE_BIN_APPS -> Win11RecycleBinIcon(modifier = modifier, size = size)
    }
}
