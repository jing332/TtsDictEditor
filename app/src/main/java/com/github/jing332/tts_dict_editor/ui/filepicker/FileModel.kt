package com.github.jing332.tts_dict_editor.ui.filepicker

import androidx.documentfile.provider.DocumentFile
import java.io.File

data class FileModel(
    /* 绝对路径 */
    val path: String,
    val name: String = "",

    val isCheckable: Boolean = true,
    var isChecked: Boolean = false,
    val fileCount: Int = 0,
    val folderCount: Int = 0,
    val fileSize: Long = 0,
    val isDirectory: Boolean = false,

    /* Content URI */
    val documentFile: DocumentFile? = null,
) {
    val isContentUriType: Boolean = documentFile != null
    fun isEmpty(): Boolean {
        return fileCount == 0 && folderCount == 0
    }

    override fun equals(other: Any?): Boolean {
        if (other is FileModel) {
            return path.trim('/') == other.path.trim('/')
        }
        return super.equals(other)
    }
}