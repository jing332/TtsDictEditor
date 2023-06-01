package com.github.jing332.tts_dict_editor.help

class DictLoadException(
    val lines: Int = -1,
    override val message: String?,
    override val cause: Throwable? = null
) : Exception()