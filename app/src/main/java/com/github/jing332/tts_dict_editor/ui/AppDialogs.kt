package com.github.jing332.tts_dict_editor.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.github.jing332.tts_dict_editor.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorDialog(
    t: Throwable? = null,
    title: String = stringResource(R.string.error),
    message: String = t?.localizedMessage ?: "",
    onDismiss: () -> Unit = {}
) {
    var isShow by remember { mutableStateOf(true) }
    if (isShow)
        AlertDialog(
            icon = { Icon(Icons.Filled.ErrorOutline, "", tint = MaterialTheme.colorScheme.error) },
            title = { Text(title) },
            text = {
                Column {
                    Text(text = message)
                    t?.stackTraceToString()?.let { traceString ->
                        val lines = traceString.lines()
                        LazyColumn(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            item {
                                lines.forEach {
                                    Text(text = it)
                                }
                            }
                        }
                    }
                }
            },
            onDismissRequest = {
                isShow = false
                onDismiss.invoke()
            },
            confirmButton = {
                Button(onClick = {
                    isShow = false
                    onDismiss.invoke()
                }) {
                    Text(stringResource(R.string.confirm))
                }
            }
        )
}

@Preview
@Composable
private fun PreviewErrorDialog() {
    ErrorDialog(Throwable("error"))
}

