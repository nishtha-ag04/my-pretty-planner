package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "New Announcement! 📣"
        val message = remoteMessage.notification?.body ?: remoteMessage.data["message"] ?: ""
        val imageUrl = remoteMessage.data["imageUrl"]

        sendPushNotification(applicationContext, title, message, imageUrl)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    companion object {
        fun sendPushNotification(context: Context, title: String, message: String, imageUrl: String? = null) {
            val channelId = "broadcast_announcements_channel"
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Developer Broadcasts 📣",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Announcements & alerts from the Admin team"
                }
                notificationManager.createNotificationChannel(channel)
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("nav_to", "Announcements")
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            if (!imageUrl.isNullOrBlank()) {
                builder.setSubText("Announcement with attachment")
            }

            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }
}
