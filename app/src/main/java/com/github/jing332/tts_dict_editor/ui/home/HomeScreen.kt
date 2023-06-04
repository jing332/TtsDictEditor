@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.jing332.tts_dict_editor.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.jing332.tts_dict_editor.R
import com.github.jing332.tts_dict_editor.data.appDb
import com.github.jing332.tts_dict_editor.data.entites.DictFile
import com.github.jing332.tts_dict_editor.ui.AppNavRoutes
import com.github.jing332.tts_dict_editor.ui.LocalNavController
import com.github.jing332.tts_dict_editor.ui.LocalSnackbarHostState
import com.github.jing332.tts_dict_editor.ui.navigateSingleTop
import com.github.jing332.tts_dict_editor.ui.replace.ReplaceRuleActivity
import com.github.jing332.tts_dict_editor.utils.ASFUriUtils.getPath
import com.github.jing332.tts_dict_editor.utils.AndroidUtils.requestDesktopShortcut
import kotlinx.coroutines.launch
import me.saket.cascade.CascadeDropdownMenu
import me.saket.cascade.rememberCascadeState

@Composable
internal fun HomeScreen(drawerState: DrawerState) {
    val scope = rememberCoroutineScope()
    val navController = LocalNavController.current

    val dictFileEdit = { dictFile: DictFile ->
        navController.navigateSingleTop(AppNavRoutes.DictFileEdit.route, args = Bundle().apply {
            putParcelable(AppNavRoutes.DictFileEdit.KEY_DICT_FILE, dictFile)
        })
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = LocalSnackbarHostState.current) },
        topBar = {
            TopAppBar(modifier = Modifier.fillMaxWidth(),
                navigationIcon = {
                    IconButton(onClick = {
                        scope.launch { drawerState.open() }
                    }) {
                        Icon(Icons.Filled.Menu, stringResource(id = R.string.open_drawer_menu))
                    }
                },
                title = { Text(text = stringResource(id = R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = {
                        dictFileEdit.invoke(DictFile())
                    }) {
                        Icon(Icons.Filled.Add, stringResource(id = R.string.add))
                    }

                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Filled.MoreVert,
                            stringResource(id = R.string.more_options)
                        )
                    }
                })
        }, content = { pad ->
            Surface(modifier = Modifier.padding(pad)) {
                DictFileManagerScreen(onEdit = {
                    dictFileEdit.invoke(it)
                })
            }
        }
    )
}

@Composable
private fun DictFileManagerScreen(
    vm: HomeScreenViewModel = viewModel(),
    onEdit: (DictFile) -> Unit
) {
    val context = LocalContext.current
    val models by vm.dictFilesFlow.collectAsState(initial = listOf())
    LazyColumn {
        items(models, key = { it.id }) {
            DictFileItem(
                it.copy(
                    filePath = try {
                        context.getPath(Uri.parse(it.filePath))
                    } catch (_: Exception) {
                        null
                    } ?: it.filePath,
                ), onReplaceRuleEdit = {
                    context.startActivity(
                        Intent(context, ReplaceRuleActivity::class.java).apply {
                            data = Uri.parse(it.filePath)
                            putExtra("name", it.name)
                        })
                }, onEdit = {
                    onEdit.invoke(it)
                }, onDelete = {
                    appDb.dictFileDao.delete(it)
                },
                onDesktopShortcut = {
                    context.requestDesktopShortcut(
                        it.name,
                        it.id.toString(),
                        R.mipmap.ic_new_launcher_round,
                        Intent(context, ReplaceRuleActivity::class.java).apply {
                            data = Uri.parse(it.filePath)
                            putExtra("name", it.name)
                        }
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DictFileItem(
    dictFile: DictFile,
    onReplaceRuleEdit: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDesktopShortcut: () -> Unit,
) {
    Card(
        onClick = onReplaceRuleEdit,
        colors = CardDefaults.elevatedCardColors(),
        elevation = CardDefaults.elevatedCardElevation(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp)
            .clickable { onReplaceRuleEdit.invoke() },
    ) {
        ConstraintLayout(
            Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            val (txtName, txtInfo, btnEdit, btnMore) = createRefs()
            Text(text = dictFile.name,
                textAlign = TextAlign.Start,
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.constrainAs(txtName) {
                    start.linkTo(parent.start)
                    end.linkTo(btnEdit.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(txtInfo.top)
                })
            Text(text = dictFile.filePath,
                textAlign = TextAlign.Start,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.constrainAs(txtInfo) {
                    start.linkTo(parent.start)
                    top.linkTo(txtName.bottom)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(btnEdit.start, margin = 8.dp)
                    width = Dimension.fillToConstraints
                })


            IconButton(modifier = Modifier.constrainAs(btnEdit) {
                end.linkTo(btnMore.start)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            }, onClick = {
                onEdit.invoke()
            }) {
                Icon(
                    Icons.Filled.Settings,
                    stringResource(R.string.desc_settings_dict_file, dictFile.name)
                )
            }

            var isMoreOptionsVisible by rememberSaveable { mutableStateOf(false) }

            IconButton(onClick = {
                isMoreOptionsVisible = true
            }, modifier = Modifier.constrainAs(btnMore) {
                end.linkTo(parent.end)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            }) {
                Icon(Icons.Filled.MoreVert, stringResource(R.string.more_options))

                val menuState = rememberCascadeState()
                CascadeDropdownMenu(state = menuState,
                    expanded = isMoreOptionsVisible,
                    onDismissRequest = { isMoreOptionsVisible = false }) {
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(stringResource(R.string.desktop_shortcut)) },
                        onClick = {
                            isMoreOptionsVisible = false
                            onDesktopShortcut.invoke()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.AddBusiness,
                                stringResource(R.string.desktop_shortcut),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        })

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

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
