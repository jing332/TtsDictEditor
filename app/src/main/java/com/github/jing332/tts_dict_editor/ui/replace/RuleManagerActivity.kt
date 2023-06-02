package com.github.jing332.tts_dict_editor.ui.replace

import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Output
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.github.jing332.tts_dict_editor.R
import com.github.jing332.tts_dict_editor.help.ReplaceRule
import com.github.jing332.tts_dict_editor.help.ReplaceRuleGroup
import com.github.jing332.tts_dict_editor.ui.AppActivityResultContracts
import com.github.jing332.tts_dict_editor.ui.ErrorDialog
import com.github.jing332.tts_dict_editor.ui.Widgets
import com.github.jing332.tts_dict_editor.ui.theme.AppTheme
import com.github.jing332.tts_dict_editor.utils.observeNoSticky
import com.github.jing332.tts_server_android.util.longToast
import com.github.jing332.tts_server_android.utils.ASFUriUtils.getPath
import kotlinx.coroutines.launch
import me.saket.cascade.CascadeDropdownMenu
import me.saket.cascade.rememberCascadeState
import okio.buffer
import okio.sink

class RuleManagerActivity : ComponentActivity() {
    companion object {
        const val TAG = "RuleManagerActivity"
    }

    private val vm: RuleManagerViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val uri = intent.data
        if (uri == null) {
            Log.e(TAG, "onCreate: uri is null")
            finish()
            return
        }

        var titleState by mutableStateOf("")
        var subTitleState by mutableStateOf("")
        var isVisibleImportConfig by mutableStateOf(false)
        var errDialog by mutableStateOf<Pair<String, Throwable?>?>(null)

        vm.saveTxtLiveData.observeNoSticky(this) {
            synchronized(vm.saveTxtLiveData) {
                it?.let { txt -> /* 同步到 dict.txt */
                    kotlin.runCatching {
                        contentResolver.openOutputStream(uri, "wt"/* 覆写 */)
                            ?.use { os -> os.write(txt.toByteArray()) }
                    }.onFailure { t ->
                        errDialog = "保存文件错误" to t
                    }
                }
            }
        }

