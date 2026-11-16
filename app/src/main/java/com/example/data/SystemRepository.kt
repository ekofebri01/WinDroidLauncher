package com.example.data

import android.app.ActivityManager
import android.app.role.RoleManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.ContextCompat
import com.example.model.HomeLauncherStatus
import com.example.model.InstalledApp
import com.example.model.SystemTelemetry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

class SystemRepository(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val activityManager =
        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val cameraManager =
        context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager

    private var torchCameraId: String? = null
    private var torchEnabled = false

    init {
        torchCameraId = runCatching {
            cameraManager?.cameraIdList?.firstOrNull { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        }.getOrNull()
    }

    suspend fun loadInstalledApps(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                launcherIntent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(launcherIntent, 0)
        }

        val selfPackage = context.packageName
        resolveInfos
            .asSequence()
            .filter { it.activityInfo.packageName != selfPackage }
            .distinctBy { "${it.activityInfo.packageName}/${it.activityInfo.name}" }
            .map { info ->
                val pkg = info.activityInfo.packageName
                val act = info.activityInfo.name
                val label = info.loadLabel(packageManager)?.toString()?.trim().orEmpty()
                    .ifEmpty { pkg }
                val icon = runCatching {
                    drawableToImageBitmap(info.loadIcon(packageManager))
                }.getOrNull()
                val isSystem = (info.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                InstalledApp(
                    packageName = pkg,
                    activityName = act,
                    label = label,
                    icon = icon,
                    isSystemApp = isSystem
                )
            }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    fun observePackageChanges(): Flow<Unit> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                trySend(Unit)
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        awaitClose {
            runCatching { context.unregisterReceiver(receiver) }
        }
    }

    fun observeSystemTelemetry(): Flow<SystemTelemetry> = callbackFlow {
        fun emitSnapshot() {
            trySend(readCurrentTelemetry())
        }

        emitSnapshot()

        val batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                emitSnapshot()
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction("android.media.VOLUME_CHANGED_ACTION")
        }
        ContextCompat.registerReceiver(
            context,
            batteryReceiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) = emitSnapshot()
            override fun onLost(network: Network) = emitSnapshot()
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) = emitSnapshot()
        }
        val callbackRegistered = runCatching {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
            true
        }.getOrDefault(false)

        awaitClose {
            runCatching { context.unregisterReceiver(batteryReceiver) }
            if (callbackRegistered) {
                runCatching { connectivityManager.unregisterNetworkCallback(networkCallback) }
            }
        }
    }

    fun readCurrentTelemetry(): SystemTelemetry {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val batteryPercent = if (level >= 0 && scale > 0) ((level * 100f) / scale).toInt() else 100
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val plugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || plugged != 0

        var isWifi = false
        var isCellular = false
        runCatching {
            val activeNetwork = connectivityManager.activeNetwork
            val caps = activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }
            if (caps != null) {
                isWifi = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                isCellular = caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            }
        }

        val networkLabel = when {
            isWifi -> "Wi-Fi Connected"
            isCellular -> "Mobile Data"
            else -> "No Internet"
        }

        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        val curVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val isMuted = curVol == 0

        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        val totalRamMb = (memInfo.totalMem / (1024L * 1024L)).coerceAtLeast(1L)
        val availRamMb = (memInfo.availMem / (1024L * 1024L)).coerceAtLeast(0L)

        val statFs = StatFs(Environment.getDataDirectory().path)
        val totalBytes = statFs.totalBytes.toFloat()
        val availBytes = statFs.availableBytes.toFloat()
        val gbDivisor = 1024f * 1024f * 1024f
        val totalGb = (totalBytes / gbDivisor).coerceAtLeast(1f)
        val usedGb = ((totalBytes - availBytes) / gbDivisor).coerceAtLeast(0f)

        val model = listOfNotNull(
            Build.MANUFACTURER?.replaceFirstChar { it.uppercase() },
            Build.MODEL
        ).distinct().joinToString(" ").ifBlank { "Android Device" }

        return SystemTelemetry(
            batteryPercent = batteryPercent.coerceIn(0, 100),
            isCharging = isCharging,
            isWifiConnected = isWifi,
            isCellularConnected = isCellular,
            networkLabel = networkLabel,
            mediaVolume = curVol,
            maxMediaVolume = maxVol,
            isMuted = isMuted,
            isFlashlightOn = torchEnabled,
            totalRamMb = totalRamMb,
            availableRamMb = availRamMb,
            totalStorageGb = totalGb,
            usedStorageGb = usedGb,
            deviceModel = model,
            androidVersion = "Android ${Build.VERSION.RELEASE}"
        )
    }

    fun setMediaVolume(level: Int) {
        runCatching {
            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, level.coerceIn(0, max), 0)
        }
    }

    fun toggleMute() {
        runCatching {
            val cur = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            if (cur > 0) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
            } else {
                val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (max / 2).coerceAtLeast(1), 0)
            }
        }
    }

    fun toggleFlashlight(): Boolean {
        val id = torchCameraId ?: return false
        return runCatching {
            torchEnabled = !torchEnabled
            cameraManager?.setTorchMode(id, torchEnabled)
            torchEnabled
        }.getOrElse {
            torchEnabled = false
            false
        }
    }

    fun launchApp(app: InstalledApp): Boolean {
        return runCatching {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                component = ComponentName(app.packageName, app.activityName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            }
            context.startActivity(intent)
            true
        }.getOrElse {
            runCatching {
                val fallback = packageManager.getLaunchIntentForPackage(app.packageName)?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (fallback != null) {
                    context.startActivity(fallback)
                    true
                } else {
                    false
                }
            }.getOrDefault(false)
        }
    }

    fun openDefaultBrowser(): Boolean {
        return openExternalUrl("https://www.google.com")
    }

    fun openExternalUrl(url: String): Boolean {
        return runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    fun openAppInfo(packageName: String) {
        runCatching {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun readHomeLauncherStatus(): HomeLauncherStatus {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        val selfPackage = context.packageName
        val roleHeld = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            runCatching {
                val rm = context.getSystemService(RoleManager::class.java)
                rm != null && rm.isRoleAvailable(RoleManager.ROLE_HOME) && rm.isRoleHeld(RoleManager.ROLE_HOME)
            }.getOrDefault(false)
        } else {
            false
        }

        val resolved = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.resolveActivity(
                    homeIntent,
                    PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
            }
        }.getOrNull()

        val resolvedPkg = resolved?.activityInfo?.packageName
        val isDefault = roleHeld || resolvedPkg == selfPackage
        val resolvedLabel = when {
            isDefault -> "Win11 Launcher"
            resolvedPkg == "android" || resolvedPkg == "com.android.settings" -> "Belum dipilih (System Picker)"
            resolved != null -> resolved.loadLabel(packageManager)?.toString()?.trim().orEmpty()
                .ifEmpty { resolvedPkg ?: "System Launcher" }
            else -> "System Launcher"
        }

        val allHomes = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.queryIntentActivities(
                    homeIntent,
                    PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.queryIntentActivities(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
            }
        }.getOrDefault(emptyList())

        val otherHomes = allHomes
            .asSequence()
            .filter {
                val pkg = it.activityInfo.packageName
                pkg != selfPackage && pkg != "android" && pkg != "com.android.settings"
            }
            .distinctBy { it.activityInfo.packageName }
            .map { info ->
                val pkg = info.activityInfo.packageName
                val act = info.activityInfo.name
                val label = info.loadLabel(packageManager)?.toString()?.trim().orEmpty().ifEmpty { pkg }
                val icon = runCatching { drawableToImageBitmap(info.loadIcon(packageManager)) }.getOrNull()
                val isSys = (info.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                InstalledApp(
                    packageName = pkg,
                    activityName = act,
                    label = label,
                    icon = icon,
                    isSystemApp = isSys
                )
            }
            .sortedByDescending { it.isSystemApp }
            .toList()

        return HomeLauncherStatus(
            isDefaultHome = isDefault,
            currentDefaultHomeLabel = resolvedLabel,
            currentDefaultHomePackage = resolvedPkg,
            otherHomeLaunchers = otherHomes
        )
    }

    fun createHomeRoleRequestIntent(): Intent? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return runCatching {
                val rm = context.getSystemService(RoleManager::class.java)
                if (rm != null && rm.isRoleAvailable(RoleManager.ROLE_HOME) && !rm.isRoleHeld(RoleManager.ROLE_HOME)) {
                    rm.createRequestRoleIntent(RoleManager.ROLE_HOME)
                } else {
                    null
                }
            }.getOrNull()
        }
        return null
    }

    fun launchFallbackHomeLauncher(target: InstalledApp): Boolean {
        return runCatching {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                component = ComponentName(target.packageName, target.activityName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            }
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    fun openHomeLauncherSettings() {
        runCatching {
            val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }.onFailure {
            runCatching {
                val fallback = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallback)
            }.onFailure {
                openSystemSettings()
            }
        }
    }

    fun openSystemSettings() {
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    fun openWifiSettings() {
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_WIFI_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    fun openBluetoothSettings() {
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_BLUETOOTH_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    fun openDisplaySettings() {
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_DISPLAY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    fun openStorageSettings() {
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }.onFailure { openSystemSettings() }
    }

    fun openDocumentFolder(mimeType: String) {
        runCatching {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                type = mimeType
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }.onFailure {
            runCatching {
                val fallback = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = mimeType
                    addCategory(Intent.CATEGORY_OPENABLE)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallback)
            }
        }
    }

    private fun drawableToImageBitmap(drawable: Drawable): ImageBitmap {
        val sizePx = 112
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            val bmp = drawable.bitmap
            if (bmp.width <= 144 && bmp.height <= 144) {
                return bmp.asImageBitmap()
            }
            return Bitmap.createScaledBitmap(bmp, sizePx, sizePx, true).asImageBitmap()
        }
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap.asImageBitmap()
    }
}
