package com.github.jing332.tts_dict_editor.help

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.buffer
import okio.source
import java.io.InputStream


class DictFileManager() {
    companion object {
        const val TAG = "DictFileLoader"

        private const val GROUP_LABEL = "GROUP"
        private const val NOTE_LABEL = '#'
        private const val DISABLED_LABEL = "DISABLED" // 禁用 例如 # DISABLED ...=...
        private const val REGEXP_START_LABEL = "r:\"^"
        private const val REGEXP_END_LABEL = "$\"="

        private const val RESULT_RESET = 0 // 中断
        private const val RESULT_CONTINUE = 1 // 跳过
        private const val RESULT_END = 2 // 结束


        @OptIn(ExperimentalSerializationApi::class)
        private val jsonBuilder by lazy {
            Json {
                ignoreUnknownKeys = true
                explicitNulls = false
            }
        }

        fun List<GroupWithReplaceRule>.toTxt(): String {
            val sb = StringBuilder()
            forEach {
                sb.appendLine("$NOTE_LABEL $GROUP_LABEL ${jsonBuilder.encodeToString(it.group)}")
                sb.append(encodeReplaceRule(it.list))
                sb.appendLine("$NOTE_LABEL END GROUP ${it.group.name} (${it.group.id})")
                sb.appendLine().appendLine()
            }

            return sb.toString()
        }

        fun encodeReplaceRule(rules: List<ReplaceRule>): String {
            val sb = StringBuilder()
            rules.forEach {
                val jStr = jsonBuilder.encodeToString(
                    it.copy(pattern = "", replacement = "", isRegex = false)
                )
                if(it.pattern.isBlank() && it.replacement.isBlank()) return@forEach

                sb.appendLine("# $jStr")
                val line = if (it.isRegex)
                    "$REGEXP_START_LABEL${it.pattern}$REGEXP_END_LABEL${it.replacement}"
                else
                    "${it.pattern}=${it.replacement}"

                if (it.isEnabled)
                    sb.appendLine(line)
                else
                    sb.appendLine("$NOTE_LABEL $DISABLED_LABEL $line")

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
     * 合并 [rules] 和 [groups]
     */
    fun groupWithReplaceRules(): List<GroupWithReplaceRule> {
        if (groups.indexOfFirst { it.id == 0L } == -1)
            groups.add(0, ReplaceRuleGroup("默认分组"))

        return groups.map { group ->
            GroupWithReplaceRule(
                group,
                rules.filter { it.groupId == group.id }.sortedBy { it.order }
            )
        }
    }


    suspend fun load(s: String) {
        load(s.byteInputStream())
    }

    /**
     * 加载后自动 close()
     */
    suspend fun load(inputStream: InputStream) {
        withContext(Dispatchers.IO) {
            inputStream.use {
                val buffered = it.source().buffer()
                var lines = 0
                var infoRule = ReplaceRule()
                while (coroutineContext.isActive) {
                    val line = buffered.readUtf8Line() ?: break
                    println(line)
                    lines++
                    kotlin.runCatching {
                        when (parseSingleLine(line.trimStart(), infoRule)) {
                            RESULT_RESET -> infoRule = ReplaceRule()
                            RESULT_CONTINUE -> {}
                            RESULT_END -> {
                                if (infoRule.id == 0L) {
                                    infoRule.id = System.currentTimeMillis() + lines
                                }
                                rules.add(infoRule)
                                infoRule = ReplaceRule()
                            }

                            else -> {}
                        }
                    }.onFailure { t ->
                        throw DictLoadException(
                            lines = lines,
                            message = "第${lines}行解析失败：${t.localizedMessage}",
                            cause = t
                        )
                    }
                }
                buffered.close()
            }
        }
    }

    /**
     * @return true 表示解析完毕
     */
    private fun parseSingleLine(line: String, infoRule: ReplaceRule): Int {
        if (line.isBlank()) return RESULT_RESET

        var formattedLine = line

        //判断是否为注释
        if (line.startsWith(NOTE_LABEL)) {
            val s = line.trimStart(NOTE_LABEL).trimStart()

            if (s.startsWith(DISABLED_LABEL)) { // 例如 # DISABLED ...=...
                infoRule.isEnabled = false
                formattedLine = s.removePrefix(DISABLED_LABEL).trimStart() // 移除 DISABLED
                //return RESULT_CONTINUE
            } else if (s.startsWith(GROUP_LABEL)) {
                val group: ReplaceRuleGroup =
                    jsonBuilder.decodeFromString(s.removePrefix(GROUP_LABEL).trimStart())
                groups.add(group)
                return RESULT_RESET
            } else if (s.startsWith("{") && s.endsWith("}")) {
                val parsed: ReplaceRule = jsonBuilder.decodeFromString(s)
                infoRule.copyFrom(parsed)
                return RESULT_CONTINUE
            } else return RESULT_RESET
        }

        parseRule(formattedLine, infoRule)
        return RESULT_END
    }

    // 解析正文
    private fun parseRule(line: String, infoRule: ReplaceRule) {
        val regexIndex: Int = line.indexOf(REGEXP_END_LABEL)
        val regexStartIndex: Int = line.indexOf(REGEXP_START_LABEL)
        val length: Int = line.length

        val index = line.indexOf("=")
//        Log.e("SS", regexStartIndex.toString() + "SS")
        if (regexStartIndex != -1 && regexStartIndex < regexIndex && regexIndex < length) {
            val regex = line.substring(regexStartIndex + REGEXP_START_LABEL.length, regexIndex)
            val value: String = line.substring(regexIndex + REGEXP_END_LABEL.length)
//            rules.add(infoRule.copy(pattern = regex, replacement = value, isRegex = true))
            infoRule.pattern = regex
            infoRule.replacement = value
            infoRule.isRegex = true
        } else if (length >= 2 && index > 0 && index < length) {
            val key = line.substring(0, index).trim()
            val value = line.substring(index + 1, length).trim()
            if (key.isNotEmpty() && value.isNotEmpty()) {
//                rules.add(infoRule.copy(pattern = key, replacement = value, isRegex = false))
                infoRule.pattern = key
                infoRule.replacement = value
                infoRule.isRegex = false
            }
        }
    }
}