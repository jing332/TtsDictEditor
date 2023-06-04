@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.jing332.tts_dict_editor.ui.replace

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.jing332.tts_dict_editor.R
import com.github.jing332.tts_dict_editor.const.AppConst
import com.github.jing332.tts_dict_editor.help.GroupWithReplaceRule
import com.github.jing332.tts_dict_editor.help.ReplaceRule
import com.github.jing332.tts_dict_editor.help.ReplaceRuleGroup
import com.github.jing332.tts_dict_editor.ui.AppActivityResultContracts
import com.github.jing332.tts_dict_editor.ui.widget.ErrorDialog
import com.talhafaki.composablesweettoast.util.SweetToastUtil
import kotlinx.coroutines.launch
import me.saket.cascade.CascadeDropdownMenu

@Composable
fun ReplaceRuleManagerScreen(
    title: String,
    subTitle: String,
    onFinishedActivity: () -> Unit,
    onEditRule: (ReplaceRule) -> Unit,

    vm: RuleManagerViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isVisibleImportConfig by remember { mutableStateOf(false) }
    var exportConfigJson by remember { mutableStateOf("") }

    var successToast by remember { mutableStateOf<String?>(null) }
    if (successToast != null)
        SweetToastUtil.SweetSuccess(successToast ?: "")

    var errDialog by remember { mutableStateOf<Pair<String, Throwable>?>(null) }

    /* 错误对话框 */
    if (errDialog != null)
        ErrorDialog(
            title = errDialog?.first ?: "Error",
            t = errDialog?.second,
            onDismiss = { errDialog = null }
        )

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
                navigationIcon = {
                    IconButton(onClick = { onFinishedActivity.invoke() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                },
                title = {
                    Column {
                        Text(
                            text = title,
                            maxLines = 1,
                            style = MaterialTheme.typography.titleMedium,
                        )

                        Text(
                            text = subTitle,
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
                                    isVisibleAddMenu = false
                                    onEditRule.invoke(ReplaceRule())
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
                                text = { Text(stringResource(R.string.config_import)) },
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
                                text = { Text(stringResource(R.string.config_export)) },
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
                    list = vm.list,
                    onEnabledChange = {
                        coroutineScope.launch {
                            vm.updateOrAddRule(it.copy(isEnabled = !it.isEnabled))
                        }
                    },
                    onChangeExpanded = { vm.updateOrAddGroup(it.copy(isExpanded = !it.isExpanded)) },
                    onEditGroup = { groupInfoEdit = it },
                    onDeleteGroup = { vm.deleteGroup(it) },
                    onEditRule = { onEditRule.invoke(it) },
                    onDeleteRule = { coroutineScope.launch { vm.deleteRule(it) } },
                    onExportGroup = {
                        coroutineScope.launch {
                            exportConfigJson =
                                com.drake.net.utils.withDefault { vm.exportGroup(it) }
                        }
                    },
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
                    errDialog = context.getString(R.string.failed_to_import) to e
                    return@ImportConfigDialog
                }
                isVisibleImportConfig = false
                successToast = context.getString(R.string.import_success_msg, count)
            }
        )

    if (exportConfigJson.isNotEmpty()) {
        ConfigExportBottomSheet(json = exportConfigJson) {
            exportConfigJson = ""
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
    list: List<Any>,
    onEnabledChange: (ReplaceRule) -> Unit,
    onEditGroup: (ReplaceRuleGroup) -> Unit,
    onDeleteGroup: (ReplaceRuleGroup) -> Unit,
    onChangeExpanded: (ReplaceRuleGroup) -> Unit,
    onEditRule: (ReplaceRule) -> Unit,
    onDeleteRule: (ReplaceRule) -> Unit,

    onExportGroup: (ReplaceRuleGroup) -> Unit,
) {
    // 保存展开的分组ID 提高效率 避免每次item都要去list中查找
//        var expandedGroups by remember { mutableStateOf<List<Long>>(emptyList()) }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        val groups = list.filterIsInstance<ReplaceRuleGroup>()
        list.forEachIndexed { index, item ->
            when (item) {
                is ReplaceRuleGroup -> {
                    stickyHeader(key = "group_${item.id}") {
                        GroupItem(
                            modifier = Modifier.animateItemPlacement(),
                            name = item.name,
                            isExpanded = item.isExpanded,
                            onClick = { onChangeExpanded.invoke(item) },
                            onEdit = { onEditGroup.invoke(item) },
                            onDeleteAction = { onDeleteGroup.invoke(item) },
                            onImportAction = { onExportGroup.invoke(item) },
                        )
                    }
                }

                is ReplaceRule -> {
                    item(key = "${item.groupId}_${item.id}") {
                        if (groups.find { it.id == item.groupId && it.isExpanded } != null) {
                            ReplaceRuleItem(
                                item.name.ifBlank { "${item.pattern} => ${item.replacement}" },
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