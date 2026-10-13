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
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ImageView
import android.widget.ProgressBar
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
    private val purple = Color.rgb(69, 91, 220)
    private val green = Color.rgb(27, 146, 105)
    private val prefs by lazy { getSharedPreferences("noreels", MODE_PRIVATE) }
    private var selectedTab = 0
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = Color.WHITE
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        render()
    }

    override fun onResume() { super.onResume(); render() }

    private fun isBlockerEnabled(): Boolean {
        val enabled = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        return enabled.split(':').any { it.equals("$packageName/com.noreels.app.BlockerService", true) }
    }

    private fun rounded(color: Int, radius: Int = 22, stroke: Int = 0): GradientDrawable =
        GradientDrawable().apply { setColor(color); cornerRadius = dp(radius).toFloat(); if (stroke != 0) setStroke(dp(1), stroke) }

    private fun label(text: String, size: Float, color: Int, bold: Boolean = false): TextView = TextView(this).apply {
        this.text = text; textSize = size; setTextColor(color); if (bold) setTypeface(typeface, Typeface.BOLD); includeFontPadding = true
    }

    private fun card(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL; setPadding(dp(17), dp(16), dp(17), dp(16))
        background = rounded(Color.WHITE, 21, Color.rgb(232, 235, 244)); elevation = dp(1).toFloat()
    }

    private fun gap(parent: LinearLayout, height: Int = 13) { parent.addView(View(this), LinearLayout.LayoutParams(1, dp(height))) }

    private fun render() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bg) }
        val scroll = ScrollView(this).apply { clipToPadding = false; isFillViewport = true }
        val page = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(18), dp(20), dp(24)) }
        scroll.addView(page)
        when (selectedTab) {
            0 -> homePage(page)
            1 -> blockersPage(page)
            2 -> insightsPage(page)
            else -> settingsPage(page)
        }
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(bottomNavigation())
        setContentView(root)
    }

    private fun brandHeader(page: LinearLayout, subtitle: String) {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val logo = TextView(this).apply {
            text = "N"; textSize = 23f; setTextColor(Color.WHITE); gravity = Gravity.CENTER; typeface = Typeface.DEFAULT_BOLD
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(purple, Color.rgb(129, 83, 218))).apply { cornerRadius = dp(15).toFloat() }
        }
        row.addView(logo, LinearLayout.LayoutParams(dp(48), dp(48)))
        val texts = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(12), 0, 0, 0); addView(label("FOCUS", 21f, ink, true)); addView(label(subtitle, 10f, muted)) }
        row.addView(texts, LinearLayout.LayoutParams(0, -2, 1f))
        val active = isBlockerEnabled()
        val pill = label(if (active) "● ACTIVE" else "○ SETUP", 10f, if (active) green else purple, true).apply {
            setPadding(dp(10), dp(8), dp(10), dp(8)); background = rounded(if (active) Color.rgb(226, 248, 240) else Color.rgb(237, 237, 255), 30)
        }
        row.addView(pill); page.addView(row); gap(page, 22)
    }

    private fun heading(page: LinearLayout, text: String) { page.addView(label(text.uppercase(Locale.getDefault()), 11f, muted, true)); gap(page, 10) }

    private fun homePage(page: LinearLayout) {
        brandHeader(page, "YOUR ATTENTION. YOUR CHOICE.")
        val active = isBlockerEnabled()
        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(dp(21), dp(23), dp(21), dp(21))
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(Color.rgb(35, 43, 104), Color.rgb(83, 75, 196))).apply { cornerRadius = dp(26).toFloat() }
        }
        hero.addView(label("MAKE ROOM FOR\nWHAT MATTERS.", 27f, Color.WHITE, true)); gap(hero, 9)
        hero.addView(label("Build a calmer digital routine by blocking endless Shorts and Reels.", 14f, Color.rgb(226, 228, 255)))
        gap(hero, 17)
        hero.addView(label(if (active) "✓  PROTECTION IS ACTIVE" else "GET STARTED IN ABOUT A MINUTE", 10.5f, Color.WHITE, true).apply {
            gravity = Gravity.CENTER; setPadding(dp(12), dp(12), dp(12), dp(12)); background = rounded(Color.argb(35,255,255,255), 13, Color.argb(70,255,255,255))
        })
        page.addView(hero); gap(page, 22)
        heading(page, "Your protection")
        page.addView(platformCard("▶", "YouTube Shorts", "Automatically leave Shorts screens", "yt", Color.rgb(232, 65, 68)))
        gap(page, 10)
        page.addView(platformCard("◎", "Instagram Reels", "Keep Reels out of your routine", "ig", Color.rgb(193, 65, 137)))
        gap(page, 18)
        val setup = card()
        setup.addView(label(if (active) "You're all set" else "Finish setting up NoReels", 17f, ink, true)); gap(setup, 5)
        setup.addView(label(if (active) "Your accessibility service is enabled. You can manage apps and settings anytime." else "Android requires Accessibility permission so NoReels can detect supported short-video screens.", 13f, muted))
        gap(setup, 12)
        setup.addView(actionButton(if (active) "Review accessibility settings" else "Enable blocker  →") { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }, LinearLayout.LayoutParams(-1, dp(48)))
        page.addView(setup); gap(page, 20)
        heading(page, "Explore NoReels")
        page.addView(optionCard("▦", "Focus insights", "Your local blocking counts and daily goal", purple) { selectedTab = 2; render() }); gap(page, 9)
        page.addView(optionCard("⚙", "App settings", "Permissions, goals and app information", green) { selectedTab = 3; render() })
        gap(page, 18)
        page.addView(label("Simple by design. Focus on your time, not your feed.", 11f, muted).apply { gravity = Gravity.CENTER })
    }

    private fun blockersPage(page: LinearLayout) {
        brandHeader(page, "MANAGE YOUR BLOCKERS")
        page.addView(label("Choose what you want to block", 25f, ink, true)); gap(page, 7)
        page.addView(label("Switch each blocker on or off. Changes are saved on this device.", 14f, muted)); gap(page, 20)
        page.addView(platformCard("▶", "YouTube Shorts", "Leave supported Shorts screens automatically", "yt", Color.rgb(232, 65, 68))); gap(page, 12)
        page.addView(platformCard("◎", "Instagram Reels", "Leave supported Reels screens automatically", "ig", Color.rgb(193, 65, 137))); gap(page, 18)
        val permission = card()
        permission.addView(label("Accessibility permission", 17f, ink, true)); gap(permission, 6)
        permission.addView(label(if (isBlockerEnabled()) "Enabled — NoReels can monitor supported screens." else "Not enabled — the blockers will not work until you enable the service.", 13f, muted)); gap(permission, 12)
        permission.addView(actionButton("Open accessibility settings") { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }, LinearLayout.LayoutParams(-1, dp(48)))
        page.addView(permission); gap(page, 16)
        page.addView(label("Note: YouTube and Instagram can change their screen identifiers in updates, which may affect detection.", 12f, muted))
    }

    private fun insightsPage(page: LinearLayout) {
        brandHeader(page, "YOUR PROGRESS, ON THIS DEVICE")
        page.addView(label("Focus insights", 27f, ink, true)); gap(page, 6)
        page.addView(label("See how often NoReels has interrupted short-video scrolling.", 14f, muted)); gap(page, 22)
        val todayKey = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val today = prefs.getInt("blocked_$todayKey", 0)
        val total = prefs.getInt("blocked_total", 0)
        var streak = 0
        val cal = Calendar.getInstance()
        while (streak < 3650) {
            val key = SimpleDateFormat("yyyyMMdd", Locale.US).format(cal.time)
            if (prefs.getInt("blocked_$key", 0) > 0) { streak++; cal.add(Calendar.DAY_OF_YEAR, -1) } else break
        }
        if (today == 0) { streak = 0; cal.time = Date(); cal.add(Calendar.DAY_OF_YEAR, -1); while (streak < 3650) { val key = SimpleDateFormat("yyyyMMdd", Locale.US).format(cal.time); if (prefs.getInt("blocked_$key", 0) > 0) { streak++; cal.add(Calendar.DAY_OF_YEAR, -1) } else break } }
        val stats = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        stats.addView(statTile(today.toString(), "BLOCKED TODAY", purple), LinearLayout.LayoutParams(0, -2, 1f))
        stats.addView(View(this), LinearLayout.LayoutParams(dp(10), 1))
        stats.addView(statTile("${streak}d", "FOCUS STREAK", green), LinearLayout.LayoutParams(0, -2, 1f))
        page.addView(stats); gap(page, 10)
        page.addView(statTile(total.toString(), "ALL-TIME DETECTED BLOCKS", ink)); gap(page, 20)
        val goal = prefs.getInt("daily_goal", 10).coerceIn(1, 100)
        val goalCard = card(); goalCard.addView(label("Daily blocking goal", 17f, ink, true)); gap(goalCard, 5)
        goalCard.addView(label("Aim to exit $goal short-video screens each day.", 13f, muted)); gap(goalCard, 12)
        val progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply { max = goal; this.progress = today.coerceAtMost(goal); progressTintList = android.content.res.ColorStateList.valueOf(purple); progressBackgroundTintList = android.content.res.ColorStateList.valueOf(Color.rgb(235,237,247)) }
        goalCard.addView(progress, LinearLayout.LayoutParams(-1, dp(8))); gap(goalCard, 8)
        goalCard.addView(label("${today.coerceAtMost(goal)} of $goal completed${if (today >= goal) " — goal reached!" else ""}", 12f, if (today >= goal) green else muted)); gap(goalCard, 12)
        val controls = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        controls.addView(actionButton("− 5") { prefs.edit().putInt("daily_goal", (goal - 5).coerceAtLeast(1)).apply(); render() }, LinearLayout.LayoutParams(0, dp(44), 1f))
        controls.addView(View(this), LinearLayout.LayoutParams(dp(8), 1))
        controls.addView(actionButton("+ 5") { prefs.edit().putInt("daily_goal", (goal + 5).coerceAtMost(100)).apply(); render() }, LinearLayout.LayoutParams(0, dp(44), 1f))
        goalCard.addView(controls); page.addView(goalCard); gap(page, 14)
        page.addView(label("These are detected blocking events, not total screen time or verified minutes saved. Data stays on this device.", 12f, muted))
    }

    private fun settingsPage(page: LinearLayout) {
        brandHeader(page, "MAKE NOREELS WORK FOR YOU")
        page.addView(label("Settings", 27f, ink, true)); gap(page, 6)
        page.addView(label("Manage permissions and learn about the app.", 14f, muted)); gap(page, 20)
        page.addView(optionCard("♧", "Accessibility permission", if (isBlockerEnabled()) "Enabled — tap to review" else "Required for blocking to work", purple) { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }); gap(page, 10)
        page.addView(optionCard("▦", "Manage blockers", "Choose which apps NoReels blocks", green) { selectedTab = 1; render() }); gap(page, 10)
        page.addView(optionCard("◷", "Focus insights", "View daily counts and your goal", purple) { selectedTab = 2; render() }); gap(page, 20)
        val about = card(); about.addView(label("About NoReels", 17f, ink, true)); gap(about, 7)
        about.addView(label("NoReels helps you reduce short-video distractions by detecting supported YouTube Shorts and Instagram Reels screens and returning you to the previous screen.", 13f, muted)); gap(about, 10)
        about.addView(label("Version 1.0  •  No account required", 11f, muted, true)); page.addView(about); gap(page, 16)
        page.addView(label("Privacy: blocking counts and preferences are stored locally on this device. Accessibility access is used to detect supported screens.", 12f, muted))
    }

    private fun statTile(value: String, caption: String, accent: Int): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL; setPadding(dp(15), dp(17), dp(12), dp(16)); background = rounded(Color.WHITE, 20, Color.rgb(232,235,244))
        addView(label(value, 26f, accent, true)); addView(label(caption, 10f, muted, true).apply { setPadding(0, dp(5), 0, 0) })
    }

    private fun actionButton(textValue: String, action: () -> Unit): Button = Button(this).apply {
        text = textValue; textSize = 13f; isAllCaps = false; setTextColor(Color.WHITE); background = rounded(purple, 14); setPadding(dp(8), dp(3), dp(8), dp(3)); setOnClickListener { action() }
    }

    private fun optionCard(symbol: String, title: String, subtitle: String, accent: Int, action: () -> Unit): LinearLayout {
        val outer = card(); outer.orientation = LinearLayout.HORIZONTAL; outer.gravity = Gravity.CENTER_VERTICAL
        val icon = TextView(this).apply { text = symbol; textSize = 23f; gravity = Gravity.CENTER; setTextColor(accent); background = rounded(Color.rgb(239,241,255), 14) }
        outer.addView(icon, LinearLayout.LayoutParams(dp(48), dp(48)))
        val texts = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(12), 0, dp(6), 0); addView(label(title, 15f, ink, true)); addView(label(subtitle, 12f, muted).apply { setPadding(0, dp(4), 0, 0) }) }
        outer.addView(texts, LinearLayout.LayoutParams(0, -2, 1f)); outer.addView(label("›", 26f, muted)); outer.setOnClickListener { action() }; return outer
    }

    private fun platformCard(symbol: String, title: String, subtitle: String, key: String, accent: Int): LinearLayout {
        val outer = card(); outer.orientation = LinearLayout.HORIZONTAL; outer.gravity = Gravity.CENTER_VERTICAL
        val appIcon = TextView(this).apply { text = symbol; textSize = 24f; gravity = Gravity.CENTER; setTextColor(accent); background = rounded(Color.rgb(250,241,244), 15) }
        outer.addView(appIcon, LinearLayout.LayoutParams(dp(49), dp(49)))
        val texts = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(12), 0, dp(5), 0); addView(label(title, 15f, ink, true)); addView(label(subtitle, 12f, muted).apply { setPadding(0, dp(4), 0, 0) }) }
        outer.addView(texts, LinearLayout.LayoutParams(0, -2, 1f))
        val toggle = Switch(this).apply { text = ""; isChecked = prefs.getBoolean(key, true); contentDescription = "Block $title"; setOnCheckedChangeListener { _, checked -> prefs.edit().putBoolean(key, checked).apply() } }
        outer.addView(toggle); return outer
    }

    private fun bottomNavigation(): LinearLayout {
        val nav = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; setBackgroundColor(Color.WHITE); setPadding(dp(5), dp(7), dp(5), dp(7)); elevation = dp(8).toFloat() }
        val items = listOf(Triple("⌂", "Home", 0), Triple("◈", "Blockers", 1), Triple("▥", "Insights", 2), Triple("⚙", "Settings", 3))
        items.forEach { item ->
            val selected = selectedTab == item.third
            val cell = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(dp(3), dp(4), dp(3), dp(3)); background = if (selected) rounded(Color.rgb(239,241,255), 13) else null }
            cell.addView(label(item.first, 21f, if (selected) purple else muted, true).apply { gravity = Gravity.CENTER })
            cell.addView(label(item.second, 10f, if (selected) purple else muted, selected).apply { gravity = Gravity.CENTER; setPadding(0, dp(2), 0, 0) })
            cell.setOnClickListener { if (selectedTab != item.third) { selectedTab = item.third; render() } }
            nav.addView(cell, LinearLayout.LayoutParams(0, dp(49), 1f))
        }
        return nav
    }
}
