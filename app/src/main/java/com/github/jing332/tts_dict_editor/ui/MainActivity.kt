package com.github.jing332.tts_dict_editor.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.view.WindowCompat
import com.github.jing332.tts_dict_editor.R
import com.github.jing332.tts_dict_editor.data.appDb
import com.github.jing332.tts_dict_editor.data.entites.DictFile
import com.github.jing332.tts_dict_editor.ui.Widgets.TransparentSystemBars
import com.github.jing332.tts_dict_editor.ui.theme.AppTheme
import me.saket.cascade.CascadeDropdownMenu
import me.saket.cascade.rememberCascadeState

class MainActivity : ComponentActivity() {
    private val vm: MainActivityViewModel by viewModels()

    private val mDictFileActivityLauncher = registerForActivityResult(
        AppActivityResultContracts.parcelableDataActivity<DictFile>(DictFileEditActivity::class.java)
    ) {
        it?.let { dictFile ->
            appDb.dictFileDao.insert(dictFile)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            AppTheme {
                TransparentSystemBars()
                Scaffold(
                    topBar = {
                        TopAppBar(
                            modifier = Modifier.fillMaxWidth(),
                            title = { Text(text = stringResource(id = R.string.app_name)) },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            actions = {
                                IconButton(onClick = {
                                    mDictFileActivityLauncher.launch(DictFile())
                                }) {
                                    Icon(Icons.Filled.Add, stringResource(id = R.string.add))
                                }

                                IconButton(onClick = {

                                }) {
                                    Icon(
                                        Icons.Filled.MoreVert,
                                        stringResource(id = R.string.more_options)
                                    )
                                }
                            }
                        )
                    },
                    content = { pad ->
                        Surface(modifier = Modifier.padding(pad)) {
                            dictFilesScreen()
                        }
                    }

                )
            }
        }
    }


    @Composable
    fun dictFilesScreen() {
        val models = vm.dictFilesFlow.collectAsState(initial = listOf())
        LazyColumn {
            items(models.value.toTypedArray(), key = { it.id }) {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    ConstraintLayout(Modifier.fillMaxWidth()) {
                        val (txtName, txtInfo, btnEdit, btnMore) = createRefs()
                        Text(text = it.name,
                            textAlign = TextAlign.Start,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.constrainAs(txtName) {
                                start.linkTo(parent.start)
                                end.linkTo(btnEdit.start)
                                top.linkTo(parent.top)
                                bottom.linkTo(txtInfo.top)
                            }
                        )
                        Text(
                            text = "it.filePath",
                            textAlign = TextAlign.Left,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.constrainAs(txtInfo) {
                                start.linkTo(parent.start)
                                top.linkTo(txtName.bottom)
                                bottom.linkTo(parent.bottom)
                            })


                        IconButton(
                            modifier = Modifier.constrainAs(btnEdit) {
                                end.linkTo(btnMore.start)
                                top.linkTo(parent.top)
                                bottom.linkTo(parent.bottom)
                            },
                            onClick = {
                                mDictFileActivityLauncher.launch(it)
                            }
                        ) {
                            Icon(Icons.Filled.Edit, stringResource(R.string.desc_edit, it.name))
                        }

                        var isMoreOptionsVisible by rememberSaveable { mutableStateOf(false) }
                        val menuState = rememberCascadeState()
                        CascadeDropdownMenu(
                            state = menuState,
                            expanded = isMoreOptionsVisible,
                            onDismissRequest = { isMoreOptionsVisible = false }) {

                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.delete)) },
                                children = {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.confirm_deletion)) },
                                        children = {})
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.cancel)) },
                                        children = {},
                                        trailingIcon = {
                                            Icon(
                                                Icons.Filled.Delete,
                                                stringResource(R.string.delete),
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        },
                                        modifier = Modifier.clickable {
                                            menuState.resetBackStack()
                                        })
                                })
                        }
//                        AnimatedContent(targetState = ) {
//
//                        }
                        IconButton(
                            onClick = {
                                isMoreOptionsVisible = true
                            },
                            modifier = Modifier.constrainAs(btnMore) {
                                end.linkTo(parent.end)
                                top.linkTo(parent.top)
                                bottom.linkTo(parent.bottom)
                            }
                        ) {
                            Icon(Icons.Filled.MoreVert, stringResource(R.string.more_options))
                        }

                    }
                }
            }
        }
    }
}


/*
@Composable
fun widget(vm: MainActivityViewModel, modifier: Modifier = Modifier) {
    val models = vm.getModels().observeAsState()
    LazyColumn {
        items(count = models.value?.size ?: 0, key = {
            models.value!![it].id
        }, itemContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    text = models.value!![it].name,
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.titleSmall
                )

                itemInGroup(models.value!![it].list, modifier)
            }
        }
        )
    }
}

@Composable
fun itemInGroup(list: List<DictFileItemModel>, modifier: Modifier) {
    Column(modifier = modifier) {
        list.forEach {
            Text(
                text = it.name,
                modifier = Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}*/
