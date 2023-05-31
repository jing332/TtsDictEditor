package com.github.jing332.tts_dict_editor.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import com.github.jing332.tts_dict_editor.utils.FileUriTools.toContentUri


object FilePermissionTools {
    val requestPermissionAndroidDataIntent: Intent
        get() = getRequestPermissionIntent("primary:Android/data")


    /**
     * 判断 content uri 是否已经被授权
     * @return 已经授权返回授权的uri，未授权返回null
     */
    fun isGrantedUriPermission(uri: Uri, context: Context): String? {
        val reqUri = uri.toString().replace("documents/document/primary", "documents/tree/primary")
        val permissions = context.contentResolver.persistedUriPermissions
        println("src: $uri")

        var tempUri: String?
        //遍历并判断请求的uri字符串是否已经被授权
        for (uriP in permissions) {
            tempUri = uriP.uri.toString()
            println(tempUri)
            //如果父目录已经授权就返回已经授权
            if (reqUri.matches((tempUri + FileUriTools.URI_SEPARATOR + ".*").toRegex()) || reqUri == tempUri && (uriP.isReadPermission || uriP.isWritePermission)) {
                return tempUri
            }
        }

        return null
    }

    fun getRequestPermissionIntent(path: String, isTree: Boolean = false): Intent {
        return getRequestPermissionIntent(path.toContentUri(isTree)!!)
    }

    fun getRequestPermissionIntent(uri: Uri): Intent {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.flags = (Intent.FLAG_GRANT_READ_URI_PERMISSION
                or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
        intent.putExtra("android.provider.extra.SHOW_ADVANCED", true)
            .putExtra("android.content.extra.SHOW_ADVANCED", true)
            .putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri)
        return intent
    }
}