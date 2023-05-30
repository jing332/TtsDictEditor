package com.github.jing332.tts_dict_editor.ui.filepicker

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drake.net.utils.withIO
import kotlinx.coroutines.launch
import java.io.File

class FilePickerViewModel : ViewModel() {
    init {
        println("Init FilePickerViewModel")
    }

}