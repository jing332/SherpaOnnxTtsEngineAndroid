package com.k2fsa.sherpa.onnx.tts.engine.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import com.k2fsa.sherpa.onnx.tts.engine.NotificationConst
import com.k2fsa.sherpa.onnx.tts.engine.R
import com.k2fsa.sherpa.onnx.tts.engine.app
import com.k2fsa.sherpa.onnx.tts.engine.synthesizer.ModelConstants
import com.k2fsa.sherpa.onnx.tts.engine.synthesizer.ModelManager
import com.k2fsa.sherpa.onnx.tts.engine.ui.MainActivity
import com.k2fsa.sherpa.onnx.tts.engine.utils.CompressUtils
import com.k2fsa.sherpa.onnx.tts.engine.utils.NotificationUtils
import com.k2fsa.sherpa.onnx.tts.engine.utils.NotificationUtils.notificationBuilder
import com.k2fsa.sherpa.onnx.tts.engine.utils.NotificationUtils.sendNotification
import com.k2fsa.sherpa.onnx.tts.engine.utils.ThrottleUtil
import com.k2fsa.sherpa.onnx.tts.engine.utils.grantReadPermission
import com.k2fsa.sherpa.onnx.tts.engine.utils.longToast
import com.k2fsa.sherpa.onnx.tts.engine.utils.notificationManager
import com.k2fsa.sherpa.onnx.tts.engine.utils.pendingIntentFlags
import com.k2fsa.sherpa.onnx.tts.engine.utils.startForegroundCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.UUID

class ImportModelPackageService : Service() {
    companion object {
        const val TAG = "ImportModelService"

        const val NOTIFICATION_ACTION_CANCEL =
            "com.k2fsa.sherpa.onnx.tts.engine.service.ImportModelPackageService.CANCEL"
        const val EXTRA_NOTIFICATION_ID = "notification_id"

        init {
            app.externalCacheDir?.resolve("model")?.deleteRecursively()
        }
    }

    private val mBinder by lazy { LocalBinder() }
    private val mScope = CoroutineScope(Dispatchers.IO + Job())
    private val mNotificationReceiver by lazy { NotificationActionReceiver() }
    private var notificationId = NotificationUtils.nextNotificationId()

    private var mFilename = ""

    override fun onBind(intent: Intent): IBinder = mBinder

    inner class LocalBinder : Binder() {
        fun getService(): ImportModelPackageService {
            return this@ImportModelPackageService
        }
    }

    inner class NotificationActionReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == NOTIFICATION_ACTION_CANCEL) {
                val id = intent.getIntExtra(EXTRA_NOTIFICATION_ID, NotificationUtils.UNSPECIFIED_ID)
                if (id == notificationId) {
                    stopSelf() // Invoke onDestroy() then mScope.cancel()
                }
            }
        }
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()

        ContextCompat.registerReceiver(
            /* context = */ this,
            /* receiver = */ mNotificationReceiver,
            /* filter = */ IntentFilter(NOTIFICATION_ACTION_CANCEL),
            /* flags = */ ContextCompat.RECEIVER_EXPORTED
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtils.createChannel(
                NotificationConst.IMPORT_MODEL_PACKAGE_CHANNEL_ID,
                getString(R.string.compress_service)
            )
        }

        Log.d(TAG, "startForegroundCompat: notificationId=$notificationId")
        startForegroundCompat(
            notificationId,
            createNotification(title = getString(R.string.import_model_package), 0, "")
        )
    }

    @Suppress("DEPRECATION")
    override fun onDestroy() {
        Log.d(TAG, "onDestroy")

        unregisterReceiver(mNotificationReceiver)
        mScope.cancel()

        stopForeground(true)
        notificationId = NotificationUtils.UNSPECIFIED_ID
        super.onDestroy()
    }


    private val throttleUtil = ThrottleUtil(time = 500L)
    private fun updateNotification(
        progress: Int = 0,
        title: String = getString(R.string.decompressing),
        content: String = ""
    ) {
        throttleUtil.runAction {
            if (notificationId != NotificationUtils.UNSPECIFIED_ID) // if foreground notification is not removed
                notificationManager.notify(
                    notificationId,
                    createNotification(title, progress, content)
                )
        }
    }

    @Suppress("DEPRECATION")
    private fun createNotification(title: String, progress: Int, text: String): Notification {
        val builder = notificationBuilder(NotificationConst.IMPORT_MODEL_PACKAGE_CHANNEL_ID)
        builder.apply {
            val pendingIntent =
                PendingIntent.getActivity(
                    this@ImportModelPackageService, 1, Intent(
                        this@ImportModelPackageService,
                        MainActivity::class.java
                    ), pendingIntentFlags
                )
            setContentIntent(pendingIntent)
            setSmallIcon(R.mipmap.ic_launcher)
            setContentTitle(title)
            setContentText(text)
            setProgress(100, progress, false)

            Log.d(TAG, "notificationId=${notificationId}")
            val cancelPending = PendingIntent.getBroadcast(
                /* context = */ this@ImportModelPackageService,
                /* requestCode = */ 0,
                /* intent = */ Intent(NOTIFICATION_ACTION_CANCEL).apply {
                    putExtra(EXTRA_NOTIFICATION_ID, notificationId)
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
        }
        return builder.build()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val uri = intent.data
        Log.d(TAG, "onStartCommand: $uri")
        if (uri == null) {
            longToast("Uri is null")
            stopSelf()
        } else {
            when (uri.scheme) {
                "content" -> {
                    uri.grantReadPermission(contentResolver)
                    DocumentFile.fromSingleUri(this, uri)!!.let {
                        mFilename = it.name!!
                    }
                }

                else -> {
                    mFilename = File(uri.path!!).name
                }
            }

            mScope.launch {
                if (execute(uri))
                    sendNotification(
                        channelId = NotificationConst.IMPORT_MODEL_PACKAGE_CHANNEL_ID,
                        title = getString(R.string.import_completed),
                        content = mFilename
                    )

                stopSelf()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }


    private suspend fun execute(uri: Uri): Boolean {
        val ins = contentResolver.openInputStream(uri)
        if (ins == null) {
            stopSelf()
        } else {
            val uuid = UUID.randomUUID().toString()
            val cacheModelDir = externalCacheDir?.resolve("model/${uuid}")!!
            ins.use {
                CompressUtils.uncompressTarBzip2(
                    ins = ins,
                    outputDir = cacheModelDir.absolutePath,
                    onProgress = { name ->
                        Log.d(TAG, "onProgress: $name")
                    },
                    onEntryProgress = { name, entrySize, bytes ->
//                        Log.d(
//                            TAG,
//                            "onEntryProgress: $name, $entrySize, $bytes, ${((bytes / entrySize.toDouble()) * 100).toFloat()} %"
//                        )

                        updateNotification(
                            progress = ((bytes / entrySize.toDouble()) * 100).toInt(),
                            content = name
                        )
                    }
                )
            }

            updateNotification(
                0,
                getString(R.string.copying),
                cacheModelDir.absolutePath
            )
            FileUtils.copyDirectory(cacheModelDir, File(ModelConstants.modelPath))
            val dir = cacheModelDir.listFiles { dir, _ ->
                dir.isDirectory
            }?.getOrNull(0) ?: return false

            ModelManager.analyzeToModel(dir)?.let {
                ModelManager.addModel(it)
                return true
            }
        }

        return false
    }

}