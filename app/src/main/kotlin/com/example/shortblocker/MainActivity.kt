package com.example.shortblocker

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(64, 64, 64, 64)
        }

        tvStatus = TextView(this).apply {
            textSize = 22f
            gravity = Gravity.CENTER
        }

        val tvInstructions = TextView(this).apply {
            setText(R.string.instructions)
            gravity = Gravity.CENTER
            setPadding(0, 32, 0, 32)
        }

        val btnSettings = Button(this).apply {
            setText(R.string.btn_open_settings)
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }

        root.addView(tvStatus)
        root.addView(tvInstructions)
        root.addView(btnSettings)
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun isServiceEnabled(): Boolean {
        val enabled = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val component = "$packageName/.ShortsBlockerService"
        return enabled.split(":").any { it.equals(component, ignoreCase = true) }
    }

    private fun updateStatus() {
        if (isServiceEnabled()) {
            tvStatus.setText(R.string.status_enabled)
            tvStatus.setTextColor(getColor(android.R.color.holo_green_dark))
        } else {
            tvStatus.setText(R.string.status_disabled)
            tvStatus.setTextColor(getColor(android.R.color.holo_red_dark))
        }
    }
}
