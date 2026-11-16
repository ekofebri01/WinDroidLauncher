package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SystemTelemetry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun Win11LockScreen(
    currentTimeMillis: Long,
    use24HourClock: Boolean,
    telemetry: SystemTelemetry,
    userName: String,
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeText = SimpleDateFormat(
        if (use24HourClock) "HH:mm" else "hh:mm",
        Locale.getDefault()
    ).format(Date(currentTimeMillis))
    val dateText = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date(currentTimeMillis))

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0x66060D1A))
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -18f) {
                        onUnlock()
                    }
                }
            }
            .clickable(onClick = onUnlock)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 28.dp)
            .testTag("win11_lock_screen")
    ) {
        // Top Centered Clock & Date (Windows 11 Lock Screen Signature)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = timeText,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 72.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = dateText,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Normal),
                color = Color.White.copy(alpha = 0.92f)
            )
        }

        // Bottom Sign-in prompt & Status Icons
        Column(
            modifier = Modifier.align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0x66161B24))
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Rounded.LockOpen, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Text(
                    text = "Tap or swipe up to sign in as $userName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (telemetry.isWifiConnected) Icons.Rounded.Wifi else Icons.Rounded.WifiOff,
                    contentDescription = telemetry.networkLabel,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Icon(
                    imageVector = if (telemetry.isCharging) Icons.Rounded.BatteryChargingFull else Icons.Rounded.BatteryFull,
                    contentDescription = "Battery ${telemetry.batteryPercent}%",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
