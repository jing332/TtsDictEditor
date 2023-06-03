package com.github.jing332.tts_dict_editor.ui.replace

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_dict_editor.R
import com.talhafaki.composablesweettoast.util.SweetToastUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigExportBottomSheet(json: String, onDismissRequest: () -> Unit) {
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    var clipToastMsg by remember { mutableStateOf(false) }
    if (clipToastMsg) {
        SweetToastUtil.SweetSuccess(message = stringResource(R.string.copied_to_clipboard))
    }

    ModalBottomSheet(onDismissRequest = onDismissRequest, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 8.dp)) {
            TextButton(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = {
                    clipboardManager.setText(AnnotatedString(json))
                    clipToastMsg = true
                    coroutineScope.launch {
                        delay(3000L)
                        clipToastMsg = false
                    }
                }) {
                Text(stringResource(id = R.string.clipboard))
            }
            Text(
                text = json,
                Modifier
                    .verticalScroll(rememberScrollState()),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}