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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.github.jing332.tts_dict_editor.R
import com.github.jing332.tts_dict_editor.const.AppConst
import com.github.jing332.tts_dict_editor.help.GroupWithReplaceRule
import com.github.jing332.tts_dict_editor.help.ReplaceRule
import com.github.jing332.tts_dict_editor.help.ReplaceRuleGroup
import com.github.jing332.tts_dict_editor.ui.AppActivityResultContracts
import com.github.jing332.tts_dict_editor.ui.widget.ErrorDialog
import com.github.jing332.tts_dict_editor.ui.widget.Widgets
import com.github.jing332.tts_dict_editor.ui.theme.AppTheme
import com.github.jing332.tts_dict_editor.utils.observeNoSticky
import com.github.jing332.tts_server_android.util.longToast
import com.github.jing332.tts_server_android.utils.ASFUriUtils.getPath
import com.talhafaki.composablesweettoast.util.SweetToastUtil
import kotlinx.coroutines.launch
import me.saket.cascade.CascadeDropdownMenu
import me.saket.cascade.rememberCascadeState

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
        var exportConfigJson by mutableStateOf("")
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

                var successToast by remember { mutableStateOf<String?>(null) }
                if (successToast != null)
                    SweetToastUtil.SweetSuccess(successToast ?: "")

                val coroutineScope = rememberCoroutineScope()

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
                        it?.let { rule ->
                            coroutineScope.launch { vm.updateOrAddRule(rule) }
                        }
                    }

                fun launchEditRule(rule: ReplaceRule): Unit {
                    launcherForEditRuleActivity.launch(vm.groups() to rule)
                }

                /* 编辑分组对话框 */
                var groupInfoEdit by remember { mutableStateOf<ReplaceRuleGroup?>(null) }
                if (groupInfoEdit != null) {
                    var name by remember { mutableStateOf(groupInfoEdit?.name ?: "") }
                    GroupInfoEditDialog(
                        name = name,
                        onConfirm = {
                            vm.updateOrAddGroup(groupInfoEdit!!.copy(name = name))
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
                                                    Icons.Filled.Input, "",
                                                    tint = MaterialTheme.colorScheme.onBackground
                                                )
                                            },
                                            onClick = {
                                                isMoreOptionsVisible = false
                                                isVisibleImportConfig = true
                                            }
                                        )
                                        androidx.compose.material3.DropdownMenuItem(
                                            text = { Text(getString(R.string.export_config)) },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Filled.Output, "",
                                                    tint = MaterialTheme.colorScheme.onBackground
                                                )
                                            },
                                            onClick = {
                                                isMoreOptionsVisible = false
                                                coroutineScope.launch {
                                                    exportConfigJson =
                                                        com.drake.net.utils.withDefault { vm.export() }
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        )
                    },
                    content = { pad ->
                        Surface(modifier = Modifier.padding(pad)) {
                            Screen(
                                onEnabledChange = {
                                    coroutineScope.launch {
                                        vm.updateOrAddRule(it.copy(isEnabled = !it.isEnabled))
                                    }
                                },
                                onEditGroup = { groupInfoEdit = it },
                                onEditRule = { launchEditRule(it) },
                                onDeleteRule = { coroutineScope.launch { vm.deleteRule(it) } },
                            )
                        }
                    })

                if (isVisibleImportConfig)
                    ImportConfigDialog(
                        onDismissRequest = { isVisibleImportConfig = false },
                        onImport = {
                            val count = try {
                                vm.import(it)
                            } catch (e: Exception) {
                                errDialog = getString(R.string.failed_to_import) to e
                                return@ImportConfigDialog
                            }
                            isVisibleImportConfig = false
                            successToast = getString(R.string.import_success_msg, count)
                        }
                    )

                if (exportConfigJson.isNotEmpty()) {
                    ConfigExportBottomSheet(json = exportConfigJson) {
                        exportConfigJson = ""
                    }
                }
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
    private fun ImportConfigDialog(
        onDismissRequest: () -> Unit,
        onImport: (List<GroupWithReplaceRule>) -> Unit
    ) {
        val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var selectDialogData by remember {
            mutableStateOf<List<GroupWithReplaceRule>?>(null)
        }
        ConfigImportBottomSheet(onImportFromJson = {
            selectDialogData = AppConst.json.decodeFromString<List<GroupWithReplaceRule>>(it)
        }, onDismiss = onDismissRequest)
        if (selectDialogData != null)
            ConfigImportSelectDialog(groupWithRules = selectDialogData ?: emptyList(),
                onConfirm = onImport,
                onDismissRequest = {
                    selectDialogData = null
                }
            )
    }


    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun Screen(
        onEnabledChange: (ReplaceRule) -> Unit,
        onEditGroup: (ReplaceRuleGroup) -> Unit,
        onEditRule: (ReplaceRule) -> Unit,
        onDeleteRule: (ReplaceRule) -> Unit,
    ) {
        val list = vm.list
        val expandedGroups = remember {
            mutableStateListOf(*list.filterIsInstance<ReplaceRuleGroup>().toTypedArray())
        }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            list.forEachIndexed { index, item ->
                when (item) {
                    is ReplaceRuleGroup -> {
                        stickyHeader(key = "group_${item.id}") {
                            GroupItem(
                                name = item.name,
                                isExpanded = expandedGroups.indexOfFirst { it.id == item.id } > -1,
                                onClick = {
                                    if (expandedGroups.indexOfFirst { it.id == item.id } > -1)
                                        expandedGroups.remove(item)
                                    else
                                        expandedGroups.add(item)
                                },
                                onEdit = { onEditGroup.invoke(item) },
                                onDeleteAction = { vm.deleteGroup(item) }
                            )
                        }
                    }

                    is ReplaceRule -> {
                        item(key = "${item.groupId}_${item.id}") {
                            if (expandedGroups.indexOfFirst { it.id == item.groupId } > -1)
                                ReplaceRuleItem(
                                    item.name.ifBlank { "${item.pattern} -> ${item.replacement}" },
                                    modifier = Modifier.animateItemPlacement(),
                                    onDelete = { onDeleteRule.invoke(item) },
                                    onClick = { onEditRule.invoke(item) },
                                    isChecked = item.isEnabled,
                                    onCheckedChange = { onEnabledChange.invoke(item) }
                                )
                        }
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

