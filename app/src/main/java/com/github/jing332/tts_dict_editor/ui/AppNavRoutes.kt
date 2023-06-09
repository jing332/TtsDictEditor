package com.github.jing332.tts_dict_editor.ui

import androidx.annotation.StringRes
import com.github.jing332.tts_dict_editor.R
import com.github.jing332.tts_dict_editor.const.AppConst
import com.github.jing332.tts_dict_editor.data.entites.DictFile
import kotlinx.serialization.encodeToString
import java.net.URLEncoder
import java.nio.charset.Charset

sealed class AppNavRoutes(@StringRes val titleResId: Int, val route: String) {
    object DictFileEdit : AppNavRoutes(R.string.set_dict_file, "dictFileEdit") {
        const val KEY_DICT_FILE = "dict_file"
    }

    object DictFileManager : AppNavRoutes(R.string.confirm, "DictFileManager")
    object About : AppNavRoutes(R.string.about, "About")
}