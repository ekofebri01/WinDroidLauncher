package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DesktopIconSize
import com.example.model.DesktopShortcut
import com.example.ui.theme.LocalWin11Colors

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Win11DesktopGrid(
    shortcuts: List<DesktopShortcut>,
    iconSize: DesktopIconSize,
    selectedShortcutId: String?,
    onDesktopTap: () -> Unit,
    onDesktopLongPress: () -> Unit,
    onShortcutTap: (DesktopShortcut) -> Unit,
    onShortcutLongPress: (DesktopShortcut) -> Unit,
    modifier: Modifier = Modifier
) {
    val winColors = LocalWin11Colors.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .combinedClickable(
                interactionSource = null,
                indication = null,
                onClick = onDesktopTap,
                onLongClick = onDesktopLongPress
            )
            .padding(horizontal = 12.dp, vertical = 14.dp)
            .testTag("desktop_canvas")
    ) {
        val cellHeight = iconSize.cellHeightDp.dp
        val cellWidth = iconSize.cellWidthDp.dp
        val rowSpacing = 8.dp
        val rowsPerCol = ((maxHeight + rowSpacing) / (cellHeight + rowSpacing))
            .toInt()
            .coerceAtLeast(3)

        val columns = shortcuts.chunked(rowsPerCol)

        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            columns.forEach { columnItems ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(rowSpacing),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    columnItems.forEach { shortcut ->
                        val isSelected = shortcut.id == selectedShortcutId
                        DesktopShortcutTile(
                            shortcut = shortcut,
                            iconSize = iconSize,
                            cellWidth = cellWidth,
                            cellHeight = cellHeight,
                            isSelected = isSelected,
                            selectionFill = winColors.selectionFill,
                            selectionBorder = winColors.selectionBorder,
                            onClick = { onShortcutTap(shortcut) },
                            onLongClick = { onShortcutLongPress(shortcut) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DesktopShortcutTile(
    shortcut: DesktopShortcut,
    iconSize: DesktopIconSize,
    cellWidth: androidx.compose.ui.unit.Dp,
    cellHeight: androidx.compose.ui.unit.Dp,
    isSelected: Boolean,
    selectionFill: Color,
    selectionBorder: Color,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = Modifier
            .width(cellWidth)
            .height(cellHeight)
            .clip(shape)
            .background(if (isSelected) selectionFill else Color.Transparent, shape)
            .then(
                if (isSelected) Modifier.border(1.dp, selectionBorder, shape) else Modifier
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 4.dp, vertical = 6.dp)
            .testTag("desktop_shortcut_${shortcut.id}"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            ShortcutIconView(
                shortcut = shortcut,
                size = iconSize.iconDp.dp
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = shortcut.label,
                color = Color.White,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = if (iconSize == DesktopIconSize.SMALL) 11.sp else 12.sp,
                    shadow = Shadow(
                        color = Color(0xCC000000),
                        offset = Offset(0f, 1.5f),
                        blurRadius = 4f
                    )
                ),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
