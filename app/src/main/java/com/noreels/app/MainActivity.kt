package com.noreels.app

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private val bg = Color.rgb(245, 247, 251)
    private val ink = Color.rgb(25, 31, 55)
    private val muted = Color.rgb(105, 114, 139)
    private val purple = Color.rgb(91, 92, 226)
    private val green = Color.rgb(27, 146, 105)
    private val prefs by lazy { getSharedPreferences("noreels", MODE_PRIVATE) }
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        render()
    }

    override fun onResume() {
        super.onResume()
        if (prefs != null) render()
    }

    private fun isBlockerEnabled(): Boolean {
        val enabled = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        return enabled.split(':').any { it.equals("$packageName/com.noreels.app.BlockerService", true) }
    }

    private fun rounded(color: Int, radius: Int = 22, stroke: Int = 0): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            if (stroke != 0) setStroke(dp(1), stroke)
        }

    private fun label(text: String, size: Float, color: Int, bold: Boolean = false): TextView =
        TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(typeface, Typeface.BOLD)
            includeFontPadding = true
        }

    private fun card(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(18), dp(17), dp(18), dp(17))
        background = rounded(Color.WHITE, 22, Color.rgb(232, 235, 244))
        elevation = dp(2).toFloat()
    }

    private fun addGap(parent: LinearLayout, height: Int = 14) {
        parent.addView(View(this), LinearLayout.LayoutParams(1, dp(height)))
    }

    private fun render() {
        val enabled = isBlockerEnabled()
        val scroll = ScrollView(this).apply { setBackgroundColor(bg); clipToPadding = false }
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(28))
        }
        scroll.addView(page)

        val brandRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val logo = TextView(this).apply {
            text = "N"
            textSize = 23f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR,
                intArrayOf(purple, Color.rgb(129, 83, 218))).apply { cornerRadius = dp(15).toFloat() }
            layoutParams = LinearLayout.LayoutParams(dp(48), dp(48))
        }
        brandRow.addView(logo)
        val brandText = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)
            addView(label("NoReels", 21f, ink, true))
            addView(label("YOUR ATTENTION. YOUR CHOICE.", 9.5f, muted, true))
        }
        brandRow.addView(brandText, LinearLayout.LayoutParams(0, -2, 1f))
        val pill = label(if (enabled) "● ACTIVE" else "○ SETUP", 10f, if (enabled) green else purple, true).apply {
            setPadding(dp(10), dp(8), dp(10), dp(8))
            background = rounded(if (enabled) Color.rgb(226, 248, 240) else Color.rgb(237, 237, 255), 30)
        }
        brandRow.addView(pill)
        page.addView(brandRow)
        addGap(page, 22)

        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(24), dp(22), dp(23))
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(43, 45, 105), Color.rgb(91, 72, 190))).apply { cornerRadius = dp(26).toFloat() }
        }
        hero.addView(label("MAKE ROOM FOR\nWHAT MATTERS.", 27f, Color.WHITE, true))
        addGap(hero, 10)
        hero.addView(label("A calmer feed starts with fewer endless scrolls. NoReels helps you leave Shorts and Reels automatically.", 14f, Color.rgb(226, 228, 255)).apply { setLineSpacing(dp(3).toFloat(), 1f) })
        addGap(hero, 18)
        val heroStatus = label(if (enabled) "✓  BLOCKER IS TURNED ON" else "1-MINUTE SETUP  •  NO ACCOUNT NEEDED", 10.5f, Color.WHITE, true).apply {
            gravity = Gravity.CENTER
            setPadding(dp(13), dp(12), dp(13), dp(12))
            background = rounded(Color.argb(35, 255, 255, 255), 14, Color.argb(60,255,255,255))
        }
        hero.addView(heroStatus)
        page.addView(hero)
        addGap(page, 22)

        page.addView(label("YOUR BLOCKING SETTINGS", 12f, muted, true))
        addGap(page, 10)
        val setup = card()
        setup.addView(label(if (enabled) "You're ready to go" else "One quick step to get started", 18f, ink, true))
        addGap(setup, 6)
        setup.addView(label(if (enabled) "NoReels accessibility service is enabled. Choose which apps you want to manage below." else "Allow NoReels to detect when a Shorts or Reels screen is open. Android requires this permission for blocking to work.", 13f, muted).apply { setLineSpacing(dp(2).toFloat(), 1f) })
        addGap(setup, 14)
        val action = Button(this).apply {
            text = if (enabled) "Review accessibility settings" else "Enable NoReels blocker  →"
            textSize = 14f
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = rounded(purple, 14)
            setPadding(dp(10), dp(5), dp(10), dp(5))
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        }
        setup.addView(action, LinearLayout.LayoutParams(-1, dp(52)))
        page.addView(setup)
        addGap(page, 20)

        // Focus dashboard: local-only counts of screens the blocker successfully exited.
        val todayKey = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val todayCount = prefs.getInt("blocked_$todayKey", 0)
        val totalCount = prefs.getInt("blocked_total", 0)
        var streak = 0
        val streakCalendar = Calendar.getInstance()
        while (streak < 3650) {
            val key = SimpleDateFormat("yyyyMMdd", Locale.US).format(streakCalendar.time)
            if (prefs.getInt("blocked_$key", 0) > 0) {
                streak++
                streakCalendar.add(Calendar.DAY_OF_YEAR, -1)
            } else break
        }
        // If no blocks have happened today, count a streak from yesterday instead.
        if (todayCount == 0) {
            streak = 0
            streakCalendar.time = Date()
            streakCalendar.add(Calendar.DAY_OF_YEAR, -1)
            while (streak < 3650) {
                val key = SimpleDateFormat("yyyyMMdd", Locale.US).format(streakCalendar.time)
                if (prefs.getInt("blocked_$key", 0) > 0) {
                    streak++
                    streakCalendar.add(Calendar.DAY_OF_YEAR, -1)
                } else break
            }
        }
        val goal = prefs.getInt("daily_goal", 10).coerceIn(1, 100)
        page.addView(label("YOUR FOCUS SNAPSHOT", 12f, muted, true))
        addGap(page, 10)
        val stats = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        fun statTile(value: String, caption: String, accent: Int): LinearLayout {
            val tile = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(14), dp(16), dp(10), dp(15))
                background = rounded(Color.WHITE, 20, Color.rgb(232, 235, 244))
            }
            tile.addView(label(value, 25f, accent, true))
            tile.addView(label(caption, 11f, muted, true).apply { setPadding(0, dp(5), 0, 0) })
            return tile
        }
        stats.addView(statTile(todayCount.toString(), "BLOCKED TODAY", purple), LinearLayout.LayoutParams(0, -2, 1f))
        val statGap = View(this)
        stats.addView(statGap, LinearLayout.LayoutParams(dp(10), 1))
        stats.addView(statTile("${streak}d", "FOCUS STREAK", green), LinearLayout.LayoutParams(0, -2, 1f))
        page.addView(stats)
        addGap(page, 10)
        val goalCard = card()
        val goalHead = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val goalTitle = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        goalTitle.addView(label("Daily blocking goal", 16f, ink, true))
        goalTitle.addView(label("Aim to exit ${goal} short-video screens", 12f, muted).apply { setPadding(0, dp(4), 0, 0) })
        goalHead.addView(goalTitle, LinearLayout.LayoutParams(0, -2, 1f))
        val minus = Button(this).apply {
            text = "−"; textSize = 18f; isAllCaps = false; minWidth = dp(42)
            setTextColor(purple); background = rounded(Color.rgb(237, 237, 255), 12)
            setOnClickListener { prefs.edit().putInt("daily_goal", (goal - 5).coerceAtLeast(1)).apply(); render() }
        }
        val plus = Button(this).apply {
            text = "+"; textSize = 18f; isAllCaps = false; minWidth = dp(42)
            setTextColor(purple); background = rounded(Color.rgb(237, 237, 255), 12)
            setOnClickListener { prefs.edit().putInt("daily_goal", (goal + 5).coerceAtMost(100)).apply(); render() }
        }
        goalHead.addView(minus, LinearLayout.LayoutParams(dp(42), dp(42)))
        goalHead.addView(View(this), LinearLayout.LayoutParams(dp(6), 1))
        goalHead.addView(plus, LinearLayout.LayoutParams(dp(42), dp(42)))
        goalCard.addView(goalHead)
        addGap(goalCard, 14)
        val progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = goal
            progress = todayCount.coerceAtMost(goal)
            progressTintList = android.content.res.ColorStateList.valueOf(purple)
            progressBackgroundTintList = android.content.res.ColorStateList.valueOf(Color.rgb(235, 237, 247))
        }
        goalCard.addView(progress, LinearLayout.LayoutParams(-1, dp(8)))
        addGap(goalCard, 8)
        goalCard.addView(label("${todayCount.coerceAtMost(goal)} of ${goal} completed  •  ${if (todayCount >= goal) "Goal reached — great job!" else "Every interruption is a choice regained."}", 11f, if (todayCount >= goal) green else muted))
        page.addView(goalCard)
        addGap(page, 8)
        page.addView(label("Counts are stored on this device and track detected blocks, not total screen time.", 10.5f, muted))
        addGap(page, 22)

        page.addView(label("CHOOSE WHAT TO BLOCK", 12f, muted, true))
        addGap(page, 10)
        page.addView(platformCard("▶", "YouTube Shorts", "Stop the short-video loop on YouTube.", "yt", Color.rgb(232, 65, 68)))
        addGap(page, 10)
        page.addView(platformCard("◎", "Instagram Reels", "Keep Reels out of your scrolling routine.", "ig", Color.rgb(193, 65, 137)))
        addGap(page, 22)

        val tip = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(16), dp(15), dp(16), dp(15))
            background = rounded(Color.rgb(235, 239, 255), 18)
        }
        tip.addView(label("✦", 23f, purple, true))
        val tipText = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)
            addView(label("A small reminder", 14f, ink, true))
            addView(label("Blocking depends on app screen identifiers, which can change after updates. If a feed slips through, update NoReels or check the settings again.", 12f, muted).apply { setLineSpacing(dp(2).toFloat(), 1f) })
        }
        tip.addView(tipText)
        page.addView(tip)
        addGap(page, 18)
        page.addView(label("Simple by design. Focus on your time, not your feed.", 11f, muted).apply { gravity = Gravity.CENTER })
        setContentView(scroll)
    }

    private fun platformCard(icon: String, title: String, subtitle: String, key: String, accent: Int): LinearLayout {
        val outer = card()
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val appIcon = TextView(this).apply {
            text = icon
            textSize = 25f
            gravity = Gravity.CENTER
            setTextColor(accent)
            background = rounded(Color.rgb(250, 241, 244), 15)
            layoutParams = LinearLayout.LayoutParams(dp(52), dp(52))
        }
        row.addView(appIcon)
        val textCol = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, dp(6), 0)
            addView(label(title, 16f, ink, true))
            addView(label(subtitle, 12f, muted).apply { setPadding(0, dp(4), 0, 0) })
        }
        row.addView(textCol, LinearLayout.LayoutParams(0, -2, 1f))
        val toggle = Switch(this).apply {
            text = ""
            isChecked = prefs.getBoolean(key, true)
            contentDescription = "Block $title"
            buttonTintList = null
            setOnCheckedChangeListener { _, on -> prefs.edit().putBoolean(key, on).apply() }
        }
        row.addView(toggle)
        outer.addView(row)
        return outer
    }
}
