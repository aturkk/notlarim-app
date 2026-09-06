package com.applenotes.ai.core.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.applenotes.ai.MainActivity
import com.applenotes.ai.R

class QuickNotesWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        const val ACTION_NEW_NOTE = "com.applenotes.ai.ACTION_NEW_NOTE"
        const val ACTION_VOICE_NOTE = "com.applenotes.ai.ACTION_VOICE_NOTE"

        fun notifyDataChanged(context: Context) {
            val intent = Intent(context, QuickNotesWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                val widgetManager = AppWidgetManager.getInstance(context)
                val ids = widgetManager.getAppWidgetIds(
                    android.content.ComponentName(context, QuickNotesWidgetProvider::class.java)
                )
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            context.sendBroadcast(intent)
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_quick_notes)

            // New Note Action
            val newNoteIntent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_NEW_NOTE
                putExtra("trigger_time", System.currentTimeMillis())
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val newNotePendingIntent = PendingIntent.getActivity(
                context,
                101,
                newNoteIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_new_note, newNotePendingIntent)

            // Voice Note Action
            val voiceNoteIntent = Intent(context, MainActivity::class.java).apply {
                action = ACTION_VOICE_NOTE
                putExtra("trigger_time", System.currentTimeMillis())
                putExtra("auto_record_audio", true)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val voiceNotePendingIntent = PendingIntent.getActivity(
                context,
                102,
                voiceNoteIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_voice_note, voiceNotePendingIntent)

            // All Notes Action
            val allNotesIntent = Intent(context, MainActivity::class.java).apply {
                putExtra("trigger_time", System.currentTimeMillis())
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val allNotesPendingIntent = PendingIntent.getActivity(
                context,
                103,
                allNotesIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_all_notes, allNotesPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
