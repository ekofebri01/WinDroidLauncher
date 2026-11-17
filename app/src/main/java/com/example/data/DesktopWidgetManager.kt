package com.example.data

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.SizeF
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.model.AppWidgetItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

class DesktopWidgetManager(private val context: Context) {

    companion object {
        const val HOST_ID = 1024
        const val REQUEST_CONFIGURE_APPWIDGET = 4102
    }

    private val appWidgetManager: AppWidgetManager? = runCatching {
        AppWidgetManager.getInstance(context)
    }.getOrNull()

    private val appWidgetHost = Win11AppWidgetHost(context, HOST_ID)

    fun startListening() {
        runCatching { appWidgetHost.startListening() }
    }

    fun stopListening() {
        runCatching { appWidgetHost.stopListening() }
    }

    fun allocateAppWidgetId(): Int {
        return runCatching { appWidgetHost.allocateAppWidgetId() }.getOrDefault(-1)
    }

    fun deleteAppWidgetId(appWidgetId: Int) {
        if (appWidgetId >= 0) {
            runCatching { appWidgetHost.deleteAppWidgetId(appWidgetId) }
        }
    }

    fun bindAppWidgetIfAllowed(appWidgetId: Int, info: AppWidgetProviderInfo): Boolean {
        val mgr = appWidgetManager ?: return false
        return runCatching {
            mgr.bindAppWidgetIdIfAllowed(appWidgetId, info.profile, info.provider, buildOptionsBundle(240f, 150f))
        }.getOrDefault(false)
    }

