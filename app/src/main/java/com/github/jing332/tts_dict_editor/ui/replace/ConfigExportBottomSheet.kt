package com.github.jing332.tts_dict_editor.ui.replace

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.drake.net.utils.fileName
import com.github.jing332.tts_dict_editor.R
import com.github.jing332.tts_server_android.utils.ASFUriUtils.getPath
import com.talhafaki.composablesweettoast.util.SweetToastUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigExportBottomSheet(
    json: String,
    fileName: String = "config.json",
    onDismissRequest: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var longToastMsg by remember { mutableStateOf<String?>(null) }
    if (longToastMsg != null) {
        SweetToastUtil.SweetSuccess(
            message = longToastMsg ?: "",
            padding = PaddingValues(bottom = 32.dp)
        )
        longToastMsg = null
    }

    var savedFileData by remember { mutableStateOf<ByteArray?>(null) }
    val fileSaver =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) {
            it?.let { uri ->
                savedFileData?.let { data ->
                    context.contentResolver.openOutputStream(uri, "wt"/* 覆写 */)?.use { os ->
                        os.write(data)
                        longToastMsg = context.getString(
                            R.string.saved_to_file,
                            context.getPath(uri, false) ?: uri.fileName() ?: uri.toString()
                        )
                    }
                }
            }
        }


    val clipboardManager = LocalClipboardManager.current

    ModalBottomSheet(onDismissRequest = onDismissRequest, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 8.dp)) {
            Row(Modifier.align(Alignment.CenterHorizontally)) {
                TextButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(json))
                        longToastMsg = context.getString(R.string.copied_to_clipboard)
                    }) {
                    Text(stringResource(id = R.string.copy))
                }
                TextButton(
                    onClick = {
                        savedFileData = json.toByteArray()
                        fileSaver.launch(fileName)
                    }) {
                    Text(stringResource(id = R.string.save_as_file))
                }
            }
            SelectionContainer(
                Modifier
                    .horizontalScroll(rememberScrollState()),
            ) {
                Text(
                    text = json,
                    Modifier
                        .verticalScroll(rememberScrollState()),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}