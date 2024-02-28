package com.k2fsa.sherpa.onnx.tts.engine.synthesizer

import android.content.Context
import android.util.Log
import com.k2fsa.sherpa.onnx.OfflineTtsConfig
import com.k2fsa.sherpa.onnx.OfflineTtsModelConfig
import com.k2fsa.sherpa.onnx.OfflineTtsVitsModelConfig
import com.k2fsa.sherpa.onnx.tts.engine.synthesizer.config.ConfigManager
import com.k2fsa.sherpa.onnx.tts.engine.synthesizer.config.Model
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

object ModelManager {
    const val TAG = "ModelManager"

    private val models = mutableListOf<Model>()

    fun models() = models

    private val _modelsFlow by lazy { MutableStateFlow<List<Model>>(emptyList()) }
    val modelsFlow: StateFlow<List<Model>>
        get() = _modelsFlow.asStateFlow()

    private fun notifyModelsChange() {
        Log.d(TAG, "notifyModelsChange: ${models.size}")
        _modelsFlow.value = models.toList()
    }

    @Synchronized
    fun load() {
        models.addAll(ConfigManager.config.models)
        deduplicateModels()

        notifyModelsChange()
    }

    fun languages(): List<String> {
        return models.distinctBy { it.lang }.map { it.lang }
    }

    fun defaultLanguage(): String {
        return models.firstOrNull()?.lang ?: "en"
    }

    // 去重models
    private fun deduplicateModels() {
        val list = models.distinctBy { it.id }
        Log.d(TAG, "deduplicateModels: ${list.size}")
        models.clear()
        models.addAll(list)
        ConfigManager.updateConfig(ConfigManager.config.copy(models = models))
    }

    fun removeModel(model: Model) {
        models.remove(model)
        ConfigManager.updateConfig(ConfigManager.config.copy(models = models))
        notifyModelsChange()
    }

    fun addModel(vararg model: Model) {
        models.addAll(model)
        ConfigManager.updateConfig(ConfigManager.config.copy(models = models))
        notifyModelsChange()
    }

    fun updateModel(model: Model) {
        models.indexOfFirst { it.id == model.id }.takeIf { it != -1 }?.let {
            models[it] = model.copy()
            ConfigManager.updateConfig(ConfigManager.config.copy(models = models))
            notifyModelsChange()
        }
    }

    fun getNotAddedModels(context: Context): List<Model> {
        val addedIds = models.map { it.id }
        return analyzeToModels().filter { it.id !in addedIds }
    }

    // 根据文件目录结构获取模型列表
    fun analyzeToModels(): List<Model> {
        Log.d(TAG, "modelPath: ${ModelConstants.modelPath}")
        val list = mutableListOf<Model>()
        File(ModelConstants.modelPath).listFiles()!!.forEach { dir ->
            if (dir.isDirectory) {
                Log.d(TAG, "load model: ${dir.name}")
                val onnx = dir.listFiles { _, name -> name.endsWith(".onnx") }
                    ?.run { if (isNotEmpty()) first() else null }
                    ?: return@forEach

                val dataDir = dir.resolve("espeak-ng-data").takeIf { it.exists() }

                list.add(
                    Model(
                        id = dir.name,
                        onnx = dir.name + "/" + onnx.name,
                        lexicon = if (dataDir == null) "${dir.name}/lexicon.txt" else "",
                        ruleFsts = if (dataDir == null) "${dir.name}/rule.fst" else "",
                        tokens = "${dir.name}/tokens.txt",
                        dataDir = dataDir?.run { "${dir.name}/espeak-ng-data" } ?: "",
                        lang = "eng"
                    )
                )

            }
        }

        return list
    }

    fun Model.toOfflineTtsConfig(root: String = ModelConstants.modelPath): OfflineTtsConfig {
        fun format(str: String): String {
            return if (str.isBlank()) "" else "$root/$str"
        }

        return OfflineTtsConfig(
            model = OfflineTtsModelConfig(
                vits = OfflineTtsVitsModelConfig(
                    model = format(onnx),
                    lexicon = format(lexicon),
                    tokens = format(tokens),
                    dataDir = format(dataDir),
                ),
                numThreads = 2,
                debug = true,
                provider = "cpu",
            ),
            ruleFsts = format(ruleFsts),
        )
    }

    /* fun readModelsFromDir(context: Context): List<OfflineTtsConfig> {
         val list = mutableListOf<OfflineTtsConfig>()
         context.getExternalFilesDir("model")?.listFiles()?.forEach { file ->
             if (file.isDirectory) {
                 Log.d(TAG, "load model: ${file.name}")
                 val onnx = file.listFiles { _, name -> name.endsWith(".onnx") }
                     ?.run { if (isNotEmpty()) first() else null }
                     ?: return@forEach

                 val dataDir = file.resolve("espeak-ng-data").takeIf { it.exists() }

                 list.add(
                     OfflineTtsConfig(
                         model = OfflineTtsModelConfig(
                             vits = OfflineTtsVitsModelConfig(
                                 model = onnx.absolutePath,
                                 lexicon = if (dataDir == null) "${file.absolutePath}/lexicon.txt" else "",
                                 tokens = "${file.absolutePath}/tokens.txt",
                                 dataDir = dataDir?.absolutePath ?: ""
                             ),
                             numThreads = 2,
                             debug = true,
                             provider = "cpu",
                         ),
                         ruleFsts = if (dataDir == null) "${file.absoluteFile}/rule.fst" else "",
                     )
                 )
             }
         }

         return list
     }
 */

}