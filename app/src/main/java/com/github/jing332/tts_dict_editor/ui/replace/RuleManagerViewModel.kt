package com.github.jing332.tts_dict_editor.ui.replace

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.github.jing332.tts_dict_editor.help.DictFileManager
import com.github.jing332.tts_dict_editor.help.GroupWithReplaceRule
import com.github.jing332.tts_dict_editor.help.ReplaceRule
import java.io.InputStream

class RuleManagerViewModel : ViewModel() {
    val groupWithRules = mutableStateListOf<GroupWithReplaceRule>()

    private val dictManager = DictFileManager()

    suspend fun loadRulesFromDictTxt(input: InputStream) {
        dictManager.rules.clear()
        dictManager.load(input)
        dictManager.groupWithReplaceRules().forEach {
            groupWithRules.add(it)
        }
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