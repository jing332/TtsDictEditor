package com.github.jing332.tts_dict_editor.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.provider.DocumentsContract
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
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

    class OpenDocument() : ActivityResultContracts.OpenDocument() {
        override fun createIntent(context: Context, input: Array<String>): Intent {
            val mInput = input.toMutableList()
            val initialUri = input.getOrElse(0) { "" }
            mInput.removeAt(0)
            return super.createIntent(context, input).apply {
                addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                            or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                            or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
                )
                putExtra(
                    "android.provider.extra.SHOW_ADVANCED",
                    true
                ).putExtra("android.content.extra.SHOW_ADVANCED", true)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && initialUri.isNotBlank())
                    putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse(initialUri))
            }
        }
    }
}