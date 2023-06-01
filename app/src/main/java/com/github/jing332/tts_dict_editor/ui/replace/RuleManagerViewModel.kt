package com.github.jing332.tts_dict_editor.ui.replace

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.github.jing332.tts_dict_editor.help.DictFileManager
import com.github.jing332.tts_dict_editor.help.DictFileManager.Companion.toTxt
import com.github.jing332.tts_dict_editor.help.GroupWithReplaceRule
import com.github.jing332.tts_dict_editor.help.ReplaceRule
import java.io.InputStream

class RuleManagerViewModel : ViewModel() {
    val groupWithRules = mutableStateListOf<GroupWithReplaceRule>()
    var saveTxtState by mutableStateOf(null as String?)

    private val dictManager = DictFileManager()

    suspend fun loadRulesFromDictTxt(input: InputStream) {
        dictManager.rules.clear()
        dictManager.load(input)
        dictManager.groupWithReplaceRules().forEach {
            groupWithRules.add(it)
        }
    }

    /**
     * 保存为Txt
     */
    fun requestSaveTxt() {
        saveTxtState = groupWithRules.toTxt()
    }

    fun updateOrAddRule(rule: ReplaceRule) {
        val groupIndex = groupWithRules.indexOfFirst { it.group.id == rule.groupId }
        if (groupIndex > -1) {
            val group = groupWithRules[groupIndex]
            val list = group.list

            val index = list.indexOfFirst { it.id == rule.id }
            if (index > -1) {
                val newGroupWithRules =
                    GroupWithReplaceRule(group.group, list.toMutableList().apply {
                        set(index, rule)
                    })
                groupWithRules[groupIndex] = newGroupWithRules
                return
            }
        }

        // 添加规则
        val newGroupWithRules = GroupWithReplaceRule(
            groupWithRules[0].group,
            groupWithRules[0].list.toMutableList().apply {
                add(rule)
            })
        groupWithRules[0] = newGroupWithRules

        // 同步到 dict.txt
        requestSaveTxt()
    }

    fun deleteRule(rule: ReplaceRule) {
        val groupIndex = groupWithRules.indexOfFirst { it.group.id == rule.groupId }
        if (groupIndex > -1) {
            val group = groupWithRules[groupIndex]
            val list = group.list

            val index = list.indexOfFirst { it.id == rule.id }
            if (index > -1) {
                val newGroupWithRules =
                    GroupWithReplaceRule(group.group, list.toMutableList().apply {
                        removeAt(index)
                    })
                groupWithRules[groupIndex] = newGroupWithRules
            }
        }
    }
}