package com.github.jing332.tts_dict_editor.ui.replace.edit

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.github.jing332.tts_dict_editor.ui.replace.ReplaceRule

class RuleEditViewModel : ViewModel() {
    var nameState = mutableStateOf("")
        private set
    var patternState = mutableStateOf("")
        private set
    var replacementState = mutableStateOf("")
        private set
    var isRegexState = mutableStateOf(false)
        private set

    private var mReplaceRule: ReplaceRule? = null


    fun init(replaceRule: ReplaceRule?) {
        mReplaceRule = mReplaceRule ?: ReplaceRule()
        mReplaceRule?.let { rule ->
            nameState.value = rule.name
            patternState.value = rule.pattern
            replacementState.value = rule.replacement
            isRegexState.value = rule.isRegex
        }
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
            name = nameState.value,
            pattern = patternState.value,
            replacement = replacementState.value,
            isRegex = isRegexState.value
        )
    }


}