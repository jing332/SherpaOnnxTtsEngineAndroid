package com.k2fsa.sherpa.onnx.tts.engine.synthesizer

import com.k2fsa.sherpa.onnx.tts.engine.conf.TtsConfig
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.DelayQueue
import java.util.concurrent.Delayed
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


interface ImplCache {
    fun destroy()
}


class SynthesizerCache {
    companion object {
        private val delayTime: Int
            get() = 1000 * 60 * TtsConfig.timeoutDestruction.value
    }

    private val delayQueue = DelayQueue<DelayedDestroyTask>()
    private val queueMap = ConcurrentHashMap<String, DelayedDestroyTask>()
    private val executor = Executors.newSingleThreadExecutor()

    private var isTaskRunning = false
    private fun ensureTaskRunning() {
        if (isTaskRunning) return

        synchronized(delayQueue) {
            isTaskRunning = true
            executor.execute {
                while (true) {
                    if (delayQueue.isEmpty()) {
                        break
                    } else {
                        val task = delayQueue.take()
                        queueMap.remove(task.id)
                        task.obj.destroy()
                    }
                }
                isTaskRunning = false
            }
        }
    }

    fun cache(id: String, obj: ImplCache) {
        val task =
            DelayedDestroyTask(delayTime = delayTime, id, obj)
        delayQueue.add(task)
        queueMap[id] = task
        ensureTaskRunning()
    }

    fun getById(id: String): ImplCache? {
        queueMap[id]?.let {
            if (it.getDelay(TimeUnit.MILLISECONDS) <= 1000 * 10) { // 小于10s便重置
                it.reset()
            }
            return it.obj
        }
        return null
    }

    class DelayedDestroyTask(private val delayTime: Int, val id: String, val obj: ImplCache) :
        Delayed {
        private var expireTime: Long = 0L

        init {
            reset()
        }

        fun reset() {
            expireTime = System.currentTimeMillis() + delayTime
        }

        override fun compareTo(other: Delayed?): Int =
            getDelay(TimeUnit.MILLISECONDS).compareTo(
                (other as DelayedDestroyTask).getDelay(TimeUnit.MILLISECONDS)
            )


        override fun getDelay(unit: TimeUnit?): Long =
            unit?.convert(expireTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS) ?: 0
    }
}
