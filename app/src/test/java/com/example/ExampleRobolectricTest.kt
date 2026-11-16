package com.example

import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.test.core.app.ApplicationProvider
import com.example.data.GitHubUpdateRepository
import java.io.File
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
}
