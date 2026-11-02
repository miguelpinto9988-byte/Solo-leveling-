package com.gymbreaker.app.alarms

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.gymbreaker.app.MainActivity
import com.gymbreaker.app.R
import java.util.Calendar

object TrainingAlarms {
    private const val PREFS = "hunter_preferences"
    private const val CHANNEL_ID = "gymbreaker_alarms"
    private const val REQUEST_CODE = 701

    fun setDaily(context: Context, hour: Int, minute: Int) {
        require(hour in 0..23 && minute in 0..59)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt("alarm_hour", hour).putInt("alarm_minute", minute)
            .putBoolean("alarm_enabled", true).apply()
        schedule(context, hour, minute)
    }

    fun restore(context: Context) {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (p.getBoolean("alarm_enabled", false)) {
            schedule(context, p.getInt("alarm_hour", 8), p.getInt("alarm_minute", 0))
        }
    }

    fun label(context: Context): String {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!p.getBoolean("alarm_enabled", false)) return "Sin alarma configurada"
        return "Alarma diaria aproximada: %02d:%02d".format(
            p.getInt("alarm_hour", 8), p.getInt("alarm_minute", 0)
        )
    }

    fun disable(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean("alarm_enabled", false).apply()
        val manager = context.getSystemService(AlarmManager::class.java)
        manager.cancel(pendingIntent(context))
    }

    private fun pendingIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            Intent(context, AlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun schedule(context: Context, hour: Int, minute: Int) {
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        val manager = context.getSystemService(AlarmManager::class.java)
        // Alarma aproximada: no solicita el permiso restringido SCHEDULE_EXACT_ALARM.
        manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.timeInMillis, pendingIntent(context))
    }

    fun notify(context: Context) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, context.getString(R.string.channel_name), NotificationManager.IMPORTANCE_DEFAULT)
        )
        val open = PendingIntent.getActivity(
            context, 702, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_crown)
            .setContentTitle(context.getString(R.string.alarm_title))
            .setContentText(context.getString(R.string.alarm_body))
            .setAutoCancel(true)
            .setContentIntent(open)
            .build()
        manager.notify(701, notification)
    }
}

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        TrainingAlarms.notify(context)
        TrainingAlarms.restore(context)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) TrainingAlarms.restore(context)
    }
}
