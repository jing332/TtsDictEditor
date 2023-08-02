package com.github.jing332.tts_dict_editor.ui.replace

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.Javascript
import androidx.compose.material.icons.filled.Output
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.github.jing332.tts_dict_editor.R
import me.saket.cascade.CascadeColumnScope


@Composable
internal fun CascadeColumnScope.ConfigExportUiMenu(
    onJson: () -> Unit,
    onCustomFormat: () -> Unit,
    onYamlFormat: () -> Unit
) {
    DropdownMenuItem(leadingIcon = {
        Icon(
            Icons.Filled.Output, "",
            tint = MaterialTheme.colorScheme.onBackground
        )
    }, text = { Text(stringResource(id = R.string.config_export)) }, children = {
        androidx.compose.material3.DropdownMenuItem(
            leadingIcon = {
                Icon(
                    Icons.Filled.Javascript, "",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            },
            text = { Text(stringResource(R.string.json_format)) },
            onClick = { onJson.invoke() })
        androidx.compose.material3.DropdownMenuItem(
            leadingIcon = {
                Icon(
                    Icons.Filled.FormatColorText, "",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            },
            text = { Text(stringResource(R.string.new_multitts_format)) },
            onClick = { onYamlFormat() })

        androidx.compose.material3.DropdownMenuItem(
            leadingIcon = {
                Icon(
                    Icons.Filled.FormatColorText, "",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            },
            text = { Text(stringResource(R.string.custom_format)) },
            onClick = { onCustomFormat.invoke() })
    })
}