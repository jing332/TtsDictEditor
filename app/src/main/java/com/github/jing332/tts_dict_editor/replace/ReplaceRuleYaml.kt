package com.github.jing332.tts_dict_editor.replace

import kotlinx.serialization.Serializable

@Serializable
data class ReplaceRuleYaml(
    val activate: Boolean = false,
    val name: String = "",
    val regex: Boolean = false,
    val source: String,
    val target: String
)