package com.trishit.optimite.platform

import com.sun.jna.Native
import com.sun.jna.Platform
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.win32.StdCallLibrary
import com.sun.jna.win32.W32APIOptions
import java.awt.Window
import java.awt.Component

/**
 * Applies Windows 11 Mica or Acrylic effect to a JVM AWT window
 * using DWM (Desktop Window Manager) APIs via JNA.
 *
 * Falls back gracefully on Windows 10, macOS, and Linux.
 */
object WindowEffects {

    // DWM attribute constants
    private const val DWMWA_USE_IMMERSIVE_DARK_MODE = 20
    private const val DWMWA_SYSTEMBACKDROP_TYPE = 38   // Win11 22H2+
    private const val DWMWA_MICA_EFFECT = 1029         // Win11 early builds fallback
    private const val DWMSBT_MAINWINDOW = 2            // Mica
    private const val DWMSBT_TRANSIENTWINDOW = 3       // Acrylic
    private const val DWMSBT_TABBEDWINDOW = 4          // Mica Alt

    private const val DWMWA_BORDER_COLOR = 34
    private const val DWMWA_CAPTION_COLOR = 35
    private const val DWMWA_TEXT_COLOR = 36
    private const val DWMWA_WINDOW_CORNER_PREFERENCE = 33
    private const val DWMWCP_ROUND = 2
    private const val DWMWCP_ROUNDSMALL = 3

    interface Dwmapi : StdCallLibrary {
        fun DwmSetWindowAttribute(
            hwnd: WinDef.HWND,
            dwAttribute: Int,
            pvAttribute: Pointer,
            cbAttribute: Int
        ): Int

        fun DwmExtendFrameIntoClientArea(
            hwnd: WinDef.HWND,
            pMarInset: IntArray
        ): Int

        companion object {
            val INSTANCE: Dwmapi? = runCatching {
                Native.load("dwmapi", Dwmapi::class.java, W32APIOptions.DEFAULT_OPTIONS)
            }.getOrNull()
        }
    }

    private fun getHwnd(window: Window): WinDef.HWND? {
        // 1. Try JNA's built-in way to get the component ID (HWND on Windows)
        try {
            val hwndLong = Native.getComponentID(window)
            if (hwndLong != 0L) {
                return WinDef.HWND(Pointer(hwndLong))
            }
        } catch (e: Throwable) {
            // Ignore and try fallbacks
        }

        // 2. Try reflection to get the peer's HWND without referencing java.awt.peer directly
        try {
            val getPeerMethod = Component::class.java.getMethod("getPeer")
            val peer = getPeerMethod.invoke(window)
            if (peer != null) {
                val getHWndMethod = peer.javaClass.methods.firstOrNull { it.name == "getHWnd" || it.name == "hwnd" }
                val hwnd = getHWndMethod?.invoke(peer)
                if (hwnd is Long) {
                    return WinDef.HWND(Pointer(hwnd))
                }
            }
        } catch (e: Throwable) {
            // Ignore and try next fallback
        }

        // 3. Try direct field access as a last resort
        return try {
            val peerField = Component::class.java.getDeclaredField("peer")
            peerField.isAccessible = true
            val peer = peerField.get(window) ?: return null
            val hwndField = peer.javaClass.getDeclaredField("hwnd")
            hwndField.isAccessible = true
            val hwndLong = hwndField.getLong(peer)
            WinDef.HWND(Pointer(hwndLong))
        } catch (e: Throwable) {
            null
        }
    }

