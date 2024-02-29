package com.k2fsa.sherpa.onnx.tts.engine.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.drake.net.Get
import com.drake.net.component.Progress
import com.drake.net.interfaces.ProgressListener
import com.k2fsa.sherpa.onnx.tts.engine.NotificationConst
import com.k2fsa.sherpa.onnx.tts.engine.R
import com.k2fsa.sherpa.onnx.tts.engine.app
import com.k2fsa.sherpa.onnx.tts.engine.utils.NotificationUtils
import com.k2fsa.sherpa.onnx.tts.engine.utils.NotificationUtils.notificationBuilder
import com.k2fsa.sherpa.onnx.tts.engine.utils.ThrottleUtil
import com.k2fsa.sherpa.onnx.tts.engine.utils.notificationManager
import com.k2fsa.sherpa.onnx.tts.engine.utils.pendingIntentFlags
import com.k2fsa.sherpa.onnx.tts.engine.utils.startForegroundCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.File

class DownloadModelService : Service() {
    companion object {
        const val TAG = "DownloadModelService"

        val downloadDir = app.externalCacheDir!!.resolve("download")

        const val ACTION_NOTIFICATION_CANCEL =
            "com.k2fsa.sherpa.onnx.tts.engine.service.DownloadModelService.ACTION_NOTIFICATION_CANCEL"

        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val EXTRA_FILE_NAME = "file_name"
    }

    override fun onBind(intent: Intent): IBinder? = null

    private var mNotificationId = NotificationUtils.nextNotificationId()
    private val mNotificationReceiver by lazy { NotificationReceiver() }

    inner class NotificationReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_NOTIFICATION_CANCEL) {
                if (mNotificationId == intent.getIntExtra(
                        EXTRA_NOTIFICATION_ID,
                        NotificationUtils.UNSPECIFIED_ID
                    )
                ) {
                    stopSelf()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtils.createChannel(
                NotificationConst.DOWNLOAD_MODEL_CHANNEL_ID,
                getString(R.string.download_model),
            )
        }

        ContextCompat.registerReceiver(
            this,
            mNotificationReceiver,
            IntentFilter(NotificationConst.DOWNLOAD_MODEL_CHANNEL_ID),
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    @Suppress("DEPRECATION")
    override fun onDestroy() {
        mNotificationId = NotificationUtils.UNSPECIFIED_ID
        stopForeground(true)
        mScope.cancel()
        unregisterReceiver(mNotificationReceiver)

        super.onDestroy()
    }

    @Suppress("DEPRECATION")
    private fun createNotification(
        progress: Int,
        title: String,
        content: String
    ): Notification {
        return notificationBuilder(NotificationConst.DOWNLOAD_MODEL_CHANNEL_ID).apply {
            setContentTitle(title)
            setContentText(content)
            setSmallIcon(R.mipmap.ic_launcher)
            setAutoCancel(true)
            setProgress(100, progress, false)

            val cancelPending = PendingIntent.getBroadcast(
                /* context = */ this@DownloadModelService,
                /* requestCode = */ 0,
                /* intent = */ Intent(ImportModelPackageService.NOTIFICATION_ACTION_CANCEL).apply {
                    putExtra(ImportModelPackageService.EXTRA_NOTIFICATION_ID, mNotificationId)
                },
                /* flags = */ pendingIntentFlags
            )
            addAction(
                Notification.Action.Builder(
                    0,
                    getString(android.R.string.cancel),
                    cancelPending
                ).build()
            )
        }.build()
    }

    private val throttleUtil = ThrottleUtil(time = 500L)
    private fun updateNotification(progress: Int, title: String, content: String) {
        throttleUtil.runAction {
            if (mNotificationId != NotificationUtils.UNSPECIFIED_ID)
                notificationManager.notify(
                    mNotificationId,
                    createNotification(progress, title, content)
                )
        }
    }


    private val mScope = CoroutineScope(Dispatchers.IO)

    private var mUrl = ""
    private var mFileName = ""

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        mUrl = intent.data?.toString() ?: run {
            Log.e(TAG, "onStartCommand: url is null")
            stopSelf()
            return super.onStartCommand(intent, flags, startId)
        }
        mFileName = intent.getStringExtra(EXTRA_FILE_NAME) ?: kotlin.run {
            Log.e(TAG, "onStartCommand: fileName is null")
            stopSelf()
            return super.onStartCommand(intent, flags, startId)
        }

        startForegroundCompat(
            mNotificationId,
            createNotification(0, getString(R.string.downloading), mFileName)
        )

        mScope.launch {
            val file = download(mUrl, mFileName) {
                Log.d(TAG, "download progress: ${it.progress()}")
                val content =
                    "${it.progress()}% \t [${it.currentSize()}/${it.totalSize()}]"
                updateNotification(it.progress(), mFileName, content)
            }

            startService(
                Intent(
                    this@DownloadModelService,
                    ImportModelPackageService::class.java
                ).apply {
                    data = file.toUri()
                }
            )

            stopSelf()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private suspend fun download(
        url: String,
        filename: String,
        onProgress: (Progress) -> Unit
    ): File = coroutineScope {
        Log.d(TAG, "download: $url, $filename")

        downloadDir.mkdirs()
        val file = Get<File>(url) {
            if (filename.isNotBlank())
                setDownloadFileName(filename)
            setDownloadDir(downloadDir)
            addDownloadListener(object : ProgressListener() {
                override fun onProgress(p: Progress) {
                    onProgress(p)
                }
            })
        }.await()

        return@coroutineScope file
    }
}