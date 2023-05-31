package com.github.jing332.tts_dict_editor.ui.filepicker

import androidx.documentfile.provider.DocumentFile
import java.io.File

data class FileModel(
    val file: File,

    val name: String,
    val isCheckable: Boolean = true,
    var isChecked: Boolean = false,
    val fileCount: Int = 0,
    val folderCount: Int = 0,

    val fileSize: Long = file.length(),

    val path: String = file.absolutePath,
    val isDirectory: Boolean = file.isDirectory,

    val documentFile: DocumentFile? = null,
    val isContentUriType: Boolean = documentFile != null,
) {
    fun isEmpty(): Boolean {
        return fileCount == 0 && folderCount == 0
    }
}