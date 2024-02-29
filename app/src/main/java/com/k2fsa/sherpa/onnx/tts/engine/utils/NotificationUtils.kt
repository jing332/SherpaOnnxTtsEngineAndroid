package com.k2fsa.sherpa.onnx.tts.engine.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.k2fsa.sherpa.onnx.tts.engine.R
import com.k2fsa.sherpa.onnx.tts.engine.app
import java.util.concurrent.atomic.AtomicLong


val pendingIntentFlags =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT else 0

val notificationManager
    get() = app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

@Suppress("DEPRECATION")
object NotificationUtils {
    const val UNSPECIFIED_ID = -1

    private val mAtomLong = AtomicLong(0)

    @Synchronized
    fun nextNotificationId(): Int = mAtomLong.incrementAndGet().toInt()

    fun Context.notificationBuilder(channelId: String): Notification.Builder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId)
        } else
            Notification.Builder(this)

    @RequiresApi(Build.VERSION_CODES.O)
    fun createChannel(
        id: String, name: String, importance: Int = NotificationManager.IMPORTANCE_DEFAULT
    ) {
        val chan = NotificationChannel(id, name, importance)
        chan.lightColor = android.graphics.Color.CYAN
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        notificationManager.createNotificationChannel(chan)
    }

    fun Context.sendNotification(
        notificationId: Int = nextNotificationId(),
        channelId: String,
        title: String,
        content: String = "",
    ) {
        notificationManager.notify(
            notificationId,
            notificationBuilder(channelId).apply {
                setContentTitle(title)
                setContentText(content)
                setSmallIcon(R.mipmap.ic_launcher)
                setAutoCancel(true)
            }.build()
        )
    }
}