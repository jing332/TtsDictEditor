package com.github.jing332.tts_dict_editor.help

import com.funny.data_saver.core.DataSaverConverter.registerTypeConverters
import com.funny.data_saver.core.DataSaverPreferences
import com.funny.data_saver.core.mutableDataSaverStateOf
import com.github.jing332.tts_dict_editor.app
import com.github.jing332.tts_dict_editor.const.ConfigConst
import com.github.jing332.tts_dict_editor.ui.theme.AppTheme
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalStdlibApi::class)
object AppConfig {
    @OptIn(ExperimentalSerializationApi::class)
    private val json by lazy {
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            allowStructuredMapKeys = true
        }
    }

    init {
        registerTypeConverters<List<Pair<String, String>>>(
            save = {
                json.encodeToString(it)
            },
            restore = {
                val list: List<Pair<String, String>> = try {
                    json.decodeFromString(it)
                } catch (_: Exception) {
                    emptyList()
                }
                list
            }
        )

        registerTypeConverters(
            save = { it.id },
            restore = { value ->
                AppTheme.values().find { it.id == value } ?: AppTheme.DEFAULT
            }
        )
    }

    val dataSaverPref = DataSaverPreferences(app.getSharedPreferences("app", 0))

    val theme = mutableDataSaverStateOf(
        dataSaverInterface = dataSaverPref,
        key = ConfigConst.KEY_THEME,
        initialValue = AppTheme.DEFAULT
    )

    val softKeyboardToolbar = mutableDataSaverStateOf<List<Pair<String, String>>>(
        dataSaverInterface = dataSaverPref,
        key = ConfigConst.KEY_SOFT_KEYBOARD_TOOLBAR,
        initialValue = emptyList()
    )

    val dictExportFormat = mutableDataSaverStateOf<String>(
        dataSaverInterface = dataSaverPref,
        key = ConfigConst.KEY_DICT_EXPORT_FORMAT,
        initialValue = "$1=$2"
    )
}