package com.github.jing332.tts_dict_editor.help

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class GroupWithReplaceRule(val group: ReplaceRuleGroup, val list: List<ReplaceRule>) : Parcelable
