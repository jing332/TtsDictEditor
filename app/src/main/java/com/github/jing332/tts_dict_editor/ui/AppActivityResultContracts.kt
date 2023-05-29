package com.github.jing332.tts_dict_editor.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContract
import com.github.jing332.tts_dict_editor.const.IntentKeys


object AppActivityResultContracts {
    /**
     * 用于传递Parcelable数据
     */
    @Suppress("DEPRECATION")
    fun <T : Parcelable> parcelableDataActivity(clz: Class<out Activity>) =
        object : ActivityResultContract<T?, T?>() {
            override fun createIntent(context: Context, input: T?): Intent {
                return Intent(context, clz).apply {
                    if (input != null) putExtra(IntentKeys.KEY_DATA, input)
                }
            }

            override fun parseResult(resultCode: Int, intent: Intent?): T? {
                return intent?.getParcelableExtra(IntentKeys.KEY_DATA)
            }
        }
}