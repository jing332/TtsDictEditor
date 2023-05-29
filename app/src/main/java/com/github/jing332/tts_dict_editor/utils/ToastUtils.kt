@file:Suppress("unused")
/* https://github.com/gedoor/legado/blob/master/app/src/main/java/io/legado/app/utils/ToastUtils.kt */
package com.github.jing332.tts_server_android.util

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

fun Context.toast(@StringRes message: Int) {
    runOnUI {
        kotlin.runCatching {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}

fun Context.toast(message: CharSequence?) {
    runOnUI {
        kotlin.runCatching {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}

fun Context.longToast(@StringRes message: Int) {
    runOnUI {
        kotlin.runCatching {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }
}

fun Context.longToast(message: CharSequence?) {
    runOnUI {
        kotlin.runCatching {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }
}