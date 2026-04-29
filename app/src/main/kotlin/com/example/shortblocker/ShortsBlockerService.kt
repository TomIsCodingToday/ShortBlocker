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
        // Check 1: URL in event text — reliable regardless of event type
        event.text?.forEach { text ->
            if (text?.contains("/shorts/", ignoreCase = true) == true) return true
        }

        // Checks 2 and 3 only run on TYPE_WINDOW_STATE_CHANGED (a new screen/fragment
        // just opened). Skipping content-change events prevents false positives from
        // the regular feed scrolling or the Shorts nav tab being visible on screen.
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return false

        // Check 2: the fragment/activity class name YouTube reports when Shorts opens
        val className = event.className?.toString() ?: ""
        if (className.contains("reel", ignoreCase = true) ||
            className.contains("short", ignoreCase = true)) return true

        // Check 3: view hierarchy contains a Shorts player node ID
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
