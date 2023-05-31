package com.github.jing332.tts_dict_editor.ui.filepicker.adapter

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import com.github.jing332.tts_dict_editor.ui.filepicker.FileModel
import com.github.jing332.tts_server_android.utils.ASFUriUtils
import com.github.jing332.tts_server_android.utils.ASFUriUtils.getPath
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class DocumentFileAdapter(
    private val context: Context,
    private val documentFile: DocumentFile
) : IFileAdapter {
    override fun name() = documentFile.name ?: ""

    override fun path(): String {
        return context.getPath(documentFile.uri, isDirectory()) ?: ""
    }

    override fun isDirectory() = documentFile.isDirectory

    override fun fileCount(): Int {
        return documentFile.listFiles().filter { it.isFile }.size
    }

    override fun directoryCount(): Int {
        return documentFile.listFiles().filter { it.isDirectory }.size
    }

    @OptIn(ExperimentalTime::class)
    override fun listFiles(): List<IFileAdapter> {
        var list: List<IFileAdapter> = emptyList()
        val time = measureTime {
            list = documentFile.listFiles().map {
                DocumentFileAdapter(context, it)
            }
        }
        println("$time ms")

        return list
    }
}