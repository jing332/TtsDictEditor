package com.github.jing332.tts_dict_editor.ui

import androidx.annotation.StringRes
import com.github.jing332.tts_dict_editor.R

sealed class AppNavRoutes(@StringRes val titleResId: Int, val route: String) {
    object DictFileManager : AppNavRoutes(R.string.confirm, "DictFileManager")
    object About : AppNavRoutes(R.string.about, "About")
}