package com.github.jing332.tts_dict_editor.ui.replace

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Output
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_dict_editor.R
import me.saket.cascade.CascadeDropdownMenu
import me.saket.cascade.rememberCascadeState

@Composable
fun GroupItem(
    name: String,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDeleteAction: () -> Unit,
    onImportAction: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick.invoke() },
    ) {
        Image(
            modifier = Modifier.align(Alignment.CenterVertically),
            painter = painterResource(id = if (isExpanded) R.drawable.ic_arrow_expand else R.drawable.ic_arrow_collapse),
            contentDescription = if (isExpanded) "已展开" else "已收起",
        )
        Text(
            text = name,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterVertically)
                .weight(1f),
            fontWeight = FontWeight.W700,
            color = Color(0xFF0079D3)
        )

        var isMoreOptionsVisible by remember { mutableStateOf(false) }
        IconButton(onClick = {
            isMoreOptionsVisible = true
        }, modifier = Modifier.padding(end = 10.dp)) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = stringResource(id = R.string.more_options),
                tint = MaterialTheme.colorScheme.onBackground
            )

            val menuState = rememberCascadeState()
            CascadeDropdownMenu(state = menuState,
                expanded = isMoreOptionsVisible,
                onDismissRequest = { isMoreOptionsVisible = false }) {
                androidx.compose.material3.DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Edit, "",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    text = { Text(stringResource(R.string.edit)) },
                    onClick = {
                        onEdit.invoke()
                        isMoreOptionsVisible = false
                    }
                )
                androidx.compose.material3.DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Output, "",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    text = { Text(stringResource(R.string.config_export)) },
                    onClick = {
                        onImportAction.invoke()
                        isMoreOptionsVisible = false
                    }
                )
                Divider(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                )

                DropdownMenuItem(text = { Text(stringResource(R.string.delete)) },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Delete,
                            stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    children = {
                        androidx.compose.material3.DropdownMenuItem(text = {
                            Text(
                                stringResource(R.string.confirm_deletion),
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }, onClick = {
                            onDeleteAction.invoke()
                            isMoreOptionsVisible = false
                        })
                        androidx.compose.material3.DropdownMenuItem(text = {
                            Text(
                                stringResource(R.string.cancel)
                            )
                        },
                            onClick = { menuState.navigateBack() })
                    })
            }
        }
    }
}