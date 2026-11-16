package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.InstallMobile
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.GitHubUpdateState
import com.example.model.InAppUpdateStage
import com.example.ui.theme.LocalWin11Colors

@Composable
fun Win11InAppUpdateDialog(
    updateState: GitHubUpdateState,
    onRequestInstallPermission: () -> Unit,
    onRetryUpdate: () -> Unit,
    onDismiss: () -> Unit
) {
    if (updateState.inAppStage == InAppUpdateStage.NONE) return

    val winColors = LocalWin11Colors.current
    val shape = RoundedCornerShape(16.dp)
    val canDismiss = updateState.inAppStage == InAppUpdateStage.PERMISSION_REQUIRED ||
        updateState.inAppStage == InAppUpdateStage.INSTALLED_SUCCESS ||
        updateState.inAppStage == InAppUpdateStage.FAILED

    Dialog(
        onDismissRequest = { if (canDismiss) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = canDismiss,
            dismissOnClickOutside = canDismiss
        )
    ) {
        Surface(
            shape = shape,
            color = winColors.micaSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, winColors.border, shape)
                .testTag("in_app_update_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Windows Update (In-App)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = winColors.textPrimary
                    )
                    if (canDismiss) {
                        IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Rounded.Close, contentDescription = "Close", tint = winColors.textPrimary)
                        }
                    }
                }

                HorizontalDivider(color = winColors.subtleBorder)

                AnimatedContent(
                    targetState = updateState.inAppStage,
                    transitionSpec = {
                        fadeIn(tween(220)) togetherWith fadeOut(tween(180))
                    },
                    label = "in_app_update_stage"
                ) { stage ->
                    when (stage) {
                        InAppUpdateStage.PERMISSION_REQUIRED -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(winColors.accent.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Security,
                                        contentDescription = null,
                                        tint = winColors.accent,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Text(
                                    text = "Izin Instalasi Aplikasi Diperlukan",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = winColors.textPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Aktifkan 'Izinkan dari sumber ini' agar Win11 Launcher dapat menginstal file update APK langsung di dalam aplikasi.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = winColors.textSecondary,
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = onRequestInstallPermission,
                                    colors = ButtonDefaults.buttonColors(containerColor = winColors.accentSoft),
                                    modifier = Modifier.fillMaxWidth().testTag("update_grant_permission_button")
                                ) {
                                    Text("Buka Pengaturan Izin")
                                }
                            }
                        }

                        InAppUpdateStage.DOWNLOADING -> {
                            DownloadingAnimationBody(
                                versionLabel = updateState.latestTag ?: "",
                                progress = updateState.downloadProgress,
                                downloadedBytes = updateState.downloadedBytes,
                                totalBytes = updateState.totalBytes
                            )
                        }

                        InAppUpdateStage.VERIFYING -> {
                            VerifyingAnimationBody()
                        }

                        InAppUpdateStage.INSTALLING -> {
                            InstallingAnimationBody(
                                versionLabel = updateState.latestTag ?: "",
                                installProgress = updateState.installProgress
                            )
                        }

                        InAppUpdateStage.CLEANING_UP -> {
                            CleaningAnimationBody()
                        }

                        InAppUpdateStage.INSTALLED_SUCCESS -> {
                            InstalledSuccessAnimationBody(
                                versionLabel = updateState.latestTag ?: updateState.currentVersion,
                                cleanedUp = updateState.cleanedUpAfterInstall,
                                onDone = onDismiss
                            )
                        }

                        InAppUpdateStage.FAILED -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF6B6B).copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ErrorOutline,
                                        contentDescription = null,
                                        tint = Color(0xFFFF6B6B),
                                        modifier = Modifier.size(34.dp)
                                    )
                                }
                                Text(
                                    text = "Update Tidak Selesai",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = winColors.textPrimary
                                )
                                Text(
                                    text = updateState.errorMessage ?: "Terjadi kendala saat mengunduh atau menginstal APK.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = winColors.textSecondary,
                                    textAlign = TextAlign.Center
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                                        Text("Tutup")
                                    }
                                    Button(
                                        onClick = onRetryUpdate,
                                        colors = ButtonDefaults.buttonColors(containerColor = winColors.accentSoft),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Coba Lagi")
                                    }
                                }
                            }
                        }

                        InAppUpdateStage.NONE -> Unit
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadingAnimationBody(
    versionLabel: String,
    progress: Float,
    downloadedBytes: Long,
    totalBytes: Long
) {
    val winColors = LocalWin11Colors.current
    val infinite = rememberInfiniteTransition(label = "download_anim")
    val pulseScale by infinite.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.24f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val bobOffset by infinite.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bob"
    )
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(180),
        label = "dl_progress"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth().testTag("update_downloading_stage")
    ) {
        Box(modifier = Modifier.size(84.dp), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(winColors.accent.copy(alpha = 0.14f))
            )
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(winColors.accentSoft.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.CloudDownload,
                    contentDescription = "Downloading",
                    tint = winColors.accent,
                    modifier = Modifier
                        .size(30.dp)
                        .offset(y = bobOffset.dp)
                )
            }
        }

        Text(
            text = "Mengunduh Update $versionLabel...",
            style = MaterialTheme.typography.titleSmall,
            color = winColors.textPrimary
        )

        if (totalBytes > 0L) {
            LinearProgressIndicator(
                progress = { animatedProgress },
                color = winColors.accent,
                trackColor = winColors.border,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
            )
            val pct = (animatedProgress * 100).toInt()
            Text(
                text = "${formatExplorerBytes(downloadedBytes, false)} / ${formatExplorerBytes(totalBytes, false)}  •  $pct%",
                style = MaterialTheme.typography.labelMedium,
                color = winColors.textSecondary
            )
        } else {
            LinearProgressIndicator(
                color = winColors.accent,
                trackColor = winColors.border,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
            )
            Text(
                text = "${formatExplorerBytes(downloadedBytes, false)} diunduh...",
                style = MaterialTheme.typography.labelMedium,
                color = winColors.textSecondary
            )
        }
    }
}

