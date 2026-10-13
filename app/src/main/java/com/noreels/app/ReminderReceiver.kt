package com.noreels.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import android.app.AlarmManager
import android.app.PendingIntent
import java.util.Calendar

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val prefs = context.getSharedPreferences("noreels", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("reminder_enabled", false)) return
        try {
            val saved = prefs.getString("reminder_tone", "")
            val uri = if (saved.isNullOrBlank()) RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) else Uri.parse(saved)
            val ringtone = RingtoneManager.getRingtone(context, uri)
            ringtone?.play()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                manager.defaultVibrator.vibrate(VibrationEffect.createOneShot(550, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) vibrator.vibrate(VibrationEffect.createOneShot(550, VibrationEffect.DEFAULT_AMPLITUDE)) else @Suppress("DEPRECATION") vibrator.vibrate(550)
            }
            Toast.makeText(context, "FOCUS reminder: take a moment to refocus", Toast.LENGTH_LONG).show()
        } catch (_: Exception) { }
        // Reschedule for the next day.
        val hour = prefs.getInt("reminder_hour", 20)
        val minute = prefs.getInt("reminder_minute", 0)
        val next = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0); add(Calendar.DAY_OF_YEAR, 1) }
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pending = PendingIntent.getBroadcast(context, 411, Intent(context, ReminderReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.timeInMillis, pending) else alarm.set(AlarmManager.RTC_WAKEUP, next.timeInMillis, pending)
    }
}
