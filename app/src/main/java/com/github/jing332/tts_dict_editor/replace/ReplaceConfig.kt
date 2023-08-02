package com.github.jing332.tts_dict_editor.replace

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("!org.nobody.multitts.tts.replace.ReplaceConfig")
data class ReplaceConfig(
    val replaces: List<ReplaceRuleYaml>
)