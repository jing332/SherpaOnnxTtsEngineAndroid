package com.k2fsa.sherpa.onnx.tts.engine.synthesizer

import com.drake.net.Get
import com.drake.net.component.Progress
import com.drake.net.exception.ResponseException
import com.drake.net.interfaces.ProgressListener
import com.k2fsa.sherpa.onnx.tts.engine.AppConst
import com.k2fsa.sherpa.onnx.tts.engine.FileConst
import com.k2fsa.sherpa.onnx.tts.engine.GithubRelease
import com.k2fsa.sherpa.onnx.tts.engine.utils.CompressUtils
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import okhttp3.Response
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.InputStream
import java.util.UUID

object ModelPackageInstaller {
    init {
        runCatching {
            clearCache()
        }
    }

    private fun clearCache() {
        FileUtils.deleteDirectory(File(FileConst.cacheModelDir))
        FileUtils.deleteDirectory(File(FileConst.cacheDownloadDir))
    }


    @OptIn(ExperimentalSerializationApi::class)
    suspend fun getTtsModels(): List<GithubRelease.Asset> = coroutineScope {
        val resp = Get<Response>("").await()

        val body = resp.body
        return@coroutineScope if (resp.isSuccessful && body != null)
            AppConst.jsonBuilder.decodeFromStream(body.byteStream())
        else {
            throw ResponseException(resp, "Failed to get tts models, ${resp.code} ${resp.message}")
        }
    }

    /**
     * Unzip package to [FileConst.cacheModelDir]/[subDir]`
     *
     * [subDir] default to UUID
     */
    suspend fun extractModelPackage(
        ins: InputStream,
        progressListener: CompressUtils.ProgressListener,
        subDir: String = UUID.randomUUID().toString(),
    ): String {
        val target = FileConst.cacheModelDir + File.separator + subDir
        CompressUtils.uncompressTarBzip2(
            ins = ins,
            outputDir = target,
            progressListener = progressListener
        )

        return target
    }

    /**
     * Install model package from local directory
     *
     * [source] example: [FileConst.cacheModelDir]/$uuid`
     */
    fun installModelPackageFromDir(source: File): Boolean {
        val dir = source.listFiles { file, _ ->
            file.isDirectory
        }?.getOrNull(0) ?: return false
        val model = ConfigModelManager.analyzeToModel(dir) ?: return false

        FileUtils.copyDirectory(source, File(FileConst.modelDir))
        ConfigModelManager.addModel(model)

        return true
    }

    /**
     * Auto extract and install model package from input stream
     *
     * [ins] *.tar.bz2 input stream
     */
    suspend fun installPackage(
        ins: InputStream,
        onUnzipProgress: (file: String, total: Long, current: Long) -> Unit,
        onStartMoveFiles: () -> Unit
    ): Boolean {
        val target = extractModelPackage(ins, progressListener = { name, entrySize, bytes ->
            onUnzipProgress(name, entrySize, bytes)
        })

        onStartMoveFiles()
        return installModelPackageFromDir(File(target))
    }

    /**
     * Download model package from url and install
     */
    suspend fun installPackageFromUrl(
        url: String,
        fileName: String,
        onDownloadProgress: (Progress) -> Unit,
        onUnzipProgress: (file: String, total: Long, current: Long) -> Unit,
        onStartMoveFiles: () -> Unit
    ): Boolean {
        val file = downloadModelPackage(url, fileName) {
            onDownloadProgress(it)
        }

        return file.inputStream().use {
            installPackage(it, onUnzipProgress, onStartMoveFiles)
        }
    }

    private suspend fun downloadModelPackage(
        url: String,
        fileName: String,
        onProgress: (Progress) -> Unit
    ): File = coroutineScope {
        val downloadDir = File(FileConst.cacheDownloadDir)
        downloadDir.mkdirs()
        val file = Get<File>(url) {
            if (fileName.isNotBlank())
                setDownloadFileName(fileName)
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