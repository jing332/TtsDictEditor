package com.github.jing332.tts_dict_editor.utils

import android.os.Environment

object FileTools {
    val PATH_EXTERNAL_STORAGE = Environment.getExternalStorageDirectory().absolutePath
    val PATH_ANDROID_DATA = "$PATH_EXTERNAL_STORAGE/Android/data"

    /**
     * 判断是否是 Android/data 下
     */
    fun isAndroidDataPath(path: String): Boolean {
        return path.startsWith(PATH_ANDROID_DATA)
    }

}