package com.github.jing332.tts_dict_editor.ui.home

import android.os.SystemClock

data class ListGroupModel<D>(
    val name: String,
    val list: List<D> = mutableListOf(),
    val id: Long = SystemClock.elapsedRealtimeNanos(),
)