package com.github.jing332.tts_dict_editor.ui.replace

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class ReplaceRule(
    val name: String = "",
    val pattern: String = "",
    val replacement: String = "",
    val isRegex: Boolean = false
) : Parcelable