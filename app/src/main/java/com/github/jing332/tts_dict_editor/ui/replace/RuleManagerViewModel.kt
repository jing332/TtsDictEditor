package com.github.jing332.tts_dict_editor.ui.replace

import androidx.compose.runtime.mutableStateListOf
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import com.github.jing332.tts_dict_editor.help.DictFileManager
import com.github.jing332.tts_dict_editor.help.GroupWithReplaceRule
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
}