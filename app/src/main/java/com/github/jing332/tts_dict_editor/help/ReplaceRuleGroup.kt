package com.github.jing332.tts_dict_editor.help

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class ReplaceRuleGroup(val name: String = "", val id: Long = 0, var order: Int = 0) :
    Parcelable