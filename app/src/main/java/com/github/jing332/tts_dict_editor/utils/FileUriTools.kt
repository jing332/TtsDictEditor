package com.github.jing332.tts_dict_editor.utils

import android.R.attr.path
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.github.jing332.tts_server_android.utils.ASFUriUtils.getPath
import java.io.File


object FileUriTools {
    //uri请求权限构建前缀
    private const val URI_PERMISSION_REQUEST_PREFIX = "com.android.externalstorage.documents"

    //uri请求权限构建完整前缀
    const val URI_PERMISSION_REQUEST_COMPLETE_PREFIX =
        "content://com.android.externalstorage.documents"

    //uri请求权限构建后缀主要特殊符号
    const val URI_PERMISSION_REQUEST_SUFFIX_SPECIAL_SYMBOL = "primary:"

    //uri路径分割符
    const val URI_SEPARATOR = "%2F"

    /**
     * 绝对路径 转为 Content Uri
     * @param isTree 是否是目录
     */
    fun String.toContentUri(isTree: Boolean = true): Uri? {
        val uriSuf = URI_PERMISSION_REQUEST_SUFFIX_SPECIAL_SYMBOL + this.replaceFirst(
            FileTools.PATH_EXTERNAL_STORAGE + File.separator, ""
        )
        return if (isTree) {
            DocumentsContract.buildTreeDocumentUri(URI_PERMISSION_REQUEST_PREFIX, uriSuf)
        } else {
            DocumentsContract.buildDocumentUri(URI_PERMISSION_REQUEST_PREFIX, uriSuf)
        }
    }
}