        setContent {
            AppTheme {
                Widgets.TransparentSystemBars()

                /* 错误对话框 */
                if (errDialog != null)
                    ErrorDialog(
                        title = errDialog?.first ?: "Error",
                        t = errDialog?.second,
                        onDismiss = { errDialog = null }
                    )

                /* 编辑规则 */
                val launcherForEditRuleActivity =
                    rememberLauncherForActivityResult(AppActivityResultContracts.EditReplaceRule()) {
                        it?.let { rule -> vm.updateOrAddRule(rule) }
                    }

                fun launchEditRule(rule: ReplaceRule): Unit {
                    val groups = vm.groupWithRules.map { it.group }
                    launcherForEditRuleActivity.launch(groups to rule)
                }

                /* 编辑分组对话框 */
                var groupInfoEdit by remember { mutableStateOf<ReplaceRuleGroup?>(null) }
                if (groupInfoEdit != null) {
                    var name by remember { mutableStateOf(groupInfoEdit?.name ?: "") }
                    GroupInfoEditDialog(
                        name = name,
                        onConfirm = {
                            vm.updateGroup(groupInfoEdit!!.copy(name = name))
                            groupInfoEdit = null
                        },
                        onNameChange = { name = it },
                        onDismissRequest = { groupInfoEdit = null }
                    )
                }

                Scaffold(
                    topBar = {
                        var isVisibleAddMenu by remember { mutableStateOf(false) }
                        TopAppBar(
                            modifier = Modifier.fillMaxWidth(),
                            title = {
                                Column {
                                    Text(
                                        text = titleState,
                                        maxLines = 1,
                                        style = MaterialTheme.typography.titleMedium,
                                    )

                                    Text(
                                        text = subTitleState,
                                        maxLines = 2,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            actions = {
                                IconButton(onClick = {
                                    isVisibleAddMenu = true
                                }) {
                                    Icon(Icons.Filled.Add, stringResource(id = R.string.add))
                                    CascadeDropdownMenu(
                                        expanded = isVisibleAddMenu,
                                        onDismissRequest = { isVisibleAddMenu = false }) {
                                        androidx.compose.material3.DropdownMenuItem(
                                            text = { Text(stringResource(id = R.string.add_rule)) },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Filled.Add, "",
                                                    tint = MaterialTheme.colorScheme.onBackground
                                                )
                                            },
                                            onClick = {
                                                launchEditRule(ReplaceRule())
                                                isVisibleAddMenu = false
                                            }
                                        )

                                        androidx.compose.material3.DropdownMenuItem(
                                            text = { Text(stringResource(id = R.string.add_group)) },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Filled.Group,
                                                    "",
                                                    tint = MaterialTheme.colorScheme.onBackground
                                                )
                                            },
                                            onClick = {
                                                groupInfoEdit =
                                                    ReplaceRuleGroup(id = System.currentTimeMillis())
                                                isVisibleAddMenu = false
                                            }
                                        )
                                    }
                                }

                                var isMoreOptionsVisible by remember {
                                    mutableStateOf(false)
                                }
                                IconButton(onClick = {
                                    isMoreOptionsVisible = true
                                }) {
                                    Icon(
                                        Icons.Filled.MoreVert,
                                        stringResource(id = R.string.more_options)
                                    )

                                    CascadeDropdownMenu(
                                        expanded = isMoreOptionsVisible,
                                        onDismissRequest = { isMoreOptionsVisible = false }) {
                                        androidx.compose.material3.DropdownMenuItem(
                                            text = { Text(getString(R.string.import_config)) },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Filled.Input,
                                                    "",
                                                    tint = MaterialTheme.colorScheme.onBackground
                                                )
                                            },
                                            onClick = {
                                                isVisibleImportConfig = true
                                            }
                                        )
                                        androidx.compose.material3.DropdownMenuItem(
                                            text = { Text(getString(R.string.export_config)) },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Filled.Output,
                                                    "",
                                                    tint = MaterialTheme.colorScheme.onBackground
                                                )
                                            },
                                            onClick = { /*TODO*/ }
                                        )
                                    }
                                }
                            }
                        )
                    },
                    content = { pad ->
                        Surface(modifier = Modifier.padding(pad)) {
                            Screen(
                                onEditGroup = { groupInfoEdit = it },
                                onEditRule = { launchEditRule(it) }
                            )
                        }
                    })

                if (isVisibleImportConfig)
                    ImportConfigDialog { isVisibleImportConfig = false }
            }
        }

        titleState = intent.getStringExtra("name") ?: getString(R.string.replace_rule_manager)
        subTitleState =
            getPath(uri)?.removePrefix(Environment.getExternalStorageDirectory().absolutePath)
                ?: uri.toString()

        lifecycleScope.launch {
            kotlin.runCatching {
                vm.loadRulesFromDictTxt(contentResolver.openInputStream(uri)!!)
            }.onFailure {
                longToast("加载失败: ${it.message}")
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ImportConfigDialog(onDismiss: () -> Unit) {
        val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ConfigImportBottomSheet(onImportFromJson = {

        }, onDismiss = onDismiss)
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun Screen(onEditGroup: (ReplaceRuleGroup) -> Unit, onEditRule: (ReplaceRule) -> Unit) {
        val groups = vm.groupWithRules

        // Keys
        val expandedGroups =
            remember { mutableStateListOf(*groups.map { it.group.id }.toTypedArray()) }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            for ((index, groupWithRule) in groups.withIndex()) {
                stickyHeader(key = "group_${groupWithRule.group.id}") {
                    GroupItem(
                        name = groupWithRule.group.name,
                        isExpanded = expandedGroups.contains(groupWithRule.group.id),
                        onClick = {
                            if (expandedGroups.contains(groupWithRule.group.id))
                                expandedGroups.remove(groupWithRule.group.id)
                            else
                                expandedGroups.add(groupWithRule.group.id)
                        },
                        onEdit = {
                            onEditGroup.invoke(groupWithRule.group)
                        },
                        onDeleteAction = {
                            vm.deleteGroup(groupWithRule.group)
                        }
                    )
                }

                groupWithRule.list.forEach { replaceRule ->
                    item(key = "${replaceRule.groupId}_${replaceRule.id}") {
                        if (expandedGroups.contains(groupWithRule.group.id))
                            ReplaceRuleItem(
                                replaceRule.name.ifBlank { "${replaceRule.pattern} -> ${replaceRule.replacement}" },
                                modifier = Modifier.animateItemPlacement(),
                                onDelete = { vm.deleteRule(replaceRule) },
                                onClick = { onEditRule.invoke(replaceRule) }
                            )
                    }
                }

            }
        }
    }

    @Composable
    fun GroupItem(
        name: String,
        isExpanded: Boolean,
        onClick: () -> Unit,
        onEdit: () -> Unit,
        onDeleteAction: () -> Unit
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ReplaceRuleItem(
        name: String,
        modifier: Modifier,
        onClick: () -> Unit,
        onDelete: () -> Unit
    ) {
        Card(
            onClick = onClick,
            colors = CardDefaults.elevatedCardColors(),
            elevation = CardDefaults.elevatedCardElevation(),
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Row(modifier = modifier.fillMaxSize()) {
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

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupInfoEditDialog(
    name: String,
    onNameChange: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(title = { Text(stringResource(R.string.edit_group)) },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(R.string.confirm))
            }
        },
        text = {
            Column(
                Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(stringResource(R.string.group_name)) },
                    value = name,
                    onValueChange = onNameChange,
                )
            }
        })
}


@Preview
@Composable
fun PreviewDialog() {
    var name by remember { mutableStateOf("123") }
    GroupInfoEditDialog(name, { name = it }, {}, {})
}

