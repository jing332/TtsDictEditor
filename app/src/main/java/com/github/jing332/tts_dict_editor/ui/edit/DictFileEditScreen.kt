@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.github.jing332.tts_dict_editor.ui.edit

import android.content.Intent
import android.os.Environment
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FileOpen
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.jing332.tts_dict_editor.R
import com.github.jing332.tts_dict_editor.data.appDb
import com.github.jing332.tts_dict_editor.data.entites.DictFile
import com.github.jing332.tts_dict_editor.ui.LocalNavController
import com.github.jing332.tts_dict_editor.utils.ASFUriUtils.getPath
import com.github.jing332.tts_dict_editor.utils.FileUriTools.toContentUri
import me.saket.cascade.CascadeDropdownMenu

@Composable
fun DictFileEditScreen(
    dictFile: DictFile,
    onResult: (DictFile?) -> Unit = {
        it?.let { dictFile ->
            println(dictFile)
            appDb.dictFileDao.insert(dictFile)
        }
    },
    vm: DictFileEditViewModel = viewModel()
) {
    val navController = LocalNavController.current
    LaunchedEffect(key1 = dictFile, block = {
        vm.init(dictFile)
    })

    val context = LocalContext.current
    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                modifier = Modifier.fillMaxWidth(),
                title = { Text(text = stringResource(R.string.set_dict_file)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        onResult.invoke(null)
                        navController.popBackStack()
                    }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            stringResource(id = R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        onResult.invoke(vm.getDictFile(context.getString(R.string.unnamed)))
                        navController.popBackStack()
                    }) {
                        Icon(
                            painterResource(id = R.drawable.ic_baseline_save_24),
                            stringResource(id = R.string.save)
                        )
                    }
                }
            )
        },
        content = { pad ->
            Surface(modifier = Modifier.padding(pad)) {
                Screen(vm)
            }
        }
    )
}


@Composable
private fun Screen(vm: DictFileEditViewModel) {
    var filePickerUri by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    val context = LocalContext.current
    if (filePickerUri != null) {
        val uriStr = filePickerUri?.second ?: ""
        val isA13Mode = filePickerUri?.first ?: false
        FilePathPicker(
            uriString = uriStr,
            isA13Mode = isA13Mode,
            mimeTypes = listOf("text/*"),
            onResult = {
                filePickerUri = null
                it?.let { uri ->
                    kotlin.runCatching {
                        context.contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                    }.onSuccess {
                        vm.updateFileUri(uri.toString())
                    }.onFailure {

                    }
                }
            }
        )
    }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        val name by vm.nameState
        OutlinedTextField(
            value = name,
            label = { Text(stringResource(id = R.string.name)) },
            onValueChange = {
                vm.updateName(it)
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.padding(8.dp))

        val path by vm.filePathState
        OutlinedTextField(
            value = try {
                context.getPath(path.toUri())
            } catch (_: Exception) {
                null
            } ?: path,
            label = { Text(stringResource(R.string.file_path)) },
            onValueChange = {},
            trailingIcon = {
                var isVisibleMenu by remember { mutableStateOf(false) }
                IconButton(
                    onClick = {
                        isVisibleMenu = true
                    },
                ) {
                    Icon(
                        Icons.Filled.FileOpen,
                        contentDescription = stringResource(R.string.select_file)
                    )

                    CascadeDropdownMenu(
                        expanded = isVisibleMenu,
                        onDismissRequest = { isVisibleMenu = false }) {

                        Text(
                            stringResource(R.string.select_file),
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(4.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(stringResource(R.string.internal_storage)) },
                            onClick = {
                                isVisibleMenu = false
                                filePickerUri =
                                    false to "/".toContentUri(false).toString()
                            }
                        )
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text("Android/data") },
                            onClick = {
                                isVisibleMenu = false
                                filePickerUri =
                                    false to (Environment.getExternalStorageDirectory().absolutePath + "/Android/data").toContentUri(
                                        isTree = false // Android/data 必须 isTree = false
                                    ).toString()
                            }
                        )

                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text("Android/data (安卓13)") },
                            onClick = {
                                isVisibleMenu = false
                                filePickerUri =
                                    true to (Environment.getExternalStorageDirectory().absolutePath + "/Android/data").toContentUri(
                                        isTree = false // Android/data 必须 isTree = false
                                    ).toString()
                            }
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}