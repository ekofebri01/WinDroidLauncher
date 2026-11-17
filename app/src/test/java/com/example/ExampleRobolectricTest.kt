package com.example

import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.test.core.app.ApplicationProvider
import com.example.data.GitHubUpdateRepository
import com.example.data.LauncherPreferencesRepository
import com.example.model.DesktopWidget
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Win11 Launcher", appName)
    }

    @Test
    fun `taskbar and desktop shortcuts render and open floating resizable file explorer`() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("win11_taskbar").assertIsDisplayed()
        composeRule.onNodeWithTag("win11_system_tray").assertIsDisplayed()
        composeRule.onNodeWithTag("tray_wifi_indicator", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("tray_battery_status", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("desktop_shortcut_sys_this_pc").assertIsDisplayed()
        composeRule.onNodeWithTag("taskbar_file_explorer_button").assertIsDisplayed()
        composeRule.onNodeWithTag("tray_clock_date_button").assertIsDisplayed()

        // Verify system tray is positioned on the right side of the taskbar
        val taskbarBounds = composeRule.onNodeWithTag("win11_taskbar").getBoundsInRoot()
        val trayBounds = composeRule.onNodeWithTag("win11_system_tray").getBoundsInRoot()
        assertTrue(
            "Expected system tray to sit on the right side of the taskbar",
            trayBounds.left > taskbarBounds.left && trayBounds.right <= taskbarBounds.right
        )

        // Open Start Menu -> verify Search bar, Pinned apps, Recommended files & Retract
        composeRule.onNodeWithTag("taskbar_start_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("win11_start_menu").assertIsDisplayed()
        composeRule.onNodeWithTag("start_search_input").assertIsDisplayed()
        composeRule.onNodeWithTag("start_pinned_apps_area").assertIsDisplayed()
        composeRule.onNodeWithTag("start_recommended_files_area").assertIsDisplayed()
        composeRule.onNodeWithText("Pinned").assertIsDisplayed()
        composeRule.onNodeWithText("Recommended").assertIsDisplayed()
        composeRule.onNodeWithText("Welcome to Win11.txt").assertIsDisplayed()

        // Search inside Start Menu for a recommended file
        composeRule.onNodeWithTag("start_search_input").performTextInput("Welcome")
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Welcome to Win11.txt").assertIsDisplayed()

        // Retract Start Menu via Start button toggle
        composeRule.onNodeWithTag("taskbar_start_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("win11_start_menu").assertDoesNotExist()

        // Open Quick Settings from System Tray
        composeRule.onNodeWithTag("tray_quick_settings_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("win11_quick_settings").assertIsDisplayed()

        // Open Calendar Flyout from Clock & Date
        composeRule.onNodeWithTag("tray_clock_date_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("win11_calendar_flyout").assertIsDisplayed()

        // Open File Explorer directly from Taskbar as a floating window
        composeRule.onNodeWithTag("taskbar_file_explorer_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("window_FILE_EXPLORER").assertIsDisplayed()
        composeRule.onNodeWithTag("win11_file_explorer").assertIsDisplayed()
        composeRule.onNodeWithText("Quick access").assertIsDisplayed()

        // Verify all three top-right caption buttons (Minimize, Cascade/Maximize, Close) are displayed
        composeRule.onNodeWithTag("window_minimize_FILE_EXPLORER").assertIsDisplayed()
        composeRule.onNodeWithTag("window_maximize_FILE_EXPLORER").assertIsDisplayed()
        composeRule.onNodeWithTag("window_close_FILE_EXPLORER").assertIsDisplayed()
        composeRule.onNodeWithTag("window_resize_br_FILE_EXPLORER").assertIsDisplayed()

        // Verify floating window is smaller than root desktop and can be resized
        val initialBounds = composeRule.onNodeWithTag("window_FILE_EXPLORER").getBoundsInRoot()
        composeRule.onNodeWithTag("window_resize_br_FILE_EXPLORER").performTouchInput {
            swipe(
                start = center,
                end = Offset(center.x - 40f, center.y - 40f),
                durationMillis = 150
            )
        }
        composeRule.waitForIdle()
        val resizedBounds = composeRule.onNodeWithTag("window_FILE_EXPLORER").getBoundsInRoot()
        assertTrue(
            "Expected window size to shrink after dragging resize handle",
            resizedBounds.right < initialBounds.right || resizedBounds.bottom < initialBounds.bottom
        )

        // Verify dragging the title bar moves the floating window
        composeRule.onNodeWithTag("window_title_bar_FILE_EXPLORER").performTouchInput {
            swipe(
                start = Offset(centerLeft.x + 40f, center.y),
                end = Offset(centerLeft.x + 80f, center.y + 30f),
                durationMillis = 150
            )
        }
        composeRule.waitForIdle()
        val movedBounds = composeRule.onNodeWithTag("window_FILE_EXPLORER").getBoundsInRoot()
        assertTrue(
            "Expected window position to change after dragging title bar",
            movedBounds.left != resizedBounds.left || movedBounds.top != resizedBounds.top
        )

        // Toggle Maximize and then Cascade / Restore back
        composeRule.onNodeWithTag("window_maximize_FILE_EXPLORER").performClick()
        composeRule.waitForIdle()
        val maximizedBounds = composeRule.onNodeWithTag("window_FILE_EXPLORER").getBoundsInRoot()
        assertTrue(
            "Expected maximized window to expand wider than floating size",
            (maximizedBounds.right - maximizedBounds.left) > (movedBounds.right - movedBounds.left)
        )

        composeRule.onNodeWithTag("window_maximize_FILE_EXPLORER").performClick()
        composeRule.waitForIdle()
        val restoredBounds = composeRule.onNodeWithTag("window_FILE_EXPLORER").getBoundsInRoot()
        assertTrue(
            "Expected cascade/restore to shrink window back to floating size",
            (restoredBounds.right - restoredBounds.left) < (maximizedBounds.right - maximizedBounds.left)
        )

        // Open a new tab inside File Explorer
        composeRule.onNodeWithTag("explorer_new_tab_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("explorer_tab_1").assertIsDisplayed()

        // Navigate File Explorer to This PC via desktop shortcut
        composeRule.onNodeWithTag("window_minimize_FILE_EXPLORER").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("desktop_shortcut_sys_this_pc").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("win11_file_explorer").assertIsDisplayed()
        composeRule.onNodeWithText("Devices and drives").assertIsDisplayed()

        // Close window via top-right close button
        composeRule.onNodeWithTag("window_close_FILE_EXPLORER").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("window_FILE_EXPLORER").assertDoesNotExist()
    }

    @Test
    fun `safe home launcher dialog and github update settings work cleanly`() {
        composeRule.waitForIdle()

        // Open Personalize window from desktop shortcut
        composeRule.onNodeWithTag("desktop_shortcut_sys_personalize").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("window_SETTINGS_PERSONALIZE").assertIsDisplayed()
        composeRule.onNodeWithTag("settings_home_launcher_card").assertIsDisplayed()
        composeRule.onNodeWithTag("settings_github_update_card").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("settings_github_repo_input").assertIsDisplayed()

        // Open Safe Home Chooser Dialog
        composeRule.onNodeWithTag("settings_set_default_home").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("safe_home_dialog").assertIsDisplayed()
        composeRule.onNodeWithTag("safe_home_confirm_button").assertIsDisplayed()
        composeRule.onNodeWithTag("safe_home_preview_button").assertIsDisplayed()

        // Stay in safe preview mode to dismiss
        composeRule.onNodeWithTag("safe_home_preview_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("safe_home_dialog").assertDoesNotExist()
    }

    @Test
    fun `widget button is removed from taskbar and taskbar personalization updates height and components`() {
        composeRule.waitForIdle()

        // Widget button is removed from taskbar
        composeRule.onNodeWithTag("taskbar_widgets_button").assertDoesNotExist()
        composeRule.onNodeWithTag("taskbar_task_view_button").assertIsDisplayed()

        val initialBarBounds = composeRule.onNodeWithTag("win11_taskbar").getBoundsInRoot()
        val initialBarHeight = initialBarBounds.bottom - initialBarBounds.top

        // Open Personalize -> scroll to Taskbar Personalization card
        composeRule.onNodeWithTag("desktop_shortcut_sys_personalize").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_taskbar_card").performScrollTo().assertIsDisplayed()

        // Change height preset to Tall (64 dp)
        composeRule.onNodeWithTag("settings_taskbar_height_preset_64").performScrollTo().performClick()
        composeRule.waitUntil(timeoutMillis = 3_000L) {
            val bounds = composeRule.onNodeWithTag("win11_taskbar").getBoundsInRoot()
            (bounds.bottom - bounds.top) > initialBarHeight
        }

        // Toggle Task View component visibility off
        composeRule.onNodeWithTag("settings_toggle_taskbar_task_view").performScrollTo().performClick()
        composeRule.waitUntil(timeoutMillis = 3_000L) {
            composeRule.onAllNodes(androidx.compose.ui.test.hasTestTag("taskbar_task_view_button"))
                .fetchSemanticsNodes().isEmpty()
        }

        // Toggle Network indicator off
        composeRule.onNodeWithTag("settings_toggle_tray_network").performScrollTo().performClick()
        composeRule.waitUntil(timeoutMillis = 3_000L) {
            composeRule.onAllNodes(
                androidx.compose.ui.test.hasTestTag("tray_wifi_indicator"),
                useUnmergedTree = true
            ).fetchSemanticsNodes().isEmpty()
        }

        // Reset taskbar defaults restores components
        composeRule.onNodeWithTag("settings_taskbar_reset").performScrollTo().performClick()
        composeRule.waitUntil(timeoutMillis = 3_000L) {
            composeRule.onAllNodes(androidx.compose.ui.test.hasTestTag("taskbar_task_view_button"))
                .fetchSemanticsNodes().isNotEmpty() &&
                composeRule.onAllNodes(
                    androidx.compose.ui.test.hasTestTag("tray_wifi_indicator"),
                    useUnmergedTree = true
                ).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun `downloaded update apk file is deleted after cleanup`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
        val dummyApk = File(updatesDir, "win11_update_v2.0.0.apk").apply {
            writeBytes(byteArrayOf(0x50, 0x4B, 0x03, 0x04, 1, 2, 3))
        }
        assertTrue(dummyApk.exists())

        val cleaned = GitHubUpdateRepository().cleanupUpdateFiles(context, dummyApk.absolutePath)
        assertTrue(cleaned)
        assertFalse(dummyApk.exists())
    }

    @Test
    fun `floating taskbar and panel settings window lets user toggle navbar and panels independently`() {
        composeRule.waitForIdle()

        // Open Personalize as a floating window -> verify Hide Navbar switch -> open floating Panel & Taskbar window
        composeRule.onNodeWithTag("desktop_shortcut_sys_personalize").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("window_SETTINGS_PERSONALIZE").assertIsDisplayed()
        composeRule.onNodeWithTag("window_resize_br_SETTINGS_PERSONALIZE").assertIsDisplayed()
        composeRule.onNodeWithTag("settings_toggle_hide_navbar").performScrollTo().assertIsDisplayed()

        composeRule.onNodeWithTag("settings_open_panel_selector").performScrollTo().performClick()
        composeRule.waitForIdle()

        // Verify Taskbar & Panel Settings opens inside a floating Win11WindowFrame
        composeRule.onNodeWithTag("window_TASKBAR_PANEL_SETTINGS").assertIsDisplayed()
        composeRule.onNodeWithTag("window_minimize_TASKBAR_PANEL_SETTINGS").assertIsDisplayed()
        composeRule.onNodeWithTag("window_maximize_TASKBAR_PANEL_SETTINGS").assertIsDisplayed()
        composeRule.onNodeWithTag("window_close_TASKBAR_PANEL_SETTINGS").assertIsDisplayed()
        composeRule.onNodeWithTag("window_resize_br_TASKBAR_PANEL_SETTINGS").assertIsDisplayed()
        composeRule.onNodeWithTag("taskbar_panel_settings_content").assertIsDisplayed()

        // Toggle Hide Navbar inside the floating window
        composeRule.onNodeWithTag("selector_toggle_hide_navbar").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("selector_toggle_hide_navbar").performScrollTo().performClick()
        composeRule.waitForIdle()

        // Toggle Task View off and verify taskbar hides Task View
        composeRule.onNodeWithTag("selector_toggle_task_view").performScrollTo().performClick()
        composeRule.waitUntil(timeoutMillis = 3_000L) {
            composeRule.onAllNodes(androidx.compose.ui.test.hasTestTag("taskbar_task_view_button"))
                .fetchSemanticsNodes().isEmpty()
        }

        // Toggle Show Desktop off and verify taskbar hides Show Desktop sliver
        composeRule.onNodeWithTag("selector_toggle_show_desktop").performScrollTo().performClick()
        composeRule.waitUntil(timeoutMillis = 3_000L) {
            composeRule.onAllNodes(androidx.compose.ui.test.hasTestTag("tray_show_desktop_button"))
                .fetchSemanticsNodes().isEmpty()
        }

        // Reset defaults inside floating window and close via caption Close button
        composeRule.onNodeWithTag("selector_reset_button").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000L) {
            composeRule.onAllNodes(androidx.compose.ui.test.hasTestTag("taskbar_task_view_button"))
                .fetchSemanticsNodes().isNotEmpty() &&
                composeRule.onAllNodes(androidx.compose.ui.test.hasTestTag("tray_show_desktop_button"))
                    .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag("window_close_TASKBAR_PANEL_SETTINGS").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("window_TASKBAR_PANEL_SETTINGS").assertDoesNotExist()
    }

    @Test
    fun `windows 11 system tray shows fluent indicators and flyouts`() {
        composeRule.waitForIdle()

        // Verify all Windows 11 System Tray clusters are displayed
        composeRule.onNodeWithTag("tray_hidden_icons_button").assertIsDisplayed()
        composeRule.onNodeWithTag("tray_quick_settings_button").assertIsDisplayed()
        composeRule.onNodeWithTag("tray_wifi_indicator", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("tray_volume_indicator", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("tray_battery_status", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("tray_clock_date_button").assertIsDisplayed()
        composeRule.onNodeWithTag("tray_notification_bell", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("tray_show_desktop_button").assertIsDisplayed()

        // Open Hidden Tray Flyout from chevron (^)
        composeRule.onNodeWithTag("tray_hidden_icons_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("win11_hidden_tray_flyout").assertIsDisplayed()

        // Open Quick Settings Flyout and verify Customize System Tray + Personalize footer
        composeRule.onNodeWithTag("tray_quick_settings_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("win11_quick_settings").assertIsDisplayed()
        composeRule.onNodeWithTag("quick_settings_tray_customize").assertIsDisplayed()
        composeRule.onNodeWithTag("quick_settings_personalize_button").assertIsDisplayed()

        // Open Calendar & Notifications Flyout and test collapsible calendar toggle
        composeRule.onNodeWithTag("tray_clock_date_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("win11_calendar_flyout").assertIsDisplayed()
        composeRule.onNodeWithText("Notifications").assertIsDisplayed()
        composeRule.onNodeWithTag("calendar_collapse_toggle").performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun `desktop app widget can be added via picker, resized freely, and removed`() {
        composeRule.waitForIdle()
        val context = ApplicationProvider.getApplicationContext<Context>()

        // 1. Open Personalize -> tap "Tambah Widget Aplikasi ke Desktop..."
        composeRule.onNodeWithTag("desktop_shortcut_sys_personalize").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_open_widget_picker").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("win11_widget_picker_dialog").assertIsDisplayed()
        composeRule.onNodeWithTag("widget_picker_search").assertIsDisplayed()

        // Close dialog & Personalize window
        composeRule.onNodeWithContentDescription("Tutup pemilih widget").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("window_close_SETTINGS_PERSONALIZE").performClick()
        composeRule.waitForIdle()

        // 2. Persist a DesktopWidget directly to DataStore and verify it renders on Desktop
        val testWidget = DesktopWidget(
            appWidgetId = 77,
            providerPackage = "com.example.clock",
            providerClass = "ClockWidgetProvider",
            label = "Jam Analog",
            xDp = 120f,
            yDp = 32f,
            widthDp = 220f,
            heightDp = 140f,
            minWidthDp = 96,
            minHeightDp = 72
        )
        runBlocking {
            LauncherPreferencesRepository(context).addDesktopWidget(testWidget)
        }

        composeRule.waitUntil(timeoutMillis = 3_000L) {
            composeRule.onAllNodes(androidx.compose.ui.test.hasTestTag("desktop_widget_77"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("desktop_widget_77").assertIsDisplayed()
        composeRule.onNodeWithText("Jam Analog").assertIsDisplayed()

        // 3. Enter widget edit mode via the quick-edit trigger badge
        composeRule.onNodeWithTag("widget_edit_trigger_77").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("widget_size_label_77").assertIsDisplayed()
        composeRule.onNodeWithTag("widget_resize_br_77").assertIsDisplayed()
        composeRule.onNodeWithTag("widget_resize_right_77").assertIsDisplayed()
        composeRule.onNodeWithTag("widget_resize_bottom_77").assertIsDisplayed()

        // 4. Drag bottom-right resize handle outward to enlarge widget
        val beforeResize = composeRule.onNodeWithTag("desktop_widget_77").getBoundsInRoot()
        composeRule.onNodeWithTag("widget_resize_br_77").performTouchInput {
            swipe(
                start = center,
                end = Offset(center.x + 80f, center.y + 60f),
                durationMillis = 150
            )
        }
        composeRule.waitForIdle()
        val afterResize = composeRule.onNodeWithTag("desktop_widget_77").getBoundsInRoot()
        assertTrue(
            "Expected desktop widget bounds to grow after dragging resize handle",
            (afterResize.right - afterResize.left) > (beforeResize.right - beforeResize.left) ||
                (afterResize.bottom - afterResize.top) > (beforeResize.bottom - beforeResize.top)
        )

        // 5. Remove widget cleanly
        composeRule.onNodeWithTag("widget_remove_77").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000L) {
            composeRule.onAllNodes(androidx.compose.ui.test.hasTestTag("desktop_widget_77"))
                .fetchSemanticsNodes().isEmpty()
        }
    }
}