@Composable
private fun VerifyingAnimationBody() {
    val winColors = LocalWin11Colors.current
    val infinite = rememberInfiniteTransition(label = "verify_anim")
    val pulse by infinite.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "verify_pulse"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .scale(pulse)
                .clip(CircleShape)
                .background(winColors.accent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.VerifiedUser,
                contentDescription = "Verifying APK",
                tint = winColors.accent,
                modifier = Modifier.size(34.dp)
            )
        }
        Text(
            text = "Memverifikasi Paket APK...",
            style = MaterialTheme.typography.titleSmall,
            color = winColors.textPrimary
        )
        LinearProgressIndicator(
            color = winColors.accent,
            trackColor = winColors.border,
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)
        )
    }
}

@Composable
private fun InstallingAnimationBody(
    versionLabel: String,
    installProgress: Float
) {
    val winColors = LocalWin11Colors.current
    val infinite = rememberInfiniteTransition(label = "install_spinner")
    val spinAngle by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )
    val animatedInstall by animateFloatAsState(
        targetValue = installProgress.coerceIn(0.08f, 1f),
        animationSpec = tween(250),
        label = "install_bar"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth().testTag("update_installing_stage")
    ) {
        Box(modifier = Modifier.size(86.dp), contentAlignment = Alignment.Center) {
            Canvas(
                modifier = Modifier
                    .size(80.dp)
                    .rotate(spinAngle)
            ) {
                drawArc(
                    color = winColors.accent.copy(alpha = 0.2f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = winColors.accent,
                    startAngle = 0f,
                    sweepAngle = 110f,
                    useCenter = false,
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Icon(
                imageVector = Icons.Rounded.InstallMobile,
                contentDescription = "Installing APK",
                tint = winColors.textPrimary,
                modifier = Modifier.size(32.dp)
            )
        }

        Text(
            text = "Menginstal Update $versionLabel...",
            style = MaterialTheme.typography.titleSmall,
            color = winColors.textPrimary
        )
        Text(
            text = "Konfirmasi prompt sistem jika muncul. File installer akan otomatis dihapus setelah selesai.",
            style = MaterialTheme.typography.labelSmall,
            color = winColors.textSecondary,
            textAlign = TextAlign.Center
        )
        LinearProgressIndicator(
            progress = { animatedInstall },
            color = winColors.accent,
            trackColor = winColors.border,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
        )
        Text(
            text = "${(animatedInstall * 100).toInt()}% • Memasang paket",
            style = MaterialTheme.typography.labelMedium,
            color = winColors.textSecondary
        )
    }
}

@Composable
private fun CleaningAnimationBody() {
    val winColors = LocalWin11Colors.current
    val infinite = rememberInfiniteTransition(label = "clean_anim")
    val tilt by infinite.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(tween(450), RepeatMode.Reverse),
        label = "clean_tilt"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(66.dp)
                .clip(CircleShape)
                .background(Color(0xFF10B981).copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.CleaningServices,
                contentDescription = "Cleaning up APK",
                tint = Color(0xFF4CD4A9),
                modifier = Modifier.size(30.dp).rotate(tilt)
            )
        }
        Text(
            text = "Membersihkan File Update...",
            style = MaterialTheme.typography.titleSmall,
            color = winColors.textPrimary
        )
        LinearProgressIndicator(
            color = Color(0xFF4CD4A9),
            trackColor = winColors.border,
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)
        )
    }
}

@Composable
private fun InstalledSuccessAnimationBody(
    versionLabel: String,
    cleanedUp: Boolean,
    onDone: () -> Unit
) {
    val winColors = LocalWin11Colors.current
    var target by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) { target = 1f }

    val badgeScale by animateFloatAsState(
        targetValue = target,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "success_scale"
    )
    val checkProgress by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 550, delayMillis = 120, easing = FastOutSlowInEasing),
        label = "check_stroke"
    )

    val emerald = Color(0xFF10B981)
    val brightEmerald = Color(0xFF4CD4A9)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth().testTag("update_installed_success_stage")
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .scale(badgeScale.coerceAtLeast(0.1f))
                .clip(CircleShape)
                .background(emerald.copy(alpha = 0.18f))
                .border(2.dp, brightEmerald, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(40.dp)) {
                val path = Path().apply {
                    moveTo(size.width * 0.18f, size.height * 0.54f)
                    lineTo(size.width * 0.42f, size.height * 0.78f)
                    lineTo(size.width * 0.84f, size.height * 0.26f)
                }
                val outPath = Path()
                val measure = PathMeasure()
                measure.setPath(path, false)
                measure.getSegment(0f, measure.length * checkProgress, outPath, true)
                drawPath(
                    path = outPath,
                    color = brightEmerald,
                    style = Stroke(
                        width = 4.5.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }

        Text(
            text = "Update Berhasil Terinstal!",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = brightEmerald
        )
        Text(
            text = buildString {
                append("Win11 Launcher telah diperbarui ($versionLabel).")
                if (cleanedUp) {
                    append("\nFile installer APK sementara sudah dihapus otomatis.")
                }
            },
            style = MaterialTheme.typography.bodySmall,
            color = winColors.textSecondary,
            textAlign = TextAlign.Center
        )

        Button(
            onClick = onDone,
            colors = ButtonDefaults.buttonColors(containerColor = winColors.accentSoft),
            modifier = Modifier.fillMaxWidth().testTag("update_success_done_button")
        ) {
            Text("Selesai")
        }
    }
}
