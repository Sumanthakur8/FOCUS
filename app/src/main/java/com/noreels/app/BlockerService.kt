package com.noreels.app

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

class BlockerService : AccessibilityService() {
    private var lastBlock = 0L

    // View IDs used by the apps' Shorts/Reels screens. Apps change these in updates,
    // so edit this list if blocking stops working.
    private val ytIds = listOf("reel_recycler", "reel_player_page_container", "shorts_container")
    private val igIds = listOf("clips_viewer_view_pager", "clips_swipe_refresh_container")

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        val prefs = getSharedPreferences("noreels", MODE_PRIVATE)
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
            performGlobalAction(GLOBAL_ACTION_BACK)
            Toast.makeText(this, "Blocked by NoReels", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onInterrupt() {}
}
