package com.github.jing332.tts_dict_editor.ui.replace

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drake.net.utils.withDefault
import com.github.jing332.tts_dict_editor.help.DictFileManager
import com.github.jing332.tts_dict_editor.help.DictFileManager.Companion.toTxt
import com.github.jing332.tts_dict_editor.help.GroupWithReplaceRule
import com.github.jing332.tts_dict_editor.help.ReplaceRule
import com.github.jing332.tts_dict_editor.help.ReplaceRuleGroup
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.InputStream
import kotlin.coroutines.coroutineContext

class RuleManagerViewModel : ViewModel() {
    companion object {
        const val TAG = "RuleManagerViewModel"
    }

    val list = mutableStateListOf<Any>()
    fun groups(): List<ReplaceRuleGroup> = list.filterIsInstance<ReplaceRuleGroup>()
    fun rules(): List<ReplaceRule> = list.filterIsInstance<ReplaceRule>()

    suspend fun groupWithRules(): List<GroupWithReplaceRule> {
        val list = mutableListOf<GroupWithReplaceRule>()
        val rules = rules()
        groups().forEach { group ->
            list.add(GroupWithReplaceRule(group, rules.filter {
                if (!coroutineContext.isActive) return list

                it.groupId == group.id
            }))
        }

        return list
    }

    //    var saveTxtState by mutableStateOf(null as String?)
    private var _saveTxtLiveData = MutableLiveData("" as String?)
    val saveTxtLiveData: LiveData<String?> = _saveTxtLiveData

    private val dictManager = DictFileManager()

    suspend fun loadRulesFromDictTxt(input: InputStream) {
        dictManager.rules.clear()
        dictManager.load(input)
        dictManager.groupWithReplaceRules().forEach { gwrs ->
            list.add(gwrs.group)
            gwrs.list.forEach { rule ->
                list.add(rule)
            }
        }
    }

    private var mSaveTxtJob: Job? = null

    /**
     * 保存为Txt 200ms内只执行最新的一次
     */
    private fun requestSaveTxt() {
        mSaveTxtJob?.cancel()

        mSaveTxtJob = viewModelScope.launch {
            delay(200)
            val gwrs = withDefault { groupWithRules() }
            Log.d(TAG, "requestSaveTxt: groupSize:${gwrs.size}")
            _saveTxtLiveData.postValue(gwrs.toTxt())
        }
    }

    /**
     * 更新或添加规则 完毕后自动同步到 dict.txt
     */
    fun updateOrAddRule(rule: ReplaceRule, isUpdate: Boolean = true) {
        val idx = list.indexOfFirst { it is ReplaceRule && it.id == rule.id }
        if (idx > -1) { // Update
            val src = list[idx] as ReplaceRule
            if (src.groupId == rule.groupId)
                list[idx] = rule
            else { // 移动到其他组
                list.removeAt(idx)
                val gIdx = list.indexOfFirst { it is ReplaceRuleGroup && it.id == rule.groupId }
                if (gIdx > -1) list.add(gIdx + 1, rule)
                else list.add(rule)
            }
        } else { // Add
            val gIdx = list.indexOfFirst { it is ReplaceRuleGroup && it.id == rule.groupId }
            if (gIdx > -1) { // 加到组的后面
                list.add(gIdx + 1, rule)
                return
            }
        }

        if (isUpdate) requestSaveTxt()
    }

    /**
     * 删除规则 完毕后自动同步到 dict.txt
     */
    suspend fun deleteRule(rule: ReplaceRule, isUpdate: Boolean = true) {
        val idx = list.indexOfFirst { it is ReplaceRule && it.id == rule.id }
        if (idx > -1) {
            list.removeAt(idx)
        }
        if (isUpdate) requestSaveTxt()
    }

    fun updateOrAddGroup(group: ReplaceRuleGroup) {
        val idx = findGroupIndex(group)
        if (idx > -1) {
            list[idx] = group
        } else
            list.add(group)

        // 同步到 dict.txt
        requestSaveTxt()
    }

    fun deleteGroup(group: ReplaceRuleGroup) {
        val idx = findGroupIndex(group)
        if (idx > -1) {
            list.removeAt(idx)
        }

        // 同步到 dict.txt
        requestSaveTxt()
    }

    private fun findGroupIndex(group: ReplaceRuleGroup): Int =
        list.indexOfFirst { it is ReplaceRuleGroup && it.id == group.id }

    private fun findRuleIndex(rule: ReplaceRule): Int =
        list.indexOfFirst { it is ReplaceRule && it.id == rule.id }

    fun import(gwrs: List<GroupWithReplaceRule>): Int {
        var count = 0
        for (gwr in gwrs) {
            updateOrAddGroup(gwr.group)
            for (rule in gwr.list) {
                updateOrAddRule(rule)
                count++
            }
        }

        requestSaveTxt()
        return count
    }

}