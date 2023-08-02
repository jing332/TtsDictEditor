package com.github.jing332.tts_dict_editor.ui.replace

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drake.net.utils.withDefault
import com.github.jing332.tts_dict_editor.const.AppConst
import com.github.jing332.tts_dict_editor.help.DictFileManager
import com.github.jing332.tts_dict_editor.help.DictFileManager.Companion.toTxt
import com.github.jing332.tts_dict_editor.help.GroupWithReplaceRule
import com.github.jing332.tts_dict_editor.help.ReplaceRule
import com.github.jing332.tts_dict_editor.help.ReplaceRuleGroup
import com.github.jing332.tts_dict_editor.replace.ReplaceManager
import com.github.jing332.tts_dict_editor.replace.ReplaceRuleYaml
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import java.io.InputStream
import kotlin.coroutines.coroutineContext
import kotlin.math.max
import kotlin.math.min

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
            list.add(GroupWithReplaceRule(group, rules.filterIndexed { index, replaceRule ->
                if (!coroutineContext.isActive) return list

                replaceRule.order = index
                replaceRule.groupId == group.id
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
        synchronized(this) {
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
    }

    /**
     * 删除规则 完毕后自动同步到 dict.txt
     */
    suspend fun deleteRule(rule: ReplaceRule, isUpdate: Boolean = true) {
        synchronized(this) {

            val idx = list.indexOfFirst { it is ReplaceRule && it.id == rule.id }
            if (idx > -1) {
                list.removeAt(idx)
            }
            if (isUpdate) requestSaveTxt()
        }
    }

    fun updateOrAddGroup(group: ReplaceRuleGroup) {
        synchronized(this) {

            val idx = findGroupIndex(group)
            if (idx > -1) {
                list[idx] = group
            } else
                list.add(group)

            // 同步到 dict.txt
            requestSaveTxt()
        }
    }

    fun deleteGroup(group: ReplaceRuleGroup) {
        synchronized(this) {

            val idx = findGroupIndex(group)
            if (idx > -1) {
                list.removeAt(idx)
            }

            // 同步到 dict.txt
            requestSaveTxt()
        }
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

    suspend fun exportYaml(): String {
        return ReplaceManager.toYaml(rules().map {
            ReplaceRuleYaml(
                activate = it.isEnabled,
                name = it.name.ifEmpty { it.pattern },
                regex = it.isRegex,
                source = it.pattern,
                target = it.replacement
            )
        })
    }

    suspend fun export(): String {
        val gwrs = groupWithRules()
        return AppConst.json.encodeToString(gwrs)
    }

    suspend fun exportByFormat(format: String): String {
        val gwrs = groupWithRules()
        return gwrs.flatMap { it.list }
            .joinToString("\n") { format.replace("$1", it.pattern).replace("$2", it.replacement) }
    }

    suspend fun exportGroup(group: ReplaceRuleGroup): String {
        val gwrs = groupWithRules().filter { it.group.id == group.id }
        return AppConst.json.encodeToString(gwrs)
    }

    suspend fun exportGroupByFormat(group: ReplaceRuleGroup, format: String): String {
        val gwrs = groupWithRules().filter { it.group.id == group.id }
        return gwrs.flatMap { it.list }
            .joinToString("\n") { format.replace("$1", it.pattern).replace("$2", it.replacement) }
    }

    fun reorder(from: Int, to: Int) {
        val fromItem = list[from]
        val toItem = list[to]
//        val startIndex = min(from, to)
//        val endIndex = max(from, to)

        if (fromItem is ReplaceRule && toItem is ReplaceRule && fromItem.groupId == toItem.groupId) {
            list.add(to, list.removeAt(from))
            requestSaveTxt()
        }

//        list.toList().forEachIndexed { index, any ->
//            if (index in startIndex..endIndex) {
//                when (index) {
//                    from -> list.removeAt(index) // 移除
//                    to -> list[index] = fromItem
////                    else -> list[index] = any
//                }
//            }
//        }
    }

}