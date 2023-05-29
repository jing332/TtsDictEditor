package com.github.jing332.tts_dict_editor.ui.filepicker

import java.io.File

data class ItemModel(
    val file: File,

    val name: String,
    val isCheckable: Boolean = true,
    var isChecked: Boolean = false,
    val fileCount: Int = 0,
    val folderCount: Int = 0,

    val fileSize: Long = file.length(),

    val id: String = file.absolutePath,
    val isDirectory: Boolean = file.isDirectory,
)