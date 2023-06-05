package com.github.jing332.tts_dict_editor.ui

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.github.jing332.tts_dict_editor.R
import com.github.jing332.tts_dict_editor.ui.theme.AppTheme

//@Preview
//@Composable
//private fun PreviewDialog() {
//    var isVisible by remember { mutableStateOf(true) }
//    if (isVisible)
//        ThemeSettingsDialog({
//            isVisible = false
//        }, {
//
//        })
//
//}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsDialog(
    onDismissRequest: () -> Unit,
    currentTheme: AppTheme,
    onChangeTheme: (AppTheme) -> Unit
) {
    AlertDialog(onDismissRequest = onDismissRequest,
        title = {
            Text(text = stringResource(id = R.string.theme))
        }, confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(id = R.string.confirm))
            }
        }, text = {
            FlowRow {
                AppTheme.values().forEach {
                    val leadingIcon: @Composable () -> Unit = { Icon(Icons.Default.Check, null) }
//                var selected by remember { mutableStateOf(it == AppTheme.DEFAULT) }
                    val selected = currentTheme.id == it.id
                    FilterChip(
                        selected,
                        leadingIcon = if (selected) leadingIcon else null,
                        onClick = {
                            onChangeTheme(it)
                        },
                        label = { Text(stringResource(id = it.stringResId)) }
                    )
                }
            }
        })
}
