package com.github.jing332.tts_dict_editor.ui.replace

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.jing332.tts_dict_editor.R
import me.saket.cascade.CascadeDropdownMenu
import me.saket.cascade.rememberCascadeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReplaceRuleItem(
    name: String,
    modifier: Modifier,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    isChecked: Boolean = false,
    onCheckedChange: (Boolean) -> Unit = {},
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.elevatedCardColors(),
        elevation = CardDefaults.elevatedCardElevation(),
        modifier = modifier
    ) {
        Row(modifier = modifier.fillMaxSize()) {
            Checkbox(
                modifier = Modifier.align(Alignment.CenterVertically),
                checked = isChecked,
                onCheckedChange = onCheckedChange
            )
            Text(
                name,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
                    .fillMaxWidth()
                    .align(Alignment.CenterVertically),
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

                    DropdownMenuItem(text = { Text(stringResource(R.string.delete)) },
                        trailingIcon = {
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
                                onDelete.invoke()
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

}