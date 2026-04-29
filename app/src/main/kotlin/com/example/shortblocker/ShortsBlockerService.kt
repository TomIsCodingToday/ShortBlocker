package com.example.shortblocker

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class ShortsBlockerService : AccessibilityService() {

    private var lastBackPressTime = 0L
    private val COOLDOWN_MS = 1500L

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.packageName != "com.google.android.youtube") return

        val now = System.currentTimeMillis()
        if (now - lastBackPressTime < COOLDOWN_MS) return

        if (isShortsContent(event)) {
            lastBackPressTime = now
            performGlobalAction(GLOBAL_ACTION_BACK)
        }
    }

    private fun isShortsContent(event: AccessibilityEvent): Boolean {
        // Check event text for a Shorts URL fragment
        event.text?.forEach { text ->
            if (text?.contains("/shorts/", ignoreCase = true) == true) return true
        }

        // Walk the view hierarchy looking for Shorts-specific node IDs.
        // YouTube's internal "Reel Watch" experience uses view IDs prefixed with "reel_",
        // e.g. reel_player_page_container, reel_watch_fragment_root. These never appear
        // in the regular video player or home feed.
        val root = rootInActiveWindow ?: return false
        return try {
            containsShortsNode(root, depth = 0)
        } finally {
            root.recycle()
        }
    }

    private fun containsShortsNode(node: AccessibilityNodeInfo, depth: Int): Boolean {
        if (depth > 8) return false

        val resId = node.viewIdResourceName ?: ""
        if (resId.contains("reel", ignoreCase = true)) return true

        val cd = node.contentDescription?.toString() ?: ""
        if (cd.equals("shorts", ignoreCase = true)) return true

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = containsShortsNode(child, depth + 1)
            child.recycle()
            if (found) return true
        }
        return false
    }

    override fun onInterrupt() = Unit
}
