package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.OpenWith
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.example.data.DesktopWidgetManager
import com.example.data.Win11AppWidgetHostView
import com.example.model.AppWidgetItem
import com.example.model.DesktopWidget
import com.example.ui.theme.LocalWin11Colors
import kotlin.math.roundToInt

@Composable
fun Win11DesktopWidgetsLayer(
    widgets: List<DesktopWidget>,
    widgetManager: DesktopWidgetManager,
    editingWidgetId: Int?,
    onStartEditing: (Int) -> Unit,
    onStopEditing: () -> Unit,
    onUpdateBounds: (appWidgetId: Int, xDp: Float, yDp: Float, widthDp: Float, heightDp: Float) -> Unit,
    onReconfigureWidget: (DesktopWidget) -> Unit,
    onRemoveWidget: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (widgets.isEmpty()) return

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("desktop_widgets_layer")
    ) {
        val maxCanvasW = maxWidth.value.coerceAtLeast(160f)
        val maxCanvasH = maxHeight.value.coerceAtLeast(120f)

        widgets.forEach { widget ->
            androidx.compose.runtime.key(widget.appWidgetId) {
                DesktopWidgetFrame(
                    widget = widget,
                    widgetManager = widgetManager,
                    maxCanvasWidthDp = maxCanvasW,
                    maxCanvasHeightDp = maxCanvasH,
                    isEditing = editingWidgetId == widget.appWidgetId,
                    onEnterEditMode = { onStartEditing(widget.appWidgetId) },
                    onExitEditMode = onStopEditing,
                    onCommitBounds = { x, y, w, h ->
                        onUpdateBounds(widget.appWidgetId, x, y, w, h)
                    },
                    onReconfigure = { onReconfigureWidget(widget) },
                    onRemove = { onRemoveWidget(widget.appWidgetId) }
                )
            }
        }
    }
}

