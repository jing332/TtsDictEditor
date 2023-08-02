package com.github.jing332.tts_dict_editor.replace

import android.content.Context
import android.net.Uri
import com.charleskorn.kaml.decodeFromStream
import com.charleskorn.kaml.encodeToStream
import com.github.jing332.tts_dict_editor.const.AppConst
import kotlinx.serialization.encodeToString
import java.io.InputStream

class ReplaceManager(val context: Context, val uri: Uri) {
    companion object {
        const val TAG = "ReplaceManager"

        fun toYaml(list: List<ReplaceRuleYaml>): String {
            return AppConst.yaml.encodeToString(ReplaceConfig(list))
        }
    }

    private val replaceList = mutableListOf<ReplaceRuleYaml>()

    fun addReplace(replaceConfig: ReplaceRuleYaml) {
        replaceList.add(replaceConfig)
    }

    fun getReplaceList(): List<ReplaceRuleYaml> {
        return replaceList
    }

    fun clearReplaceList() {
        replaceList.clear()
    }

    fun readConfigFromFile() {
        context.contentResolver.openInputStream(uri)?.use {
            readConfigFromInputStream(it)
            return
        }

        throw Exception("Failed to read config file")
    }

    private fun readConfigFromInputStream(ins: InputStream) {
        val cfg: ReplaceConfig = AppConst.yaml.decodeFromStream(ins)
        replaceList.clear()
        replaceList.addAll(cfg.replaces)
    }

    private fun updateConfigFile() {
        val cfg = ReplaceConfig(replaceList)
        context.contentResolver.openOutputStream(uri)?.use {
            AppConst.yaml.encodeToStream(cfg, it)
            return
        }

        throw Exception("Failed to write config file")
    }
}