package com.github.jing332.tts_dict_editor.ui.replace

import com.github.jing332.tts_dict_editor.help.ReplaceRule
import com.github.jing332.tts_dict_editor.help.ReplaceRuleGroup

data class RuleItemModel(val data: ReplaceRule, val group: ReplaceRuleGroup) {
    val key = "${data.groupId}_${data.id}"
    val name = data.name.ifEmpty { "${data.pattern} => ${data.replacement}" }
    val isEnabled = data.isEnabled
}