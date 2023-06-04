package com.github.jing332.tts_dict_editor.utils

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Log
import com.github.jing332.tts_server_android.util.longToast


object AndroidUtils {
    /**
     *  添加快捷方式
     *  @param name 快捷方式名称
     *  @param id 快捷方式id
     *  @param iconResId 快捷方式图标Id
     *  @param launcherIntent 启动的 intent
     */
    @SuppressLint("UnspecifiedImmutableFlag")
    @Suppress("DEPRECATION")
    fun Context.requestDesktopShortcut(
        name: String,
        id: String,
        iconResId: Int,
        launcherIntent: Intent
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) { // SDK < Android8.0
            longToast("如失败 请手动授予权限")
            val addShortcutIntent = Intent("com.android.launcher.action.INSTALL_SHORTCUT")
            // 不允许重复创建
            addShortcutIntent.putExtra("duplicate", false) // 经测试不是根据快捷方式的名字判断重复的
            addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name)
            addShortcutIntent.putExtra(
                Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(this, iconResId)
            )

            launcherIntent.action = Intent.ACTION_MAIN
            launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent)

            sendBroadcast(addShortcutIntent)
        } else {// SDK > Android8.0
            val shortcutManager: ShortcutManager = getSystemService(ShortcutManager::class.java)
            if (shortcutManager.isRequestPinShortcutSupported) {
                launcherIntent.action = Intent.ACTION_VIEW
                val pinShortcutInfo = ShortcutInfo.Builder(this, id)
                    .setIcon(Icon.createWithResource(this, iconResId))
                    .setIntent(launcherIntent)
                    .setShortLabel(name)
                    .build()
                val pinnedShortcutCallbackIntent = shortcutManager
                    .createShortcutResultIntent(pinShortcutInfo)

                val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.FLAG_IMMUTABLE
                } else 0
                val successCallback = PendingIntent.getBroadcast(
                    this, 0, pinnedShortcutCallbackIntent, pendingIntentFlags
                )
                shortcutManager.requestPinShortcut(pinShortcutInfo, successCallback.intentSender)
            }
        }
    }

    /**
     * 获取当前app version code
     */
    fun getAppVersionCode(context: Context): Long {
        var appVersionCode: Long = 0
        try {
            val packageInfo = context.applicationContext
                .packageManager
                .getPackageInfo(context.packageName, 0)
            appVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                packageInfo.versionCode.toLong()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("", e.message!!)
        }
        return appVersionCode
    }

    /**
     * 获取当前app version name
     */
    fun getAppVersionName(context: Context): String? {
        var appVersionName = ""
        try {
            val packageInfo = context.applicationContext
                .packageManager
                .getPackageInfo(context.packageName, 0)
            appVersionName = packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("", e.message!!)
        }
        return appVersionName
    }
}