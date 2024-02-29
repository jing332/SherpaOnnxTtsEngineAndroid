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

    private suspend fun ArchiveInputStream<*>.uncompress(
        outputDir: String,
        onProgress: (name: String) -> Unit,
        onEntryProgress: (name: String, entrySize: Long, bytes: Long) -> Unit
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

                    onProgress(entry.name)
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
                            onEntryProgress(entry.name, entry.size, bytes)
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
        onProgress: (name: String) -> Unit,
        onEntryProgress: (name: String, entrySize: Long, bytes: Long) -> Unit
    ) {
        TarArchiveInputStream(BZip2CompressorInputStream(ins)).use { tarIn ->
            tarIn.uncompress(outputDir, onProgress, onEntryProgress)
        }
    }

    suspend fun uncompressZip(
        ins: InputStream,
        outputDir: String,
        onProgress: (name: String) -> Unit,
        onEntryProgress: (name: String, entrySize: Long, bytes: Long) -> Unit
    ) {
        ZipArchiveInputStream(ins).use { zipIn ->
            zipIn.uncompress(outputDir, onProgress, onEntryProgress)
        }
    }

    private fun createFile(outputDir: String, name: String): File {
        return File(outputDir + File.separator + name)
    }
}