    /**
     * Apply dark Mica backdrop on Windows 11, Acrylic fallback on Win10,
     * rounded corners, dark caption. No-op on other OS.
     */
    fun applyDarkMica(window: Window) {
        if (!Platform.isWindows()) return
        val dwm = Dwmapi.INSTANCE ?: return
        val hwnd = getHwnd(window) ?: return

        runCatching {
            val mem = com.sun.jna.Memory(4)

            // 1. Force dark mode title bar
            mem.setInt(0, 1)
            dwm.DwmSetWindowAttribute(hwnd, DWMWA_USE_IMMERSIVE_DARK_MODE, mem, 4)

            // 2. Rounded corners (Win11)
            mem.setInt(0, DWMWCP_ROUND)
            dwm.DwmSetWindowAttribute(hwnd, DWMWA_WINDOW_CORNER_PREFERENCE, mem, 4)

            // 3. Dark caption bar color (near-black with slight blue tint)
            // Color is COLORREF = 0x00BBGGRR
            mem.setInt(0, 0x00120F0A) // very dark
            dwm.DwmSetWindowAttribute(hwnd, DWMWA_CAPTION_COLOR, mem, 4)

            // 4. Light text color on caption
            mem.setInt(0, 0x00D4D4D4)
            dwm.DwmSetWindowAttribute(hwnd, DWMWA_TEXT_COLOR, mem, 4)

            // 5. Accent border color (cyan glow)
            mem.setInt(0, 0x00FFD400) // COLORREF cyan = R=0x00,G=0xD4,B=0xFF → 0x00FFD400
            dwm.DwmSetWindowAttribute(hwnd, DWMWA_BORDER_COLOR, mem, 4)

            // 6a. Try Win11 22H2+ Mica backdrop
            mem.setInt(0, DWMSBT_MAINWINDOW)
            val micaResult = dwm.DwmSetWindowAttribute(hwnd, DWMWA_SYSTEMBACKDROP_TYPE, mem, 4)

            if (micaResult != 0) {
                // 6b. Fallback: older Win11 mica toggle
                mem.setInt(0, 1)
                dwm.DwmSetWindowAttribute(hwnd, DWMWA_MICA_EFFECT, mem, 4)
            }

            // Avoid full-client glass extension: it can swallow Compose content on transparent windows.
            // Keeping only backdrop attributes is more reliable for Compose Desktop rendering.
        }
    }

    /**
     * Apply Acrylic (blurred transparency) instead of Mica.
     * Better for floating panels.
     */
    fun applyAcrylic(window: Window) {
        if (!Platform.isWindows()) return
        val dwm = Dwmapi.INSTANCE ?: return
        val hwnd = getHwnd(window) ?: return

        runCatching {
            val mem = com.sun.jna.Memory(4)
            mem.setInt(0, 1)
            dwm.DwmSetWindowAttribute(hwnd, DWMWA_USE_IMMERSIVE_DARK_MODE, mem, 4)

            mem.setInt(0, DWMWCP_ROUND)
            dwm.DwmSetWindowAttribute(hwnd, DWMWA_WINDOW_CORNER_PREFERENCE, mem, 4)

            mem.setInt(0, 0x00120F0A)
            dwm.DwmSetWindowAttribute(hwnd, DWMWA_CAPTION_COLOR, mem, 4)

            mem.setInt(0, 0x00FFD400)
            dwm.DwmSetWindowAttribute(hwnd, DWMWA_BORDER_COLOR, mem, 4)

            mem.setInt(0, DWMSBT_TRANSIENTWINDOW)
            dwm.DwmSetWindowAttribute(hwnd, DWMWA_SYSTEMBACKDROP_TYPE, mem, 4)

            // Same as Mica path: do not extend glass into the client area.
        }
    }

    val isMicaSupported: Boolean get() {
        if (!Platform.isWindows()) return false
        return try {
            // Win11 reports os.version as "10.0" and build via a separate property
            // The build number >= 22000 indicates Windows 11
            val buildStr = System.getProperty("os.version", "0.0.0")
                .split(".")
                .getOrNull(2)
                ?.toIntOrNull()
                ?: 0
            // Fallback: check via ProcessHandle or native ver command
            if (buildStr >= 22000) return true
            // Alternative: try reading from registry via environment
            val osName = System.getProperty("os.name", "").lowercase()
            osName.contains("windows 11")
        } catch (e: Exception) { false }
    }
}
