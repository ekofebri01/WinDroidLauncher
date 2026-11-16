package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.InsertDriveFile
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.VideoFile
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.model.ExplorerItem
import com.example.model.ExplorerPreviewState
import com.example.model.FileItemKind
import com.example.ui.theme.LocalWin11Colors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExplorerItemIcon(
    item: ExplorerItem,
    size: Dp,
    modifier: Modifier = Modifier
) {
    if (item.isDirectory) {
        Win11FolderIcon(size = size, modifier = modifier)
        return
    }
    val (icon, tint) = when (item.kind) {
        FileItemKind.FOLDER -> Icons.Rounded.Description to Color(0xFFF6B93B)
        FileItemKind.TEXT -> Icons.Rounded.Description to Color(0xFF60CDFF)
        FileItemKind.IMAGE -> Icons.Rounded.Image to Color(0xFF4CD4A9)
        FileItemKind.AUDIO -> Icons.Rounded.AudioFile to Color(0xFFFF8C42)
        FileItemKind.VIDEO -> Icons.Rounded.VideoFile to Color(0xFFA78BFA)
        FileItemKind.PDF -> Icons.Rounded.PictureAsPdf to Color(0xFFEF4444)
        FileItemKind.ARCHIVE -> Icons.Rounded.Archive to Color(0xFFF59E0B)
        FileItemKind.OTHER -> Icons.AutoMirrored.Rounded.InsertDriveFile to Color(0xFF94A3B8)
    }
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(6.dp))
            .background(tint.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = item.name,
            tint = tint,
            modifier = Modifier.size(size * 0.68f)
        )
    }
}

@Composable
fun ExplorerNameInputDialog(
    title: String,
    label: String,
    initialValue: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val winColors = LocalWin11Colors.current
    var text by remember { mutableStateOf(initialValue) }
    val shape = RoundedCornerShape(12.dp)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = shape,
            color = winColors.micaSurface,
            modifier = Modifier.border(1.dp, winColors.border, shape)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = winColors.textPrimary
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(label) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = winColors.textSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { if (text.isNotBlank()) onConfirm(text.trim()) },
                        colors = ButtonDefaults.buttonColors(containerColor = winColors.accentSoft)
                    ) {
                        Text(confirmText)
                    }
                }
            }
        }
    }
}

@Composable
fun ExplorerPreviewDialog(
    preview: ExplorerPreviewState,
    onDismiss: () -> Unit,
    onSaveText: (String) -> Unit,
    onOpenExternally: (ExplorerItem) -> Unit,
    onSetAsWallpaper: (ExplorerItem) -> Unit
) {
    val winColors = LocalWin11Colors.current
    var textDraft by remember(preview.item.id) { mutableStateOf(preview.textContent.orEmpty()) }
    val shape = RoundedCornerShape(14.dp)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = shape,
            color = winColors.micaSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, winColors.border, shape)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        ExplorerItemIcon(item = preview.item, size = 24.dp)
                        Text(
                            text = preview.item.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = winColors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = winColors.textPrimary)
                    }
                }

                HorizontalDivider(color = winColors.subtleBorder)

                if (preview.imageUriString != null) {
                    AsyncImage(
                        model = preview.imageUriString,
                        contentDescription = preview.item.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 180.dp, max = 320.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.25f))
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onOpenExternally(preview.item) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open App")
                        }
                        Button(
                            onClick = { onSetAsWallpaper(preview.item) },
                            colors = ButtonDefaults.buttonColors(containerColor = winColors.accentSoft),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Wallpaper, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Wallpaper")
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = textDraft,
                        onValueChange = { textDraft = it },
                        readOnly = !preview.isEditableText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(onClick = { onOpenExternally(preview.item) }) {
                            Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open App")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        if (preview.isEditableText) {
                            Button(
                                onClick = { onSaveText(textDraft) },
                                colors = ButtonDefaults.buttonColors(containerColor = winColors.accentSoft)
                            ) {
                                Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save")
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatExplorerBytes(bytes: Long, isDirectory: Boolean): String {
    if (isDirectory) return "$bytes items"
    if (bytes < 1024L) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024.0) return String.format(Locale.getDefault(), "%.1f KB", kb)
    val mb = kb / 1024.0
    if (mb < 1024.0) return String.format(Locale.getDefault(), "%.1f MB", mb)
    val gb = mb / 1024.0
    return String.format(Locale.getDefault(), "%.2f GB", gb)
}

fun formatExplorerDate(millis: Long): String {
    if (millis <= 0L) return "—"
    return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(millis))
}
