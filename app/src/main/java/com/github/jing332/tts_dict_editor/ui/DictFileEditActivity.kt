package com.github.jing332.tts_dict_editor.ui

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.github.jing332.tts_dict_editor.R
import com.github.jing332.tts_dict_editor.const.IntentKeys
import com.github.jing332.tts_dict_editor.data.entites.DictFile
import com.github.jing332.tts_dict_editor.ui.theme.AppTheme

@Suppress("DEPRECATION")
class DictFileEditActivity : ComponentActivity() {
    private val vm: DictFileEditViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val dictFile: DictFile = intent.getParcelableExtra(IntentKeys.KEY_DATA) ?: return
        vm.init(dictFile)

        setContent {
            AppTheme {
                Widgets.TransparentSystemBars()
                Scaffold(
                    topBar = {
                        TopAppBar(
                            modifier = Modifier.fillMaxWidth(),
                            title = { Text(text = "编辑词典文件") },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            navigationIcon = {
                                IconButton(onClick = {
                                    finishAfterTransition()
                                }) {
                                    Icon(
                                        Icons.Filled.ArrowBack,
                                        getString(R.string.back)
                                    )
                                }
                            },
                            actions = {
                                IconButton(onClick = {
                                    setResult(RESULT_OK, Intent().apply {
                                        putExtra(
                                            IntentKeys.KEY_DATA,
                                            vm.getDictFile(getString(R.string.unnamed))
                                        )
                                    })
                                    finishAfterTransition()
                                }) {
                                    Icon(
                                        painterResource(id = R.drawable.ic_baseline_save_24),
                                        stringResource(id = R.string.save)
                                    )
                                }

                                /*IconButton(onClick = {}) {
                                    Icon(
                                        Icons.Filled.MoreVert,
                                        stringResource(id = R.string.more_options)
                                    )
                                }*/
                            }
                        )
                    },
                    content = { pad ->
                        Surface(modifier = Modifier.padding(pad)) {
                            screen()
                        }
                    }
                )
            }
        }
    }

    private val filepicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        it?.let { uri ->
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )

            vm.updateFilePath(uri.toString())
        }
    }

    @Composable
    fun screen() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            val name by vm.nameState
            OutlinedTextField(
                value = name, label = { Text("名称") }, onValueChange = {
                    vm.updateName(it)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.padding(8.dp))

            val path by vm.filePathState
            OutlinedTextField(
                value = path, label = { Text("文件路径") }, onValueChange = {
                    vm.updateFilePath(it)
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            filepicker.launch(
                                arrayOf(
                                    "${Environment.getExternalStorageDirectory()}Android/data",
                                    "text/*"
                                )
                            )
                        },
                    ) {
                        Icon(
                            Icons.Filled.FileOpen,
                            contentDescription = stringResource(R.string.select_file)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

        }

    }


}