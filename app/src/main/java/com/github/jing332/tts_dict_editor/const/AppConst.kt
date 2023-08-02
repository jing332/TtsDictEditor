package com.github.jing332.tts_dict_editor.const

import com.charleskorn.kaml.PolymorphismStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

object AppConst {
    @OptIn(ExperimentalSerializationApi::class)
    val json by lazy {
        Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            explicitNulls = false
        }
    }

    val yaml by lazy {
        Yaml(
            configuration = YamlConfiguration(
                encodeDefaults = false,
                strictMode = false,
                polymorphismStyle = PolymorphismStyle.Tag
            )
        )
    }
}