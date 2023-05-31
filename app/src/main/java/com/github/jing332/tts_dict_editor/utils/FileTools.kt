package com.github.jing332.tts_dict_editor.utils

import android.os.Build
import android.os.Environment

object FileTools {
    val PATH_EXTERNAL_STORAGE = Environment.getExternalStorageDirectory().absolutePath.trimEnd('/')
    val PATH_ANDROID_DATA = "$PATH_EXTERNAL_STORAGE/Android/data"

    /**
     * 判断是否是 Android/data 下
     */
    fun isAndroidDataPath(path: String): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && path.startsWith(PATH_ANDROID_DATA)
    }

}