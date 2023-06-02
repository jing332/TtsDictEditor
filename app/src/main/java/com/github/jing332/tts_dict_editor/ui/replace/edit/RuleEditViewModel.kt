package com.github.jing332.tts_dict_editor.ui.replace.edit

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.github.jing332.tts_dict_editor.help.ReplaceRule
import com.github.jing332.tts_dict_editor.help.ReplaceRuleGroup

class RuleEditViewModel : ViewModel() {
    var nameState = mutableStateOf("")
        private set
    var patternState = mutableStateOf("")
        private set
    var replacementState = mutableStateOf("")
        private set
    var isRegexState = mutableStateOf(false)
        private set

    var groupsState = mutableStateListOf<ReplaceRuleGroup>()
        private set

    var groupKeyState = mutableStateOf(ReplaceRuleGroup())
        private set

    private var mReplaceRule: ReplaceRule? = null


    fun init(replaceRule: ReplaceRule?, groups: List<ReplaceRuleGroup>) {
        mReplaceRule = replaceRule ?: ReplaceRule()
        mReplaceRule?.let { rule ->
            groupsState.clear()
            groupsState.addAll(groups)

            nameState.value = rule.name
            patternState.value = rule.pattern
            replacementState.value = rule.replacement
            isRegexState.value = rule.isRegex
        }
    }

    fun setGroup(group: ReplaceRuleGroup) {
        groupKeyState.value = group
    }

    fun setName(s: String) {
        nameState.value = s
    }

    fun setPattern(s: String) {
        patternState.value = s
    }

    fun setReplacement(s: String) {
        replacementState.value = s
    }

    fun setIsRegex(b: Boolean) {
        isRegexState.value = b
    }

    fun getRule(): ReplaceRule {
        return mReplaceRule!!.copy(
            groupId = groupKeyState.value.id,
            name = nameState.value,
            pattern = patternState.value,
            replacement = replacementState.value,
            isRegex = isRegexState.value
        )
    }

    fun doReplace(text: String): String {
        return if (isRegexState.value)
            Regex(patternState.value).replace(text, replacementState.value)
        else
            text.replace(patternState.value, replacementState.value)
    }


}