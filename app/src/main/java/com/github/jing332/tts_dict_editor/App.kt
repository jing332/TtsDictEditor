package com.github.jing332.tts_dict_editor

import android.app.Application
import android.content.Context
import com.chibatching.kotpref.Kotpref
import java.util.*
import kotlin.properties.Delegates


val app: App
    inline get() = App.instance

@Suppress("DEPRECATION")
class App : Application() {
    companion object {
        const val TAG = "App"
        var instance: App by Delegates.notNull()
        val context: Context by lazy { instance }


    }


    override fun onCreate() {
        super.onCreate()
        instance = this

        Kotpref.init(this)
    }
}