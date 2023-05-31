package com.github.jing332.tts_dict_editor.ui.replace

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Group(val name: String = "", val id: Long = 0) : Parcelable