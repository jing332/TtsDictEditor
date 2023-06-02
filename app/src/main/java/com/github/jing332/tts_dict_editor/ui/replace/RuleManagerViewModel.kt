package com.github.jing332.tts_dict_editor.ui.replace

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.jing332.tts_dict_editor.help.DictFileManager
import com.github.jing332.tts_dict_editor.help.DictFileManager.Companion.toTxt
import com.github.jing332.tts_dict_editor.help.GroupWithReplaceRule
import com.github.jing332.tts_dict_editor.help.ReplaceRule
import com.github.jing332.tts_dict_editor.help.ReplaceRuleGroup
import java.io.InputStream

class RuleManagerViewModel : ViewModel() {
    companion object {
        const val TAG = "RuleManagerViewModel"
    }

    val list = mutableStateListOf<Any>()

    val groupWithRules = mutableStateListOf<GroupWithReplaceRule>()

    //    var saveTxtState by mutableStateOf(null as String?)
    private var _saveTxtLiveData = MutableLiveData("" as String?)
    val saveTxtLiveData: LiveData<String?> = _saveTxtLiveData

    private val dictManager = DictFileManager()

    suspend fun loadRulesFromDictTxt(input: InputStream) {
        groupWithRules.clear()
        _saveTxtLiveData.postValue(null)

        dictManager.rules.clear()
        dictManager.load(input)
        dictManager.groupWithReplaceRules().forEach {
            groupWithRules.add(it)
        }
    }

    /**
     * 保存为Txt
     */
    private fun requestSaveTxt() {
        Log.d(TAG, "requestSaveTxt: groupSize:${groupWithRules.size}")
        _saveTxtLiveData.postValue(groupWithRules.toTxt())
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

        // 同步到 dict.txt
        requestSaveTxt()
    }

    fun updateGroup(group: ReplaceRuleGroup) {
        val index = groupWithRules.indexOfFirst { it.group.id == group.id }
        if (index > -1) {
            val newGroupWithRules =
                GroupWithReplaceRule(group, groupWithRules[index].list.toMutableList())
            groupWithRules[index] = newGroupWithRules
        } else { // Add Group
            groupWithRules.add(GroupWithReplaceRule(group, listOf()))
        }

        // 同步到 dict.txt
        requestSaveTxt()
    }

    fun deleteGroup(group: ReplaceRuleGroup) {
        val index = groupWithRules.indexOfFirst { it.group.id == group.id }
        if (index > -1) {
            Log.d(TAG, "deleteGroup: $group")
            groupWithRules.removeAt(index)
        }

        // 同步到 dict.txt
        requestSaveTxt()
    }
}