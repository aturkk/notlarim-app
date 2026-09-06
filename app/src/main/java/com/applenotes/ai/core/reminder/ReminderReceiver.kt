package com.applenotes.ai.core.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.applenotes.ai.MainActivity
import com.applenotes.ai.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_COMPLETE_REMINDER = "com.applenotes.ai.ACTION_COMPLETE_REMINDER"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val noteId = intent.getLongExtra("note_id", 0L)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (intent.action == ACTION_COMPLETE_REMINDER) {
            notificationManager.cancel(noteId.toInt())
            if (noteId > 0) {
                CoroutineScope(Dispatchers.IO).launch {
                    val db = AppDatabase.getInstance(context)
                    db.noteDao.updateReminderTime(noteId, null)
                }
            }
            Toast.makeText(context, "Hatırlatıcı tamamlandı olarak işaretlendi", Toast.LENGTH_SHORT).show()
            return
        }

        val noteTitle = intent.getStringExtra("note_title") ?: "Not Hatırlatıcısı"
        val noteSnippet = intent.getStringExtra("note_snippet") ?: "Notunuz için hatırlatıcı zamanı geldi."
        val channelId = "apple_notes_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Not Hatırlatıcıları",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notlar için ayarlanan akıllı hatırlatıcı bildirimleri"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("note_id", noteId)
        }

        val openPendingIntent = PendingIntent.getActivity(
            context,
            noteId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val completeIntent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_COMPLETE_REMINDER
            putExtra("note_id", noteId)
        }

        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            (noteId + 100000).toInt(),
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(noteTitle)
            .setContentText(noteSnippet)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .addAction(android.R.drawable.ic_menu_view, "Aç", openPendingIntent)
            .addAction(android.R.drawable.checkbox_on_background, "Tamamlandı", completePendingIntent)
            .build()

        notificationManager.notify(noteId.toInt(), notification)
    }
}