package com.noreels.app

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BlockerService : AccessibilityService() {
    private var lastBlock = 0L

    // View IDs used by the apps' Shorts/Reels screens. Apps change these in updates,
    // so edit this list if blocking stops working.
    private val ytIds = listOf("reel_recycler", "reel_player_page_container", "shorts_container")
    private val igIds = listOf("clips_viewer_view_pager", "clips_swipe_refresh_container")

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        val prefs = getSharedPreferences("noreels", MODE_PRIVATE)
        // User-selected apps are blocked completely, except YouTube and Instagram,
        // which have dedicated Shorts/Reels-only controls below.
        val selectedApps = prefs.getStringSet("blocked_apps", emptySet()) ?: emptySet()
        if (pkg != "com.google.android.youtube" && pkg != "com.instagram.android" && selectedApps.contains(pkg)) {
            val now = System.currentTimeMillis()
            if (now - lastBlock > 1200) {
                lastBlock = now
                recordBlock(prefs, now)
                performGlobalAction(GLOBAL_ACTION_BACK)
                android.widget.Toast.makeText(this, "App blocked by FOCUS", android.widget.Toast.LENGTH_SHORT).show()
            }
            return
        }
        val ids = when {
            pkg == "com.google.android.youtube" && prefs.getBoolean("yt", true) -> ytIds
            pkg == "com.instagram.android" && prefs.getBoolean("ig", true) -> igIds
            else -> return
        }
        val root = rootInActiveWindow ?: return
        val found = ids.any { root.findAccessibilityNodeInfosByViewId("$pkg:id/$it").isNotEmpty() }
        val now = System.currentTimeMillis()
        if (found && now - lastBlock > 1200) {
            lastBlock = now
            // Count a successful block locally for today's focus progress and lifetime stats.
            recordBlock(prefs, now)
            performGlobalAction(GLOBAL_ACTION_BACK)
            Toast.makeText(this, "Blocked by FOCUS", Toast.LENGTH_SHORT).show()
        }
    }

    private fun recordBlock(prefs: android.content.SharedPreferences, now: Long) {
        val day = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date(now))
        prefs.edit()
            .putInt("blocked_$day", prefs.getInt("blocked_$day", 0) + 1)
            .putInt("blocked_total", prefs.getInt("blocked_total", 0) + 1)
            .apply()
    }

    override fun onInterrupt() {}
}
