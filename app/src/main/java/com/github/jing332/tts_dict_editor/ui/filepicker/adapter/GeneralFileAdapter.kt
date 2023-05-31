package com.github.jing332.tts_dict_editor.ui.filepicker.adapter

import java.io.File
import java.io.FileFilter

class GeneralFileAdapter(val file: File) : IFileAdapter {
    override fun name(): String = file.name

    override fun path(): String = file.absolutePath

    override fun isDirectory() = file.isDirectory

    override fun fileCount(): Int {
        return file.listFiles(FileFilter { it.isFile })?.size ?: 0
    }

    override fun directoryCount(): Int {
        return file.listFiles(FileFilter { it.isDirectory })?.size ?: 0
    }

    override fun listFiles(): List<IFileAdapter> {
        return file.listFiles()?.map { GeneralFileAdapter(it) } ?: emptyList()
    }

}