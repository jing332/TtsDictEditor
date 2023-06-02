package com.github.jing332.tts_dict_editor.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.max


/**
 * 下拉框菜单
 */
@Composable
fun DropMenuTextField(
    label: @Composable() (() -> Unit),
    key: Any,
    keys: List<Any>,
    values: List<String>,
    onKeyChange: (key: Any) -> Unit,
) {
    var value by remember { mutableStateOf(values[max(0, keys.indexOf(key))]) }
    var menuExpanded by remember { mutableStateOf(false) }

    Column {
        OutlinedTextField(
            value = TextFieldValue(value),
            onValueChange = { value = "" },
            label = label,
            readOnly = true,
            enabled = false,
            colors = TextFieldDefaults.colors(disabledContainerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = true),
                    onClick = { menuExpanded = true }
                ),
        )

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }) {
            values.forEachIndexed { index, s ->
                DropdownMenuItem(
                    text = { Text(s) },
                    onClick = {
                        menuExpanded = false
                        value = s
                        onKeyChange.invoke(keys[index])
                    }, modifier = Modifier.background(
                        if (key == keys[index]) MaterialTheme.colorScheme.surfaceVariant
                        else Color.Transparent
                    )
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewDropMenu() {
    var key by remember { mutableIntStateOf(1) }
    DropMenuTextField(
        label = { Text("所属分组") },
        key = key,
        keys = listOf(1, 2, 3),
        values = listOf("1", "2", "3"),
    ) {
        key = it as Int
    }
}