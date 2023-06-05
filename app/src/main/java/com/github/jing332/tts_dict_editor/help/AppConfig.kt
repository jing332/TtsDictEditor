package com.github.jing332.tts_dict_editor.help

import com.funny.data_saver.core.DataSaverConverter.registerTypeConverters
import com.funny.data_saver.core.DataSaverPreferences
import com.funny.data_saver.core.mutableDataSaverStateOf
import com.github.jing332.tts_dict_editor.app
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
    }

    val dataSaverPref = DataSaverPreferences(app.getSharedPreferences("app", 0))

    val softKeyboardToolbar = mutableDataSaverStateOf<List<Pair<String, String>>>(
        dataSaverInterface = dataSaverPref,
        key = "",
        initialValue = emptyList()
    )
}