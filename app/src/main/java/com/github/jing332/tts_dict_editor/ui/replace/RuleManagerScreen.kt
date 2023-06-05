@file:OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class
)

package com.github.jing332.tts_dict_editor.ui.replace

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.jing332.tts_dict_editor.R
import com.github.jing332.tts_dict_editor.const.AppConst
import com.github.jing332.tts_dict_editor.help.AppConfig
import com.github.jing332.tts_dict_editor.help.GroupWithReplaceRule
import com.github.jing332.tts_dict_editor.help.ReplaceRule
import com.github.jing332.tts_dict_editor.help.ReplaceRuleGroup
import com.github.jing332.tts_dict_editor.ui.widget.ErrorDialog
import com.talhafaki.composablesweettoast.util.SweetToastUtil
import kotlinx.coroutines.launch
import me.saket.cascade.CascadeDropdownMenu
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

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
    var exportedConfigText by remember { mutableStateOf("") }
    // 导出配置  Pair<isVisible, ReplaceRuleGroup>
    var isVisibleCustomFormatExport by remember {
        mutableStateOf<Pair<Boolean, ReplaceRuleGroup?>>(
            false to null
        )
    }

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

                            ConfigExportUiMenu(
                                onJson = {
                                    isMoreOptionsVisible = false
                                    coroutineScope.launch {
                                        exportedConfigText = vm.export()
                                    }
                                },
                                onCustomFormat = {
                                    isMoreOptionsVisible = false
                                    isVisibleCustomFormatExport = true to null
                                }
                            )
                        }
                    }
                }
            )
        },
        content = { pad ->
            Surface(modifier = Modifier.padding(pad)) {
                var groups by remember { mutableStateOf(emptyList<ReplaceRuleGroup>()) }
                if (vm.list.size > 0) // list变化时重新过滤出分组
                    groups = vm.list.filterIsInstance<ReplaceRuleGroup>()

                Screen(
                    list = vm.list,
                    groups = groups,
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
                    onReorder = { from, to -> vm.reorder(from, to) },
                    onExportGroup = { group, isCustomFormat ->
                        if (isCustomFormat)
                            isVisibleCustomFormatExport = true to group
                        else
                            coroutineScope.launch {
                                exportedConfigText =
                                    com.drake.net.utils.withDefault { vm.exportGroup(group) }
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

    if (exportedConfigText.isNotEmpty()) {
        val fileName =
            if (exportedConfigText.contains("\"group\":") && exportedConfigText.contains("\"list\":")) "replaceRules.json" else "dict.txt"
        ConfigExportBottomSheet(json = exportedConfigText, fileName) {
            exportedConfigText = ""
        }
    }

    if (isVisibleCustomFormatExport.first) {
        var format by remember { AppConfig.dictExportFormat }
        CustomExportFormatDialog(
            format = format,
            onFormatChange = { format = it },
            onDismissRequest = {
                isVisibleCustomFormatExport = false to null
            },
            onConfirm = {
                isVisibleCustomFormatExport = false to null
                coroutineScope.launch {
                    exportedConfigText =
                        if (isVisibleCustomFormatExport.second == null) vm.exportByFormat(format)
                        else vm.exportGroupByFormat(isVisibleCustomFormatExport.second!!, format)
                }
            }
        )
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
    groups: List<ReplaceRuleGroup>,
    onEnabledChange: (ReplaceRule) -> Unit,
    onEditGroup: (ReplaceRuleGroup) -> Unit,
    onDeleteGroup: (ReplaceRuleGroup) -> Unit,
    onChangeExpanded: (ReplaceRuleGroup) -> Unit,
    onEditRule: (ReplaceRule) -> Unit,
    onDeleteRule: (ReplaceRule) -> Unit,
    onReorder: (fromIndex: Int, toIndex: Int) -> Unit,
    onExportGroup: (ReplaceRuleGroup, Boolean) -> Unit,
) {
    // 保存展开的分组ID 提高效率 避免每次item都要去list中查找
//        var expandedGroups by remember { mutableStateOf<List<Long>>(emptyList()) }
    val orderState = rememberReorderableLazyListState(onMove = { from, to ->
        println("from $from to $to")
        onReorder.invoke(from.index, to.index)
    })
    LazyColumn(
        state = orderState.listState,
        modifier = Modifier
            .fillMaxSize()
            .reorderable(orderState)
            .detectReorderAfterLongPress(orderState)
    ) {
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
                            onExportAction = { onExportGroup.invoke(item, it) },
                        )
                    }
                }

                is ReplaceRule -> {
                    val key = "${item.groupId}_${item.id}"
                    item(key = key) {
                        if (groups.find { it.id == item.groupId }?.isExpanded == true) {
                            ReorderableItem(state = orderState, key = key) { isDragging ->
                                val elevation = animateDpAsState(
                                    if (isDragging) 2.dp else 0.dp,
                                    label = "拖动"
                                )
                                ReplaceRuleItem(
                                    item.name.ifBlank { "${item.pattern} => ${item.replacement}" },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                        .animateItemPlacement()
                                        .shadow(
                                            elevation.value,
                                            shape = MaterialTheme.shapes.small
                                        ),
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

@Preview
@Composable
fun PreviewOrderList() {
//    VerticalReorderList()
//    return
    val orderState = rememberReorderableLazyListState(onMove = { from, to ->
        println("from $from to $to")
    })
    val list = listOf("1", "2", "3", "4")
    LazyColumn(
        state = orderState.listState,
        modifier = Modifier
            .fillMaxSize()
            .reorderable(orderState)
            .detectReorderAfterLongPress(orderState),
    ) {
        items(list, key = { it }) {
            ReorderableItem(state = orderState, key = it) { isDraging ->
                Text(
                    text = it,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}

@Composable
fun VerticalReorderList() {
    val data = remember { mutableStateOf(List(100) { "Item $it" }) }
    val state = rememberReorderableLazyListState(onMove = { from, to ->
        data.value = data.value.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    })
    LazyColumn(
        state = state.listState,
        modifier = Modifier
            .reorderable(state)
            .detectReorderAfterLongPress(state)
    ) {
        items(data.value, { it }) { item ->
            ReorderableItem(state, key = item) { isDragging ->
                val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp)
                Column(
                    modifier = Modifier
                        .shadow(elevation.value)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Text(item)
                }
            }
        }
    }
}
