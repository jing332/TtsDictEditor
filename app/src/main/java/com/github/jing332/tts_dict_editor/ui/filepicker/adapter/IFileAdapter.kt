package com.github.jing332.tts_dict_editor.ui.filepicker.adapter

interface IFileAdapter {
    fun name(): String
    fun path(): String

    fun isDirectory(): Boolean
    fun isFile(): Boolean = !isDirectory()

    fun fileCount(): Int
    fun directoryCount(): Int

    fun listFiles(): List<IFileAdapter>
}