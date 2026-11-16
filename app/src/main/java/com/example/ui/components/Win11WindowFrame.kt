package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CropSquare
import androidx.compose.material.icons.rounded.FilterNone
import androidx.compose.material.icons.rounded.Minimize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.model.DesktopWindowState
import com.example.ui.theme.LocalWin11Colors
import com.example.ui.theme.WinCloseRed
import kotlin.math.roundToInt

@Composable
fun Win11WindowFrame(
    windowState: DesktopWindowState,
    isFocused: Boolean,
    cascadeIndex: Int = 0,
    onFocus: () -> Unit,
    onMinimize: () -> Unit,
    onToggleMaximize: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val winColors = LocalWin11Colors.current
    val density = LocalDensity.current
    val latestOnFocus by rememberUpdatedState(onFocus)
    val latestOnToggleMaximize by rememberUpdatedState(onToggleMaximize)

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val canvasWidthPx = with(density) { maxWidth.toPx() }
        val canvasHeightPx = with(density) { maxHeight.toPx() }

        val minWidthPx = with(density) { minOf(250.dp, maxWidth * 0.82f).toPx() }
        val minHeightPx = with(density) { minOf(230.dp, maxHeight * 0.72f).toPx() }

        val defaultWidthDp = if (maxWidth < 520.dp) {
            (maxWidth * 0.90f).coerceAtLeast(250.dp.coerceAtMost(maxWidth))
        } else {
            (maxWidth * 0.80f).coerceAtMost(760.dp)
        }
        val defaultHeightDp = if (maxHeight < 600.dp) {
            (maxHeight * 0.74f).coerceAtLeast(230.dp.coerceAtMost(maxHeight))
        } else {
            (maxHeight * 0.78f).coerceAtMost(580.dp)
        }

        val defaultWidthPx = with(density) { defaultWidthDp.toPx() }
        val defaultHeightPx = with(density) { defaultHeightDp.toPx() }
        val cascadeOffsetPx = with(density) { ((cascadeIndex % 4) * 14).dp.toPx() }

        val initialPosX = ((canvasWidthPx - defaultWidthPx) / 2f + cascadeOffsetPx)
            .coerceIn(0f, (canvasWidthPx - defaultWidthPx).coerceAtLeast(0f))
        val initialPosY = ((canvasHeightPx - defaultHeightPx) / 2f + cascadeOffsetPx)
            .coerceIn(0f, (canvasHeightPx - defaultHeightPx).coerceAtLeast(0f))

        var posX by remember(windowState.type) { mutableFloatStateOf(initialPosX) }
        var posY by remember(windowState.type) { mutableFloatStateOf(initialPosY) }
        var winWidthPx by remember(windowState.type) { mutableFloatStateOf(defaultWidthPx) }
        var winHeightPx by remember(windowState.type) { mutableFloatStateOf(defaultHeightPx) }

        val latestCanvasW by rememberUpdatedState(canvasWidthPx)
        val latestCanvasH by rememberUpdatedState(canvasHeightPx)
        val latestMinW by rememberUpdatedState(minWidthPx)
        val latestMinH by rememberUpdatedState(minHeightPx)

        val effectiveWidthPx = winWidthPx.coerceIn(minWidthPx, canvasWidthPx.coerceAtLeast(minWidthPx))
        val effectiveHeightPx = winHeightPx.coerceIn(minHeightPx, canvasHeightPx.coerceAtLeast(minHeightPx))
        val minVisibleTitlePx = with(density) { 72.dp.toPx() }.coerceAtMost(effectiveWidthPx)
        val minPosX = -(effectiveWidthPx - minVisibleTitlePx)
        val maxPosX = (canvasWidthPx - minVisibleTitlePx).coerceAtLeast(minPosX)
        val maxPosY = (canvasHeightPx - with(density) { 44.dp.toPx() }).coerceAtLeast(0f)

        val clampedPosX = posX.coerceIn(minPosX, maxPosX)
        val clampedPosY = posY.coerceIn(0f, maxPosY)

        val isMaximized = windowState.isMaximized
        val shape = if (isMaximized) RoundedCornerShape(8.dp) else RoundedCornerShape(12.dp)

        val windowModifier = if (isMaximized) {
            Modifier
                .fillMaxSize()
                .padding(4.dp)
        } else {
            Modifier
                .offset { IntOffset(clampedPosX.roundToInt(), clampedPosY.roundToInt()) }
                .width(with(density) { effectiveWidthPx.toDp() })
                .height(with(density) { effectiveHeightPx.toDp() })
        }

        Surface(
            modifier = windowModifier
                .shadow(if (isFocused) 24.dp else 10.dp, shape)
                .border(
                    width = 1.dp,
                    color = if (isFocused) winColors.border else winColors.subtleBorder,
                    shape = shape
                )
                .pointerInput(windowState.type) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            if (event.changes.any { it.pressed && !it.previousPressed }) {
                                latestOnFocus()
                            }
                        }
                    }
                }
                .testTag("window_${windowState.type.name}"),
            shape = shape,
            color = winColors.micaSurface
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Windows 11 Title Bar: drag to move, double-tap to maximize/cascade
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .background(winColors.cardSurface.copy(alpha = 0.72f))
                            .pointerInput(windowState.type, isMaximized) {
                                detectTapGestures(
                                    onDoubleTap = { latestOnToggleMaximize() }
                                )
                            }
                            .then(
                                if (!isMaximized) {
                                    Modifier.pointerInput(windowState.type) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            val curW = winWidthPx.coerceIn(latestMinW, latestCanvasW)
                                            val curMinVisible = with(density) { 72.dp.toPx() }.coerceAtMost(curW)
                                            val curMinX = -(curW - curMinVisible)
                                            val curMaxX = (latestCanvasW - curMinVisible).coerceAtLeast(curMinX)
                                            val curMaxY = (latestCanvasH - with(density) { 44.dp.toPx() }).coerceAtLeast(0f)
                                            posX = (posX + dragAmount.x).coerceIn(curMinX, curMaxX)
                                            posY = (posY + dragAmount.y).coerceIn(0f, curMaxY)
                                        }
                                    }
                                } else {
                                    Modifier
                                }
                            )
                            .padding(start = 10.dp, end = 4.dp)
                            .testTag("window_title_bar_${windowState.type.name}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            WindowTypeIconView(type = windowState.type, size = 18.dp)
                            Text(
                                text = windowState.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                                color = winColors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Classic Windows 11 top-right Minimize, Cascade/Maximize, Close controls
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            WindowCaptionButton(
                                onClick = onMinimize,
                                contentDescription = "Minimize",
                                tint = winColors.textPrimary,
                                testTag = "window_minimize_${windowState.type.name}"
                            ) {
                                Icon(
                                    Icons.Rounded.Minimize,
                                    contentDescription = "Minimize",
                                    tint = winColors.textPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            WindowCaptionButton(
                                onClick = onToggleMaximize,
                                contentDescription = if (isMaximized) "Cascade / Restore" else "Maximize",
                                tint = winColors.textPrimary,
                                testTag = "window_maximize_${windowState.type.name}"
                            ) {
                                Icon(
                                    if (isMaximized) Icons.Rounded.FilterNone else Icons.Rounded.CropSquare,
                                    contentDescription = if (isMaximized) "Cascade / Restore" else "Maximize",
                                    tint = winColors.textPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            WindowCaptionButton(
                                onClick = onClose,
                                contentDescription = "Close",
                                tint = winColors.textPrimary,
                                containerColor = WinCloseRed.copy(alpha = 0.14f),
                                testTag = "window_close_${windowState.type.name}"
                            ) {
                                Icon(
                                    Icons.Rounded.Close,
                                    contentDescription = "Close",
                                    tint = winColors.textPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(thickness = 1.dp, color = winColors.subtleBorder)

                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        content()
                    }
                }

                // Floating Window Edge & Corner Resize Handles
                if (!isMaximized) {
                    // Left edge resize
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .width(10.dp)
                            .fillMaxHeight()
                            .padding(vertical = 18.dp)
                            .pointerInput(windowState.type) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val rightEdge = (posX + winWidthPx).coerceAtMost(latestCanvasW)
                                    val newX = (posX + dragAmount.x).coerceIn(0f, (rightEdge - latestMinW).coerceAtLeast(0f))
                                    val newW = (rightEdge - newX).coerceIn(latestMinW, latestCanvasW)
                                    posX = newX
                                    winWidthPx = newW
                                }
                            }
                    )

                    // Right edge resize
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .width(10.dp)
                            .fillMaxHeight()
                            .padding(top = 44.dp, bottom = 24.dp)
                            .pointerInput(windowState.type) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val maxAllowedW = (latestCanvasW - posX.coerceAtLeast(0f)).coerceAtLeast(latestMinW)
                                    winWidthPx = (winWidthPx + dragAmount.x).coerceIn(latestMinW, maxAllowedW)
                                }
                            }
                    )

                    // Bottom edge resize
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(10.dp)
                            .padding(horizontal = 24.dp)
                            .pointerInput(windowState.type) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val maxAllowedH = (latestCanvasH - posY.coerceAtLeast(0f)).coerceAtLeast(latestMinH)
                                    winHeightPx = (winHeightPx + dragAmount.y).coerceIn(latestMinH, maxAllowedH)
                                }
                            }
                    )

                    // Top edge resize (left/middle strip, avoids top-right caption buttons)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .fillMaxWidth()
                            .padding(end = 124.dp)
                            .height(6.dp)
                            .pointerInput(windowState.type) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val bottomEdge = (posY + winHeightPx).coerceAtMost(latestCanvasH)
                                    val newY = (posY + dragAmount.y).coerceIn(0f, (bottomEdge - latestMinH).coerceAtLeast(0f))
                                    val newH = (bottomEdge - newY).coerceIn(latestMinH, latestCanvasH)
                                    posY = newY
                                    winHeightPx = newH
                                }
                            }
                    )

                    // Bottom-Left corner resize
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .size(22.dp)
                            .pointerInput(windowState.type) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val rightEdge = (posX + winWidthPx).coerceAtMost(latestCanvasW)
                                    val newX = (posX + dragAmount.x).coerceIn(0f, (rightEdge - latestMinW).coerceAtLeast(0f))
                                    val newW = (rightEdge - newX).coerceIn(latestMinW, latestCanvasW)
                                    val maxAllowedH = (latestCanvasH - posY.coerceAtLeast(0f)).coerceAtLeast(latestMinH)
                                    posX = newX
                                    winWidthPx = newW
                                    winHeightPx = (winHeightPx + dragAmount.y).coerceIn(latestMinH, maxAllowedH)
                                }
                            }
                    )

                    // Bottom-Right corner resize with visible Windows diagonal grip
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(24.dp)
                            .pointerInput(windowState.type) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val maxAllowedW = (latestCanvasW - posX.coerceAtLeast(0f)).coerceAtLeast(latestMinW)
                                    val maxAllowedH = (latestCanvasH - posY.coerceAtLeast(0f)).coerceAtLeast(latestMinH)
                                    winWidthPx = (winWidthPx + dragAmount.x).coerceIn(latestMinW, maxAllowedW)
                                    winHeightPx = (winHeightPx + dragAmount.y).coerceIn(latestMinH, maxAllowedH)
                                }
                            }
                            .testTag("window_resize_br_${windowState.type.name}"),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        val gripColor = winColors.textSecondary.copy(alpha = 0.72f)
                        Canvas(
                            modifier = Modifier
                                .padding(end = 5.dp, bottom = 5.dp)
                                .size(11.dp)
                        ) {
                            drawLine(
                                color = gripColor,
                                start = Offset(size.width * 0.2f, size.height),
                                end = Offset(size.width, size.height * 0.2f),
                                strokeWidth = 1.6f,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = gripColor,
                                start = Offset(size.width * 0.6f, size.height),
                                end = Offset(size.width, size.height * 0.6f),
                                strokeWidth = 1.6f,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WindowCaptionButton(
    onClick: () -> Unit,
    contentDescription: String,
    tint: Color,
    testTag: String,
    containerColor: Color = Color.Transparent,
    icon: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = 36.dp, height = 32.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(containerColor)
            .clickable(onClickLabel = contentDescription, onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}
