package com.noreels.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("noreels", MODE_PRIVATE)
        val pad = (24 * resources.displayMetrics.density).toInt()
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad * 2, pad, pad)
        }
        layout.addView(TextView(this).apply { text = "NoReels"; textSize = 28f })
        layout.addView(TextView(this).apply {
            text = "Step 1: tap the button below, find \"NoReels Blocker\" and turn it on.\nStep 2: choose what to block."
            setPadding(0, pad / 2, 0, pad / 2)
        })
        layout.addView(Button(this).apply {
            text = "Open Accessibility Settings"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        })
        fun toggle(label: String, key: String) {
            layout.addView(Switch(this).apply {
                text = label
                textSize = 18f
                isChecked = prefs.getBoolean(key, true)
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, pad / 2, 0, pad / 2)
                setOnCheckedChangeListener { _, on -> prefs.edit().putBoolean(key, on).apply() }
            })
        }
        toggle("Block YouTube Shorts", "yt")
        toggle("Block Instagram Reels", "ig")
        setContentView(layout)
    }
}
