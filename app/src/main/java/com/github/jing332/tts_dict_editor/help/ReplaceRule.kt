package com.github.jing332.tts_dict_editor.help

import android.os.Parcelable
import android.os.SystemClock
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class ReplaceRule(
    var id: Long = 0,
    var groupId: Long = 0,

    var isEnabled: Boolean = true,

    var name: String = "",
    var pattern: String = "",
    var replacement: String = "",
    var isRegex: Boolean = false,
    var order: Int = 0
) : Parcelable {
    fun copyFrom(other: ReplaceRule) {
        this.id = other.id
        this.groupId = other.groupId
        this.isEnabled = other.isEnabled
        this.name = other.name
        this.pattern = other.pattern
        this.replacement = other.replacement
        this.isRegex = other.isRegex
        this.order = other.order
    }
}