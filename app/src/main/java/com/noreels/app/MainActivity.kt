package com.noreels.app

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.content.Context
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
import java.util.GregorianCalendar

class MainActivity : Activity() {
    private val bg = Color.rgb(10, 12, 18)
    private val surface = Color.rgb(21, 25, 35)
    private val surfaceAlt = Color.rgb(29, 35, 48)
    private val border = Color.rgb(43, 50, 67)
    private val ink = Color.rgb(242, 245, 252)
    private val muted = Color.rgb(160, 170, 190)
    private val purple = Color.rgb(43, 139, 255)
    private val green = Color.rgb(52, 211, 153)
    private val prefs by lazy { getSharedPreferences("noreels", MODE_PRIVATE) }
    private var selectedTab = 0
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private val tonePicker = 7402

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == tonePicker && resultCode == RESULT_OK) {
            val uri = data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            prefs.edit().putString("reminder_tone", uri?.toString() ?: "").apply()
            render()
        }
    }

    private fun scheduleDailyReminder(hour: Int, minute: Int) {
        prefs.edit().putInt("reminder_hour", hour).putInt("reminder_minute", minute).putBoolean("reminder_enabled", true).apply()
        val alarm = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderReceiver::class.java)
        val pending = PendingIntent.getBroadcast(this, 411, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val next = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0); if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.timeInMillis, pending) else alarm.set(AlarmManager.RTC_WAKEUP, next.timeInMillis, pending)
    }

    private fun cancelDailyReminder() {
        prefs.edit().putBoolean("reminder_enabled", false).apply()
        val intent = Intent(this, ReminderReceiver::class.java)
        val pending = PendingIntent.getBroadcast(this, 411, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        (getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pending)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = surface
        window.decorView.systemUiVisibility = 0
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
        background = rounded(surface, 21, border); elevation = dp(1).toFloat()
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
        val logo = ImageView(this).apply {
            setImageResource(R.drawable.focus_logo)
            scaleType = ImageView.ScaleType.CENTER_CROP
            contentDescription = "FOCUS logo"
            background = rounded(surfaceAlt, 12)
            clipToOutline = true
        }
        row.addView(logo, LinearLayout.LayoutParams(dp(52), dp(52)))
        val texts = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(12), 0, 0, 0); addView(label("FOCUS", 21f, ink, true)); addView(label(subtitle, 10f, muted)) }
        row.addView(texts, LinearLayout.LayoutParams(0, -2, 1f))
        val active = isBlockerEnabled()
        val pill = label(if (active) "● ACTIVE" else "○ SETUP", 10f, if (active) green else purple, true).apply {
            setPadding(dp(10), dp(8), dp(10), dp(8)); background = rounded(if (active) Color.rgb(226, 248, 240) else Color.rgb(24, 48, 78), 30)
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
        gap(page, 10)
        page.addView(optionCard("▣", "Choose apps to block", "Select any installed app to block completely", purple) { selectedTab = 1; render() })
        gap(page, 18)
        val setup = card()
        setup.addView(label(if (active) "You're all set" else "Finish setting up FOCUS", 17f, ink, true)); gap(setup, 5)
        setup.addView(label(if (active) "Your accessibility service is enabled. You can manage apps and settings anytime." else "Android requires Accessibility permission so FOCUS can detect supported short-video screens.", 13f, muted))
        gap(setup, 12)
        setup.addView(actionButton(if (active) "Review accessibility settings" else "Enable blocker  →") { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }, LinearLayout.LayoutParams(-1, dp(48)))
        page.addView(setup); gap(page, 20)
        heading(page, "Explore FOCUS")
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
        heading(page, "BLOCK ENTIRE APPS")
        page.addView(label("Choose installed apps you want FOCUS to block completely. When you open a selected app, FOCUS will send you back. YouTube and Instagram below are controlled by their Shorts/Reels options.", 13f, muted)); gap(page, 12)
        page.addView(installedAppsPicker()); gap(page, 18)
        val permission = card()
        permission.addView(label("Accessibility permission", 17f, ink, true)); gap(permission, 6)
        permission.addView(label(if (isBlockerEnabled()) "Enabled — FOCUS can monitor supported screens." else "Not enabled — the blockers will not work until you enable the service.", 13f, muted)); gap(permission, 12)
        permission.addView(actionButton("Open accessibility settings") { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }, LinearLayout.LayoutParams(-1, dp(48)))
        page.addView(permission); gap(page, 16)
        page.addView(label("Note: YouTube and Instagram can change their screen identifiers in updates, which may affect detection.", 12f, muted))
    }

    private fun installedAppsPicker(): LinearLayout {
        val outer = card()
        val query = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = try {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(query, PackageManager.MATCH_ALL)
        } catch (_: Exception) { emptyList() }
        val unique = apps.mapNotNull { info ->
            val pkg = info.activityInfo?.packageName ?: return@mapNotNull null
            if (pkg == packageName || pkg == "com.google.android.youtube" || pkg == "com.instagram.android") return@mapNotNull null
            val title = info.loadLabel(packageManager)?.toString()?.trim().orEmpty()
            if (title.isBlank()) null else pkg to title
        }.distinctBy { it.first }.sortedBy { it.second.lowercase(Locale.getDefault()) }
        val selected = prefs.getStringSet("blocked_apps", emptySet())?.toMutableSet() ?: mutableSetOf()
        if (unique.isEmpty()) {
            outer.addView(label("No launchable apps found. Try reopening FOCUS.", 13f, muted))
            return outer
        }
        outer.addView(label("Select apps", 16f, ink, true)); gap(outer, 4)
        outer.addView(label("${selected.size} app(s) selected", 12f, muted)); gap(outer, 8)
        unique.forEachIndexed { index, item ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(0, dp(7), 0, dp(7)) }
            val appIcon = ImageView(this).apply {
                try { setImageDrawable(packageManager.getApplicationIcon(item.first)) } catch (_: Exception) { }
                scaleType = ImageView.ScaleType.FIT_CENTER
            }
            row.addView(appIcon, LinearLayout.LayoutParams(dp(38), dp(38)))
            val appName = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(10), 0, dp(5), 0) }
            appName.addView(label(item.second, 14f, ink, true))
            appName.addView(label(item.first, 10f, muted))
            row.addView(appName, LinearLayout.LayoutParams(0, -2, 1f))
            val toggle = Switch(this).apply {
                isChecked = selected.contains(item.first)
                contentDescription = "Block ${item.second}"
                setOnCheckedChangeListener { _, checked ->
                    val current = prefs.getStringSet("blocked_apps", emptySet())?.toMutableSet() ?: mutableSetOf()
                    if (checked) current.add(item.first) else current.remove(item.first)
                    prefs.edit().putStringSet("blocked_apps", current).apply()
                }
            }
            row.addView(toggle)
            outer.addView(row)
            if (index < unique.lastIndex) outer.addView(View(this).apply { setBackgroundColor(border) }, LinearLayout.LayoutParams(-1, dp(1)))
        }
        return outer
    }

    private fun insightsPage(page: LinearLayout) {
        brandHeader(page, "YOUR PROGRESS, ON THIS DEVICE")
        page.addView(label("Focus insights", 27f, ink, true)); gap(page, 6)
        page.addView(label("See how often FOCUS has interrupted short-video scrolling.", 14f, muted)); gap(page, 22)
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
        val progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply { max = goal; this.progress = today.coerceAtMost(goal); progressTintList = android.content.res.ColorStateList.valueOf(purple); progressBackgroundTintList = android.content.res.ColorStateList.valueOf(surfaceAlt) }
        goalCard.addView(progress, LinearLayout.LayoutParams(-1, dp(8))); gap(goalCard, 8)
        goalCard.addView(label("${today.coerceAtMost(goal)} of $goal completed${if (today >= goal) " — goal reached!" else ""}", 12f, if (today >= goal) green else muted)); gap(goalCard, 12)
        val controls = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        controls.addView(actionButton("− 5") { prefs.edit().putInt("daily_goal", (goal - 5).coerceAtLeast(1)).apply(); render() }, LinearLayout.LayoutParams(0, dp(44), 1f))
        controls.addView(View(this), LinearLayout.LayoutParams(dp(8), 1))
        controls.addView(actionButton("+ 5") { prefs.edit().putInt("daily_goal", (goal + 5).coerceAtMost(100)).apply(); render() }, LinearLayout.LayoutParams(0, dp(44), 1f))
        goalCard.addView(controls); page.addView(goalCard); gap(page, 14)
        gap(page, 18)
        heading(page, "CALENDAR & REMINDERS")
        val calCard = card()
        val monthCal = Calendar.getInstance()
        calCard.addView(label(SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(monthCal.time), 17f, ink, true)); gap(calCard, 10)
        val weekdays = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        listOf("M", "T", "W", "T", "F", "S", "S").forEach { d -> weekdays.addView(label(d, 11f, muted, true).apply { gravity = Gravity.CENTER }, LinearLayout.LayoutParams(0, dp(28), 1f)) }
        calCard.addView(weekdays)
        val month = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }
        val firstOffset = (month.get(Calendar.DAY_OF_WEEK) + 5) % 7
        val daysInMonth = month.getActualMaximum(Calendar.DAY_OF_MONTH)
        var day = 1
        repeat(6) { week ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            repeat(7) { col ->
                val cellDay = week * 7 + col - firstOffset + 1
                val cell = TextView(this).apply {
                    gravity = Gravity.CENTER; textSize = 12f; setTextColor(if (cellDay == Calendar.getInstance().get(Calendar.DAY_OF_MONTH) && month.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH)) Color.WHITE else muted)
                    text = if (cellDay in 1..daysInMonth) cellDay.toString() else ""
                    if (cellDay in 1..daysInMonth && cellDay == Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) background = rounded(purple, 12)
                    if (cellDay in 1..daysInMonth) setOnClickListener { prefs.edit().putInt("calendar_selected_day", cellDay).putInt("calendar_selected_month", month.get(Calendar.MONTH)).putInt("calendar_selected_year", month.get(Calendar.YEAR)).apply(); render() }
                }
                row.addView(cell, LinearLayout.LayoutParams(0, dp(37), 1f))
            }
            calCard.addView(row)
            if (week * 7 + 7 - firstOffset >= daysInMonth) return@repeat
        }
        gap(calCard, 8)
        val chosenDay = prefs.getInt("calendar_selected_day", Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
        calCard.addView(label("Selected date: $chosenDay • Daily goal: ${prefs.getInt("daily_goal", 10)} blocks", 12f, muted)); gap(calCard, 8)
        calCard.addView(label("Tap a date to select it. Your daily goal is adjustable above.", 11f, muted))
        page.addView(calCard); gap(page, 12)

        val reminderCard = card()
        val reminderEnabled = prefs.getBoolean("reminder_enabled", false)
        val hour = prefs.getInt("reminder_hour", 20)
        val minute = prefs.getInt("reminder_minute", 0)
        reminderCard.addView(label("Daily focus reminder", 17f, ink, true)); gap(reminderCard, 5)
        reminderCard.addView(label(if (reminderEnabled) "Reminder set for %02d:%02d".format(hour, minute) else "Choose a time to get a gentle reminder to refocus.", 13f, muted)); gap(reminderCard, 12)
        reminderCard.addView(actionButton(if (reminderEnabled) "Change reminder time" else "Set reminder time") {
            TimePickerDialog(this, { _, h, m -> scheduleDailyReminder(h, m); render() }, hour, minute, true).show()
        }, LinearLayout.LayoutParams(-1, dp(46))); gap(reminderCard, 8)
        reminderCard.addView(actionButton("Choose reminder tune") {
            val pick = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Choose FOCUS reminder tune")
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                val saved = prefs.getString("reminder_tone", "")
                if (!saved.isNullOrBlank()) putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(saved))
            }
            startActivityForResult(pick, tonePicker)
        }, LinearLayout.LayoutParams(-1, dp(46))); gap(reminderCard, 8)
        reminderCard.addView(actionButton("Turn reminder off") { cancelDailyReminder(); render() }, LinearLayout.LayoutParams(-1, dp(46)))
        page.addView(reminderCard); gap(page, 14)
        page.addView(label("These are detected blocking events, not total screen time or verified minutes saved. Data stays on this device.", 12f, muted))
    }

    private fun settingsPage(page: LinearLayout) {
        brandHeader(page, "MAKE FOCUS WORK FOR YOU")
        page.addView(label("Settings", 27f, ink, true)); gap(page, 6)
        page.addView(label("Manage permissions and learn about the app.", 14f, muted)); gap(page, 20)
        page.addView(optionCard("♧", "Accessibility permission", if (isBlockerEnabled()) "Enabled — tap to review" else "Required for blocking to work", purple) { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }); gap(page, 10)
        page.addView(optionCard("▦", "Manage blockers", "Choose which apps FOCUS blocks", green) { selectedTab = 1; render() }); gap(page, 10)
        page.addView(optionCard("◷", "Focus insights", "View daily counts and your goal", purple) { selectedTab = 2; render() }); gap(page, 20)
        page.addView(label("Privacy: blocking counts and preferences are stored locally on this device. Accessibility access is used to detect supported screens.", 12f, muted))
    }

    private fun statTile(value: String, caption: String, accent: Int): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL; setPadding(dp(15), dp(17), dp(12), dp(16)); background = rounded(surface, 20, border)
        addView(label(value, 26f, accent, true)); addView(label(caption, 10f, muted, true).apply { setPadding(0, dp(5), 0, 0) })
    }

    private fun actionButton(textValue: String, action: () -> Unit): Button = Button(this).apply {
        text = textValue; textSize = 13f; isAllCaps = false; setTextColor(Color.WHITE); background = rounded(purple, 14); setPadding(dp(8), dp(3), dp(8), dp(3)); setOnClickListener { action() }
    }

    private fun optionCard(symbol: String, title: String, subtitle: String, accent: Int, action: () -> Unit): LinearLayout {
        val outer = card(); outer.orientation = LinearLayout.HORIZONTAL; outer.gravity = Gravity.CENTER_VERTICAL
        val icon = TextView(this).apply { text = symbol; textSize = 23f; gravity = Gravity.CENTER; setTextColor(accent); background = rounded(surfaceAlt, 14) }
        outer.addView(icon, LinearLayout.LayoutParams(dp(48), dp(48)))
        val texts = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(12), 0, dp(6), 0); addView(label(title, 15f, ink, true)); addView(label(subtitle, 12f, muted).apply { setPadding(0, dp(4), 0, 0) }) }
        outer.addView(texts, LinearLayout.LayoutParams(0, -2, 1f)); outer.addView(label("›", 26f, muted)); outer.setOnClickListener { action() }; return outer
    }

    private fun platformCard(symbol: String, title: String, subtitle: String, key: String, accent: Int): LinearLayout {
        val outer = card(); outer.orientation = LinearLayout.HORIZONTAL; outer.gravity = Gravity.CENTER_VERTICAL
        val appIcon = TextView(this).apply { text = symbol; textSize = 24f; gravity = Gravity.CENTER; setTextColor(accent); background = rounded(surfaceAlt, 15) }
        outer.addView(appIcon, LinearLayout.LayoutParams(dp(49), dp(49)))
        val texts = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(12), 0, dp(5), 0); addView(label(title, 15f, ink, true)); addView(label(subtitle, 12f, muted).apply { setPadding(0, dp(4), 0, 0) }) }
        outer.addView(texts, LinearLayout.LayoutParams(0, -2, 1f))
        val toggle = Switch(this).apply { text = ""; isChecked = prefs.getBoolean(key, true); contentDescription = "Block $title"; setOnCheckedChangeListener { _, checked -> prefs.edit().putBoolean(key, checked).apply() } }
        outer.addView(toggle); return outer
    }

    private fun bottomNavigation(): LinearLayout {
        val nav = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; setBackgroundColor(surface); setPadding(dp(5), dp(7), dp(5), dp(7)); elevation = dp(8).toFloat() }
        val items = listOf(Triple("⌂", "Home", 0), Triple("◈", "Blockers", 1), Triple("▥", "Insights", 2), Triple("⚙", "Settings", 3))
        items.forEach { item ->
            val selected = selectedTab == item.third
            val cell = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(dp(3), dp(4), dp(3), dp(3)); background = if (selected) rounded(surfaceAlt, 13) else null }
            cell.addView(label(item.first, 21f, if (selected) purple else muted, true).apply { gravity = Gravity.CENTER })
            cell.addView(label(item.second, 10f, if (selected) purple else muted, selected).apply { gravity = Gravity.CENTER; setPadding(0, dp(2), 0, 0) })
            cell.setOnClickListener { if (selectedTab != item.third) { selectedTab = item.third; render() } }
            nav.addView(cell, LinearLayout.LayoutParams(0, dp(49), 1f))
        }
        return nav
    }
}