@Composable
private fun DesktopWidgetFrame(
    widget: DesktopWidget,
    widgetManager: DesktopWidgetManager,
    maxCanvasWidthDp: Float,
    maxCanvasHeightDp: Float,
    isEditing: Boolean,
    onEnterEditMode: () -> Unit,
    onExitEditMode: () -> Unit,
    onCommitBounds: (Float, Float, Float, Float) -> Unit,
    onReconfigure: () -> Unit,
    onRemove: () -> Unit
) {
    val winColors = LocalWin11Colors.current
    val density = LocalDensity.current
    val minW = widget.minWidthDp.toFloat().coerceAtLeast(88f)
    val minH = widget.minHeightDp.toFloat().coerceAtLeast(64f)

    var xDp by remember(widget.appWidgetId) { mutableFloatStateOf(widget.xDp) }
    var yDp by remember(widget.appWidgetId) { mutableFloatStateOf(widget.yDp) }
    var widthDp by remember(widget.appWidgetId) { mutableFloatStateOf(widget.widthDp) }
    var heightDp by remember(widget.appWidgetId) { mutableFloatStateOf(widget.heightDp) }

    LaunchedEffect(widget.xDp, widget.yDp, widget.widthDp, widget.heightDp) {
        xDp = widget.xDp.coerceIn(4f, (maxCanvasWidthDp - minW).coerceAtLeast(4f))
        yDp = widget.yDp.coerceIn(4f, (maxCanvasHeightDp - minH).coerceAtLeast(4f))
        widthDp = widget.widthDp.coerceIn(minW, (maxCanvasWidthDp - 8f).coerceAtLeast(minW))
        heightDp = widget.heightDp.coerceIn(minH, (maxCanvasHeightDp - 8f).coerceAtLeast(minH))
    }

    val clampedW = widthDp.coerceIn(minW, (maxCanvasWidthDp - 8f).coerceAtLeast(minW))
    val clampedH = heightDp.coerceIn(minH, (maxCanvasHeightDp - 8f).coerceAtLeast(minH))
    val clampedX = xDp.coerceIn(4f, (maxCanvasWidthDp - clampedW - 4f).coerceAtLeast(4f))
    val clampedY = yDp.coerceIn(4f, (maxCanvasHeightDp - clampedH - 4f).coerceAtLeast(4f))

    val providerInfo = remember(widget.providerPackage, widget.providerClass) {
        widgetManager.findProviderInfo(widget.providerPackage, widget.providerClass)
    }
    val canReconfigure = remember(providerInfo) {
        widgetManager.isReconfigurable(providerInfo)
    }
    var hostViewRef by remember(widget.appWidgetId) { mutableStateOf<Win11AppWidgetHostView?>(null) }

    LaunchedEffect(clampedW, clampedH, hostViewRef) {
        hostViewRef?.let { view ->
            widgetManager.updateWidgetSizeOptions(view, clampedW, clampedH)
        }
    }

    val frameShape = RoundedCornerShape(14.dp)

    Box(
        modifier = Modifier
            .offset {
                with(density) {
                    IntOffset(clampedX.dp.roundToPx(), clampedY.dp.roundToPx())
                }
            }
            .size(width = clampedW.dp, height = clampedH.dp)
            .testTag("desktop_widget_${widget.appWidgetId}")
    ) {
        // Main Widget Surface
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(frameShape)
                .background(winColors.cardSurface.copy(alpha = if (isEditing) 0.42f else 0.14f))
                .border(
                    width = if (isEditing) 2.dp else 1.dp,
                    color = if (isEditing) winColors.accent else winColors.subtleBorder.copy(alpha = 0.55f),
                    shape = frameShape
                )
        ) {
            if (providerInfo != null) {
                AndroidView(
                    factory = {
                        widgetManager.createHostView(
                            appWidgetId = widget.appWidgetId,
                            info = providerInfo,
                            widthDp = clampedW,
                            heightDp = clampedH,
                            onLongPress = onEnterEditMode
                        )?.also { created ->
                            hostViewRef = created
                        } ?: android.widget.FrameLayout(it)
                    },
                    update = { view ->
                        if (view is Win11AppWidgetHostView) {
                            view.onLongPressDetected = onEnterEditMode
                            hostViewRef = view
                        }
                    },
                    modifier = Modifier.fillMaxSize().padding(if (isEditing) 3.dp else 0.dp)
                )
            } else {
                // Fallback display if widget provider is unavailable (or during headless unit tests)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Widgets,
                        contentDescription = widget.label,
                        tint = winColors.accent,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = widget.label,
                        style = MaterialTheme.typography.titleSmall,
                        color = winColors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${clampedW.roundToInt()} × ${clampedH.roundToInt()} dp",
                        style = MaterialTheme.typography.labelSmall,
                        color = winColors.textSecondary
                    )
                }
            }

            // Edit Mode Overlay (Move drag + Header controls)
            if (isEditing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(winColors.accent.copy(alpha = 0.08f))
                        .pointerInput(widget.appWidgetId, maxCanvasWidthDp, maxCanvasHeightDp) {
                            detectDragGestures(
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val dxDp = dragAmount.x / density.density
                                    val dyDp = dragAmount.y / density.density
                                    xDp = (xDp + dxDp).coerceIn(4f, (maxCanvasWidthDp - clampedW - 4f).coerceAtLeast(4f))
                                    yDp = (yDp + dyDp).coerceIn(4f, (maxCanvasHeightDp - clampedH - 4f).coerceAtLeast(4f))
                                },
                                onDragEnd = {
                                    onCommitBounds(xDp, yDp, clampedW, clampedH)
                                }
                            )
                        }
                )

                // Top floating Edit Control Bar
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(winColors.micaSurface.copy(alpha = 0.94f))
                        .border(1.dp, winColors.border, RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.OpenWith,
                        contentDescription = "Drag to move widget",
                        tint = winColors.accent,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${clampedW.roundToInt()}×${clampedH.roundToInt()}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = winColors.textPrimary,
                        modifier = Modifier.testTag("widget_size_label_${widget.appWidgetId}")
                    )
                    if (canReconfigure) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(winColors.cardSurface)
                                .clickable(onClick = onReconfigure),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = "Configure widget",
                                tint = winColors.textPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFC42B1C).copy(alpha = 0.18f))
                            .clickable(onClick = onRemove)
                            .testTag("widget_remove_${widget.appWidgetId}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Remove widget",
                            tint = Color(0xFFFF7B72),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(winColors.accent)
                            .clickable(onClick = onExitEditMode)
                            .testTag("widget_done_${widget.appWidgetId}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Done resizing",
                            tint = if (winColors.isDark) Color(0xFF001C32) else Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            } else {
                // Subtle single-tap Resize/Edit Trigger Badge on top-right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(winColors.micaSurface.copy(alpha = 0.72f))
                        .border(1.dp, winColors.subtleBorder, CircleShape)
                        .clickable(onClick = onEnterEditMode)
                        .testTag("widget_edit_trigger_${widget.appWidgetId}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = "Atur & Ubah Ukuran Widget",
                        tint = winColors.textPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Interactive Edge & Corner Resize Handles when Editing
        if (isEditing) {
            // Right Edge Resize Handle
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(width = 16.dp, height = 44.dp)
                    .pointerInput(widget.appWidgetId, maxCanvasWidthDp) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val dxDp = dragAmount.x / density.density
                                widthDp = (widthDp + dxDp).coerceIn(minW, (maxCanvasWidthDp - clampedX - 4f).coerceAtLeast(minW))
                            },
                            onDragEnd = {
                                onCommitBounds(clampedX, clampedY, widthDp, clampedH)
                            }
                        )
                    }
                    .testTag("widget_resize_right_${widget.appWidgetId}"),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(26.dp)
                        .clip(CircleShape)
                        .background(winColors.accent)
                        .border(1.dp, Color.White.copy(alpha = 0.7f), CircleShape)
                )
            }

            // Bottom Edge Resize Handle
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .size(width = 44.dp, height = 16.dp)
                    .pointerInput(widget.appWidgetId, maxCanvasHeightDp) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val dyDp = dragAmount.y / density.density
                                heightDp = (heightDp + dyDp).coerceIn(minH, (maxCanvasHeightDp - clampedY - 4f).coerceAtLeast(minH))
                            },
                            onDragEnd = {
                                onCommitBounds(clampedX, clampedY, clampedW, heightDp)
                            }
                        )
                    }
                    .testTag("widget_resize_bottom_${widget.appWidgetId}"),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(26.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(winColors.accent)
                        .border(1.dp, Color.White.copy(alpha = 0.7f), CircleShape)
                )
            }

            // Bottom-Right Corner Diagonal Resize Handle
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(26.dp)
                    .pointerInput(widget.appWidgetId, maxCanvasWidthDp, maxCanvasHeightDp) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val dxDp = dragAmount.x / density.density
                                val dyDp = dragAmount.y / density.density
                                widthDp = (widthDp + dxDp).coerceIn(minW, (maxCanvasWidthDp - clampedX - 4f).coerceAtLeast(minW))
                                heightDp = (heightDp + dyDp).coerceIn(minH, (maxCanvasHeightDp - clampedY - 4f).coerceAtLeast(minH))
                            },
                            onDragEnd = {
                                onCommitBounds(clampedX, clampedY, widthDp, heightDp)
                            }
                        )
                    }
                    .testTag("widget_resize_br_${widget.appWidgetId}"),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(winColors.accent)
                        .border(2.dp, Color.White, CircleShape)
                )
            }

            // Bottom-Left Corner Resize Handle
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .size(24.dp)
                    .pointerInput(widget.appWidgetId, maxCanvasWidthDp, maxCanvasHeightDp) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val dxDp = dragAmount.x / density.density
                                val dyDp = dragAmount.y / density.density
                                val newX = (xDp + dxDp).coerceIn(4f, (xDp + widthDp - minW).coerceAtLeast(4f))
                                val deltaX = newX - xDp
                                xDp = newX
                                widthDp = (widthDp - deltaX).coerceAtLeast(minW)
                                heightDp = (heightDp + dyDp).coerceIn(minH, (maxCanvasHeightDp - clampedY - 4f).coerceAtLeast(minH))
                            },
                            onDragEnd = {
                                onCommitBounds(xDp, clampedY, widthDp, heightDp)
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(winColors.accent)
                        .border(1.5.dp, Color.White, CircleShape)
                )
            }
        }
    }
}

