package com.github.jing332.tts_dict_editor.help

import com.github.jing332.tts_dict_editor.help.DictFileManager.Companion.toTxt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.buffer
import okio.source
import java.io.InputStream


class DictFileManager() {
    companion object {
        const val TAG = "DictFileLoader"

        private const val NOTE_LABEL = '#'
        private const val REGEXP_START_LABEL = "r:\"^"
        private const val REGEXP_END_LABEL = "$\"="

        private val jsonBuilder by lazy {
            Json {
                ignoreUnknownKeys = true
                explicitNulls = false
            }
        }

        fun List<GroupWithReplaceRule>.toTxt(): String {
            val groupSet = this.distinctBy { it.group.id }
            val groupTxt = jsonBuilder.encodeToString(groupSet.map { it.group })

            val sb = StringBuilder()
            sb.appendLine("$NOTE_LABEL $groupTxt")
            forEach {
                sb.appendLine("$NOTE_LABEL START GROUP: ${it.group.name}")
                sb.append(encodeReplaceRule(it.list))
                sb.appendLine("$NOTE_LABEL END GROUP: ${it.group.name}")
                sb.appendLine().appendLine()
            }

            return sb.toString()
        }

        fun encodeReplaceRule(rules: List<ReplaceRule>): String {
            val sb = StringBuilder()
            rules.forEach {
                val jStr = jsonBuilder.encodeToString(
                    it.copy(
                        pattern = "",
                        replacement = "",
                        isRegex = false
                    )
                )
                sb.appendLine("# $jStr")
                if (it.isRegex)
                    sb.appendLine("$REGEXP_START_LABEL${it.pattern}$REGEXP_END_LABEL${it.replacement}")
                else
                    sb.appendLine("${it.pattern}=${it.replacement}")
                sb.appendLine()
            }
            return sb.toString()
        }
    }

    val rules = mutableListOf<ReplaceRule>()
    val groups = mutableListOf<ReplaceRuleGroup>()

    fun reset() {
        rules.clear()
        groups.clear()
    }

    /**
     * 加载后自动 close()
     */
    suspend fun load(inputStream: InputStream) {
        withContext(Dispatchers.IO) {
            inputStream.use {
                val buffered = it.source().buffer()
                while (coroutineContext.isActive) {
                    val line = buffered.readUtf8Line() ?: break
                    parseSingleLine(line.trim())
                }
                buffered.close()
            }
        }
    }

    private fun parseSingleLine(line: String) {
        if (line.isBlank()) return

        var infoRule = ReplaceRule()
        //判断是否为注释
        if (line.startsWith(NOTE_LABEL)) {
            val s = line.trimStart(NOTE_LABEL).trim()
            if (s.startsWith("{") && s.endsWith("}")) {
                infoRule = jsonBuilder.decodeFromString(s)
            } else if (s.startsWith("[") && s.endsWith("]")) {
                val group = jsonBuilder.decodeFromString<ReplaceRuleGroup>(s)
                groups.add(group)
            } else return
        }

        val regexIndex: Int = line.indexOf(REGEXP_END_LABEL)
        val regexStartIndex: Int = line.indexOf(REGEXP_START_LABEL)
        val length: Int = line.length

        val index = line.indexOf("=")
//        Log.e("SS", regexStartIndex.toString() + "SS")
        if (regexStartIndex != -1 && regexStartIndex < regexIndex && regexIndex < length) {
            val regex = line.substring(regexStartIndex + REGEXP_START_LABEL.length, regexIndex)
            val value: String = line.substring(regexIndex + REGEXP_END_LABEL.length)
            rules.add(infoRule.copy(pattern = regex, replacement = value, isRegex = true))
        } else if (length > 3 && index > 0 && index < length) {
            val key = line.substring(0, index).trim()
            val value = line.substring(index + 1, length).trim()
            if (key.isNotEmpty() && value.isNotEmpty()) {
                rules.add(infoRule.copy(pattern = key, replacement = value, isRegex = false))
            }
        }
    }
}