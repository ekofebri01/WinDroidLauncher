package com.example.data

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream

class ApkInstallerManager(private val context: Context) {

    companion object {
        private const val ACTION_INSTALL_COMMIT = "com.example.action.IN_APP_UPDATE_INSTALL_STATUS"
    }

    private val packageInstaller: PackageInstaller = context.packageManager.packageInstaller
    private var activeSessionId: Int = -1
    private var receiverRegistered = false
    private var callbackRegistered = false

    private var onProgressCallback: ((Float) -> Unit)? = null
    private var onUserActionCallback: ((Intent) -> Unit)? = null
    private var onSuccessCallback: (() -> Unit)? = null
    private var onFailureCallback: ((String) -> Unit)? = null

    private val sessionCallback = object : PackageInstaller.SessionCallback() {
        override fun onCreated(sessionId: Int) = Unit
        override fun onBadgingChanged(sessionId: Int) = Unit
        override fun onActiveChanged(sessionId: Int, active: Boolean) = Unit
        override fun onProgressChanged(sessionId: Int, progress: Float) {
            if (sessionId == activeSessionId) {
                onProgressCallback?.invoke(progress.coerceIn(0f, 1f))
            }
        }
        override fun onFinished(sessionId: Int, success: Boolean) {
            if (sessionId == activeSessionId) {
                if (success) {
                    onProgressCallback?.invoke(1f)
                    onSuccessCallback?.invoke()
                }
            }
        }
    }

    private val statusReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            if (intent?.action != ACTION_INSTALL_COMMIT) return
            val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
            when (status) {
                PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                    val confirmIntent: Intent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(Intent.EXTRA_INTENT)
                    }
                    if (confirmIntent != null) {
                        confirmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        onUserActionCallback?.invoke(confirmIntent)
                    } else {
                        onFailureCallback?.invoke("Prompt konfirmasi sistem tidak tersedia")
                    }
                }
                PackageInstaller.STATUS_SUCCESS -> {
                    onProgressCallback?.invoke(1f)
                    onSuccessCallback?.invoke()
                }
                PackageInstaller.STATUS_FAILURE_ABORTED -> {
                    onFailureCallback?.invoke("Instalasi dibatalkan oleh pengguna")
                }
                else -> {
                    val msg = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                        ?.takeIf { it.isNotBlank() }
                        ?: "Instalasi APK gagal (kode status $status)"
                    onFailureCallback?.invoke(msg)
                }
            }
        }
    }

    fun canRequestPackageInstalls(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            runCatching { context.packageManager.canRequestPackageInstalls() }.getOrDefault(true)
        } else {
            true
        }
    }

    fun createUnknownSourcesIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }

    fun registerListeners(
        onProgress: (Float) -> Unit,
        onUserAction: (Intent) -> Unit,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        onProgressCallback = onProgress
        onUserActionCallback = onUserAction
        onSuccessCallback = onSuccess
        onFailureCallback = onFailure

        if (!callbackRegistered) {
            packageInstaller.registerSessionCallback(sessionCallback)
            callbackRegistered = true
        }
        if (!receiverRegistered) {
            val filter = IntentFilter(ACTION_INSTALL_COMMIT)
            ContextCompat.registerReceiver(
                context,
                statusReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
            receiverRegistered = true
        }
    }

    suspend fun installApk(apkFile: File): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val params = PackageInstaller.SessionParams(
                PackageInstaller.SessionParams.MODE_FULL_INSTALL
            ).apply {
                setAppPackageName(context.packageName)
            }
            val sessionId = packageInstaller.createSession(params)
            activeSessionId = sessionId

            val session = packageInstaller.openSession(sessionId)
            try {
                val total = apkFile.length().coerceAtLeast(1L)
                var written = 0L
                session.openWrite("win11_update.apk", 0, apkFile.length()).use { out ->
                    FileInputStream(apkFile).use { input ->
                        val buffer = ByteArray(16 * 1024)
                        while (true) {
                            val read = input.read(buffer)
                            if (read <= 0) break
                            out.write(buffer, 0, read)
                            written += read
                            val stageFraction = (written.toFloat() / total.toFloat()) * 0.45f
                            session.setStagingProgress(stageFraction)
                            onProgressCallback?.invoke(stageFraction)
                        }
                        session.fsync(out)
                    }
                }

                val commitIntent = Intent(ACTION_INSTALL_COMMIT).apply {
                    setPackage(context.packageName)
                }
                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    sessionId,
                    commitIntent,
                    flags
                )
                session.commit(pendingIntent.intentSender)
                Unit
            } catch (t: Throwable) {
                runCatching { session.abandon() }
                throw t
            } finally {
                runCatching { session.close() }
            }
        }
    }

    fun dispose() {
        if (callbackRegistered) {
            runCatching { packageInstaller.unregisterSessionCallback(sessionCallback) }
            callbackRegistered = false
        }
        if (receiverRegistered) {
            runCatching { context.unregisterReceiver(statusReceiver) }
            receiverRegistered = false
        }
    }
}
