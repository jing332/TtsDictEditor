package com.github.jing332.tts_dict_editor.ui.replace

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.github.jing332.tts_dict_editor.help.DictFileManager
import com.github.jing332.tts_dict_editor.help.GroupWithReplaceRule
import java.io.InputStream

class RuleManagerViewModel : ViewModel() {
    val groupWithRules = mutableStateListOf<GroupWithReplaceRule>()

    private val dictLoader = DictFileManager()

    suspend fun loadRulesFromDictTxt(input: InputStream) {
        dictLoader.rules.clear()
        dictLoader.load(input)
        dictLoader.rules


    }
}