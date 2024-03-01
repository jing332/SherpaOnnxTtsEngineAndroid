package com.k2fsa.sherpa.onnx.tts.engine.utils

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.ArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import java.io.File
import java.io.InputStream
import kotlin.coroutines.coroutineContext


object CompressUtils {
    const val TAG = "CompressUtils"

    fun interface ProgressListener {
        fun onEntryProgress(name: String, entrySize: Long, bytes: Long)
    }

    private suspend fun ArchiveInputStream<*>.uncompress(
        outputDir: String,
        progressListener: ProgressListener
    ) {
        createFile(outputDir, "").mkdirs()
        var totalBytes = 0L

        var entry: ArchiveEntry
        try {
            while (nextEntry.also { entry = it } != null) {
                totalBytes += entry.size
                val file = createFile(outputDir, entry.name)
                if (entry.isDirectory) {
                    file.mkdirs()
                } else {
                    if (file.exists()) {
                        file.delete()
                    } else {
                        withContext(Dispatchers.IO) { file.createNewFile() }
                    }

                    file.outputStream().use { out ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var bytes = 0L
                        var len = 0
                        while (read(buffer).also { len = it } != -1) {
                            if (!coroutineContext.isActive) {
                                throw CancellationException()
                            }

                            out.write(buffer, 0, len)
                            bytes += len
                            progressListener.onEntryProgress(entry.name, entry.size, bytes)
                        }
                    }
                }
            }
        } catch (_: NullPointerException) {
        }
    }

    suspend fun uncompressTarBzip2(
        ins: InputStream,
        outputDir: String,
        progressListener: ProgressListener
    ) {
        TarArchiveInputStream(BZip2CompressorInputStream(ins)).use { tarIn ->
            tarIn.uncompress(outputDir, progressListener)
        }
    }

    suspend fun uncompressZip(
        ins: InputStream,
        outputDir: String,
        progressListener: ProgressListener
    ) {
        ZipArchiveInputStream(ins).use { zipIn ->
            zipIn.uncompress(outputDir, progressListener)
        }
    }

    private fun createFile(outputDir: String, name: String): File {
        return File(outputDir + File.separator + name)
    }
}