    fun createBindIntent(appWidgetId: Int, info: AppWidgetProviderInfo, widthDp: Float, heightDp: Float): Intent {
        return Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, info.provider)
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER_PROFILE, info.profile)
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_OPTIONS, buildOptionsBundle(widthDp, heightDp))
        }
    }

    fun needsInitialConfiguration(info: AppWidgetProviderInfo): Boolean {
        if (info.configure == null) return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val optional = (info.widgetFeatures and AppWidgetProviderInfo.WIDGET_FEATURE_CONFIGURATION_OPTIONAL) != 0
            val reconfigurable = (info.widgetFeatures and AppWidgetProviderInfo.WIDGET_FEATURE_RECONFIGURABLE) != 0
            if (optional && reconfigurable) return false
        }
        return true
    }

    fun isReconfigurable(info: AppWidgetProviderInfo?): Boolean {
        if (info?.configure == null) return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            (info.widgetFeatures and AppWidgetProviderInfo.WIDGET_FEATURE_RECONFIGURABLE) != 0
        } else {
            true
        }
    }

    fun launchConfigureActivity(activity: android.app.Activity, appWidgetId: Int): Boolean {
        return runCatching {
            appWidgetHost.startAppWidgetConfigureActivityForResult(
                activity,
                appWidgetId,
                0,
                REQUEST_CONFIGURE_APPWIDGET,
                null
            )
            true
        }.getOrDefault(false)
    }

    fun findProviderInfo(packageName: String, className: String): AppWidgetProviderInfo? {
        val mgr = appWidgetManager ?: return null
        return runCatching {
            mgr.installedProviders.firstOrNull {
                it.provider.packageName == packageName && it.provider.className == className
            }
        }.getOrNull()
    }

    fun createHostView(
        appWidgetId: Int,
        info: AppWidgetProviderInfo?,
        widthDp: Float,
        heightDp: Float,
        onLongPress: () -> Unit
    ): Win11AppWidgetHostView? {
        val resolvedInfo = info ?: appWidgetManager?.getAppWidgetInfo(appWidgetId) ?: return null
        return runCatching {
            val view = appWidgetHost.createView(context, appWidgetId, resolvedInfo) as Win11AppWidgetHostView
            view.setAppWidget(appWidgetId, resolvedInfo)
            view.onLongPressDetected = onLongPress
            updateWidgetSizeOptions(view, widthDp, heightDp)
            view
        }.getOrNull()
    }

    fun updateWidgetSizeOptions(hostView: AppWidgetHostView, widthDp: Float, heightDp: Float) {
        val w = widthDp.toInt().coerceAtLeast(48)
        val h = heightDp.toInt().coerceAtLeast(48)
        val options = buildOptionsBundle(widthDp, heightDp)
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                hostView.updateAppWidgetSize(
                    options,
                    listOf(SizeF(w.toFloat(), h.toFloat()))
                )
            } else {
                @Suppress("DEPRECATION")
                hostView.updateAppWidgetSize(options, w, h, w, h)
            }
            hostView.updateAppWidgetOptions(options)
        }
    }

    fun buildOptionsBundle(widthDp: Float, heightDp: Float): Bundle {
        val w = widthDp.toInt().coerceAtLeast(48)
        val h = heightDp.toInt().coerceAtLeast(48)
        return Bundle().apply {
            putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, w)
            putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, w)
            putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, h)
            putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, h)
        }
    }

    suspend fun loadAvailableAppWidgets(): List<AppWidgetItem> = withContext(Dispatchers.IO) {
        val mgr = appWidgetManager ?: return@withContext emptyList()
        val pm = context.packageManager
        val providers = runCatching { mgr.installedProviders }.getOrDefault(emptyList())
        val densityDpi = context.resources.displayMetrics.densityDpi
            .coerceAtLeast(DisplayMetrics.DENSITY_MEDIUM)

        providers.mapNotNull { info ->
            runCatching {
                val pkg = info.provider.packageName
                val appInfo = pm.getApplicationInfo(pkg, 0)
                val appName = pm.getApplicationLabel(appInfo)?.toString()?.trim().orEmpty()
                    .ifEmpty { pkg }
                val widgetLabel = info.loadLabel(pm)?.trim().orEmpty()
                    .ifEmpty { appName }

                val appIcon = runCatching {
                    drawableToImageBitmap(pm.getApplicationIcon(appInfo), 96)
                }.getOrNull()

                val preview = runCatching {
                    val d = info.loadPreviewImage(context, densityDpi) ?: info.loadIcon(context, densityDpi)
                    d?.let { drawableToImageBitmap(it, 180) }
                }.getOrNull()

                val density = context.resources.displayMetrics.density.coerceAtLeast(1f)
                val rawMinW = (info.minWidth / density).toInt().coerceAtLeast(80)
                val rawMinH = (info.minHeight / density).toInt().coerceAtLeast(64)

                val defaultW = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && info.targetCellWidth > 0) {
                    (info.targetCellWidth * 78f).coerceIn(150f, 340f)
                } else {
                    rawMinW.toFloat().coerceIn(150f, 340f)
                }
                val defaultH = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && info.targetCellHeight > 0) {
                    (info.targetCellHeight * 78f).coerceIn(100f, 260f)
                } else {
                    rawMinH.toFloat().coerceIn(100f, 260f)
                }

                AppWidgetItem(
                    providerPackage = pkg,
                    providerClass = info.provider.className,
                    appName = appName,
                    widgetLabel = widgetLabel,
                    defaultWidthDp = defaultW,
                    defaultHeightDp = defaultH,
                    minWidthDp = 96,
                    minHeightDp = 72,
                    previewBitmap = preview,
                    appIcon = appIcon,
                    providerInfo = info
                )
            }.getOrNull()
        }.sortedWith(compareBy({ it.appName.lowercase() }, { it.widgetLabel.lowercase() }))
    }

    private fun drawableToImageBitmap(drawable: Drawable, maxDimPx: Int): ImageBitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            val bmp = drawable.bitmap
            if (bmp.width <= maxDimPx && bmp.height <= maxDimPx) {
                return bmp.asImageBitmap()
            }
            val scale = maxDimPx.toFloat() / maxOf(bmp.width, bmp.height).toFloat()
            val targetW = (bmp.width * scale).toInt().coerceAtLeast(1)
            val targetH = (bmp.height * scale).toInt().coerceAtLeast(1)
            return Bitmap.createScaledBitmap(bmp, targetW, targetH, true).asImageBitmap()
        }
        val w = drawable.intrinsicWidth.takeIf { it > 0 }?.coerceAtMost(maxDimPx) ?: maxDimPx
        val h = drawable.intrinsicHeight.takeIf { it > 0 }?.coerceAtMost(maxDimPx) ?: maxDimPx
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap.asImageBitmap()
    }
}

class Win11AppWidgetHost(
    context: Context,
    hostId: Int
) : AppWidgetHost(context, hostId) {
    override fun onCreateView(
        context: Context,
        appWidgetId: Int,
        appWidget: AppWidgetProviderInfo?
    ): AppWidgetHostView {
        return Win11AppWidgetHostView(context)
    }
}

class Win11AppWidgetHostView(context: Context) : AppWidgetHostView(context) {

    var onLongPressDetected: (() -> Unit)? = null

    private val handler = Handler(Looper.getMainLooper())
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var downX = 0f
    private var downY = 0f
    private var longPressTriggered = false

    private val longPressRunnable = Runnable {
        longPressTriggered = true
        performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
        onLongPressDetected?.invoke()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                longPressTriggered = false
                downX = ev.x
                downY = ev.y
                handler.removeCallbacks(longPressRunnable)
                handler.postDelayed(longPressRunnable, ViewConfiguration.getLongPressTimeout().toLong())
            }
            MotionEvent.ACTION_MOVE -> {
                if (abs(ev.x - downX) > touchSlop || abs(ev.y - downY) > touchSlop) {
                    handler.removeCallbacks(longPressRunnable)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                handler.removeCallbacks(longPressRunnable)
                if (longPressTriggered) {
                    return true
                }
            }
        }
        return false
    }
}