@Composable
fun Win11WidgetPickerDialog(
    availableWidgets: List<AppWidgetItem>,
    isLoading: Boolean,
    onSelectWidget: (AppWidgetItem) -> Unit,
    onDismiss: () -> Unit
) {
    val winColors = LocalWin11Colors.current
    val shape = RoundedCornerShape(14.dp)
    var searchQuery by remember { mutableStateOf("") }

    val filtered = remember(availableWidgets, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) availableWidgets
        else availableWidgets.filter {
            it.appName.lowercase().contains(q) || it.widgetLabel.lowercase().contains(q)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .widthIn(min = 300.dp, max = 480.dp)
                .fillMaxWidth()
                .heightIn(min = 280.dp, max = 560.dp)
                .shadow(24.dp, shape)
                .border(1.dp, winColors.border, shape)
                .testTag("win11_widget_picker_dialog"),
            shape = shape,
            color = winColors.micaSurface
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Widgets,
                            contentDescription = null,
                            tint = winColors.accent,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Tambah Widget Aplikasi",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = winColors.textPrimary
                            )
                            Text(
                                text = "Pilih widget dari aplikasi terpasang, lalu geser/ubah ukurannya di desktop",
                                style = MaterialTheme.typography.labelSmall,
                                color = winColors.textSecondary
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Tutup pemilih widget",
                            tint = winColors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari aplikasi atau widget...", style = MaterialTheme.typography.bodySmall) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = winColors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("widget_picker_search")
                )

                HorizontalDivider(color = winColors.subtleBorder)

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = winColors.accent, modifier = Modifier.size(32.dp))
                        }
                    }
                    filtered.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Widgets,
                                    contentDescription = null,
                                    tint = winColors.textSecondary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = if (availableWidgets.isEmpty()) {
                                        "Belum ada widget aplikasi yang tersedia di perangkat ini."
                                    } else {
                                        "Tidak ada widget yang cocok dengan \"$searchQuery\"."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = winColors.textSecondary
                                )
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = filtered,
                                key = { "${it.providerPackage}/${it.providerClass}" }
                            ) { item ->
                                WidgetPickerRow(
                                    item = item,
                                    onClick = { onSelectWidget(item) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetPickerRow(
    item: AppWidgetItem,
    onClick: () -> Unit
) {
    val winColors = LocalWin11Colors.current
    val cardShape = RoundedCornerShape(10.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(winColors.cardSurface)
            .border(1.dp, winColors.subtleBorder, cardShape)
            .clickable(onClick = onClick)
            .padding(10.dp)
            .testTag("widget_picker_item_${item.providerPackage}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Preview or App Icon
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(winColors.micaSurface)
                .border(1.dp, winColors.subtleBorder, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            val preview = item.previewBitmap ?: item.appIcon
            if (preview != null) {
                Image(
                    bitmap = preview,
                    contentDescription = item.widgetLabel,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(4.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Apps,
                    contentDescription = item.widgetLabel,
                    tint = winColors.accent,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = item.widgetLabel,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = winColors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.appName,
                style = MaterialTheme.typography.labelSmall,
                color = winColors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Default: ${item.defaultWidthDp.roundToInt()} × ${item.defaultHeightDp.roundToInt()} dp",
                style = MaterialTheme.typography.labelSmall,
                color = winColors.accent
            )
        }

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(winColors.accentSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Pasang widget",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
