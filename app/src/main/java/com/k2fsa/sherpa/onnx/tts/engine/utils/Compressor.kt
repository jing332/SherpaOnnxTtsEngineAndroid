package com.k2fsa.sherpa.onnx.tts.engine.utils

import com.k2fsa.sherpa.onnx.tts.engine.utils.CompressUtils.uncompress
import org.apache.commons.compress.archivers.ArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream
import java.io.InputStream

object CompressorFactory {
    val compressors = listOf(
        TarBzip2Compressor(),
        ZipCompressor(),
        TarGzipCompressor(),
        TarXzCompressor()
    )

    fun createCompressor(type: String): CompressorInterface? {
        for (compressor in compressors) {
            if (compressor.verifyType(type)) {
                return compressor
            }
        }
        return null
    }
}

interface CompressorInterface {
    fun verifyType(type: String): Boolean


    suspend fun uncompress(
        ins: InputStream,
        outputDir: String,
        progressListener: CompressUtils.ProgressListener
    )

//    fun compress(dir: String): OutputStream
}

abstract class ImplCompressor(open val extName: List<String>) : CompressorInterface {
    override fun verifyType(type: String): Boolean {
        return extName.any { it.lowercase() == type.lowercase() }
    }

    abstract fun archiveInputStream(ins: InputStream): ArchiveInputStream<*>

    override suspend fun uncompress(
        ins: InputStream,
        outputDir: String,
        progressListener: CompressUtils.ProgressListener
    ) {
        archiveInputStream(ins).use { arIn ->
            arIn.uncompress(outputDir, progressListener)
        }
    }
}

class TarBzip2Compressor : ImplCompressor(listOf("tar.bz2", "tbz2")) {
    override fun archiveInputStream(ins: InputStream): ArchiveInputStream<*> =
        TarArchiveInputStream(BZip2CompressorInputStream(ins))
}

class ZipCompressor : ImplCompressor(listOf("zip")) {
    override fun archiveInputStream(ins: InputStream): ArchiveInputStream<*> =
        ZipArchiveInputStream(ins)
}

class TarGzipCompressor : ImplCompressor(listOf("tar.gz", "tgz")) {
    override fun archiveInputStream(ins: InputStream): ArchiveInputStream<*> =
        TarArchiveInputStream(GzipCompressorInputStream(ins))
}

class TarXzCompressor : ImplCompressor(listOf("tar.xz", "txz")) {
    override fun archiveInputStream(ins: InputStream): ArchiveInputStream<*> =
        TarArchiveInputStream(XZCompressorInputStream(ins))
}