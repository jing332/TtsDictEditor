package com.github.jing332.tts_dict_editor.ui.edit

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import com.github.jing332.tts_dict_editor.R
import com.github.jing332.tts_dict_editor.ui.AppActivityResultContracts
import com.github.jing332.tts_dict_editor.utils.ASFUriUtils.getPath
import com.github.jing332.tts_dict_editor.utils.FileTools
import com.github.jing332.tts_dict_editor.utils.FileUriTools.toContentUri
import com.github.jing332.tts_dict_editor.utils.FileUtils
import java.io.File


object FilePathPicker {
    // 外部拓展存储
    const val PATH_EXTERNAL = 0

    // Android/data
    const val PATH_ANDROID_DATA = 1

    // Android/data/ (SDK>=33)
    const val PATH_ANDROID_DATA_A13 = 2
}

@Preview
@Composable
fun PreviewFilePicker() {
    val uri = "/storage/emulated/0/Android/data".toContentUri(true).toString()
    FilePathPicker(uriString = uri, mimeTypes = listOf(), onResult = {
        Log.e("TAG", "PreviewFilePicker: $it")
    })
}

@Composable
fun FilePathPicker(
    uriString: String,
    mimeTypes: List<String>,
    /* Android/data 时 是否使用A13模式 即获取包名后进入系统文件选择器 */
    isA13Mode: Boolean = false,
    onResult: (Uri?) -> Unit
) {
    val picker = rememberLauncherForActivityResult(AppActivityResultContracts.OpenDocument()) {
        onResult.invoke(it)
    }

    val path =
        LocalContext.current.getPath(uriString.toUri(), false)
            ?: LocalContext.current.getPath(
                uriString.toUri(), true
            ) ?: return
    if (FileTools.isAndroidDataPath(path) && isA13Mode) {
        var isVisibleDialog by remember { mutableStateOf(true) }
        if (isVisibleDialog)
            AndroidDataDirSelectDialog(onDismissRequest = { isVisibleDialog = false }, onResult = {
                isVisibleDialog = false
                val uri =
                    "${Environment.getExternalStorageDirectory().absolutePath}/Android/data/$it"
                        .toContentUri(false).toString()
                picker.launch(arrayOf(uri, *mimeTypes.toTypedArray()))
            })

        return
    }

    LaunchedEffect(uriString) {
        picker.launch(arrayOf(uriString, *mimeTypes.toTypedArray()))
    }
}

@Composable
private fun AndroidDataDirSelectDialog(onDismissRequest: () -> Unit, onResult: (String) -> Unit) {
    val context = LocalContext.current
    AlertDialog(onDismissRequest = onDismissRequest,
        title = { Text("选择文件夹") },
        confirmButton = {
            TextButton(onClick = { /*TODO*/ }) {
                Text(stringResource(id = R.string.cancel))
            }
        }, text = {
            val pkgNames = remember { getInstallPackageNames(context) }
            LazyColumn(modifier = androidx.compose.ui.Modifier.fillMaxWidth()) {
                items(items = pkgNames, key = { it }) {
                    val icon = remember { getAppIcon(context, it)?.toBitmap()?.asImageBitmap() }
                    Row(
                        Modifier
                            .clickable { onResult(it) }
                            .padding(vertical = 8.dp)) {
                        Image(
                            icon
                                ?: context.getDrawable(R.drawable.baseline_image_24)!!.toBitmap()
                                    .asImageBitmap(),
                            contentDescription = "",
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = it,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 4.dp)
                                .align(androidx.compose.ui.Alignment.CenterVertically)
                        )
                    }
                }
            }
        }
    )
}

private fun getAppIcon(context: Context, name: String): Drawable? {
    return try {
        context.packageManager.getApplicationIcon(name)
    } catch (e: Exception) {
        null
    }
}

@Suppress("DEPRECATION")
private fun getInstallPackageNames(context: Context): List<String> {
    val pm = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN)
        .addCategory(Intent.CATEGORY_LAUNCHER)

    val resolveInfoList: List<ResolveInfo> = pm.queryIntentActivities(intent, 0)
    val packageNameList = mutableListOf<String>()

    var packageName: String
    for (resolveInfo in resolveInfoList) {
        packageName = resolveInfo.activityInfo.packageName
        if (FileUtils.exists(
                "${Environment.getExternalStorageDirectory().absolutePath}${File.separator}Android/data/$packageName"
            )
        ) {
            packageNameList.add(packageName)
        }
    }

    return packageNameList
}

@Preview
@Composable
fun Preview() {
    AndroidDataDirSelectDialog(onDismissRequest = { }, onResult = {
        Log.e("TAG", it)
    })
//    val path = "/storage/emulated/0/Android/data/org.nobody.multitts"
//    println(File(path).exists())

//    LazyColumn {
//        item {
//            getInstallPackageNames(LocalContext.current).forEach {
//                Log.e("TAG", it)
//                Text(text = it)
//            }
//        }
//    }
    /*   FilePathPicker(
           uriString = "",
           mimeTypes = listOf(),
           onResult = {}
       )*/
}