package com.github.jing332.tts_dict_editor.ui.filepicker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.github.jing332.tts_dict_editor.R
import com.github.jing332.tts_dict_editor.ui.Widgets
import com.github.jing332.tts_dict_editor.ui.theme.AppTheme
import com.github.jing332.tts_server_android.util.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FilePickerActivity : ComponentActivity() {
    private val vm: FilePickerViewModel by viewModels()

    private fun checkFileReadPermission() {
        val permission = ActivityCompat.checkSelfPermission(
            this, Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (permission != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 1
            )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                isShowPermissionDialog.value = true
            }
        }
    }

    private val isShowPermissionDialog = mutableStateOf(false)

    @Composable
    fun filePermissionDialog() {
        if (isShowPermissionDialog.value) {
            AlertDialog(
                onDismissRequest = { isShowPermissionDialog.value = false },
                confirmButton = {
                    OutlinedButton(
                        onClick = {
                            startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                data = Uri.parse("package:$packageName")
                            })
                        }) {
                        Text(text = "前往设置")
                    }
                },
                text = {
                    Text(text = "请授予【所有文件访问权限】")
                },
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        checkFileReadPermission()

        setContent {
            AppTheme {
                Widgets.TransparentSystemBars()
                Scaffold(
                    topBar = {
                        TopAppBar(
                            modifier = Modifier.fillMaxWidth(),
                            title = {
                                Text(
                                    text = vm.currentPath.value,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                            actions = {
                                IconButton(onClick = {
                                }) {
                                    Icon(Icons.Filled.MoreVert, "add")
                                }
                            }
                        )
                    },
                    content = { pad ->
                        Surface(modifier = Modifier.padding(pad)) {
                            filePermissionDialog()
                            pickerScreen(vm)
                        }
                    }

                )
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            vm.updateModels(Environment.getExternalStorageDirectory().absolutePath)
        }
    }

    @Composable
    fun pickerScreen(vm: FilePickerViewModel) {
        val models = remember { vm.models }
        Column {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(
                    count = models.size, key = {
                        models[it].id
                    }, itemContent = { pos ->
                        val model = models[pos]

                        itemWidget(title = model.name,
                            subTitle = "${model.fileCount}个文件，${model.folderCount}个文件夹",
                            isCheckable = model.isCheckable,
                            isChecked = model.isChecked,
                            isDirectory = model.isDirectory,
                            onClickItem = {
                                if (model.isDirectory) vm.enterDir(model)
                                else {
                                    vm.updateModelsSelected(model, true)
                                }
                            },
                            onCheckedChanged = { vm.updateModelsSelected(model, it) }
                        )
                    }
                )
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                onClick = { toast("qwq") },
                enabled = vm.hasSelectedModels.value
            ) {
                Text(text = "OK")
            }
        }
    }

    @Composable
    fun itemWidget(
        title: String,
        subTitle: String,
        isCheckable: Boolean,
        isChecked: Boolean,
        isDirectory: Boolean,
        onClickItem: () -> Unit,
        onCheckedChanged: (Boolean) -> Unit
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(8))
                .clickable {
                    onClickItem.invoke()
                }
        ) {
            val (textTitle, textSubtitle, imgType, radioSelected) = createRefs()
            Image(
                painter = painterResource(id = if (isDirectory) R.drawable.baseline_folder_24 else R.drawable.baseline_insert_drive_file_24),
                contentDescription = "类型",
                modifier = Modifier
                    .constrainAs(imgType) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
                    .size(24.dp)
            )
            Text(
                text = title,
                modifier = Modifier.constrainAs(textTitle) {
                    start.linkTo(imgType.end, margin = 8.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(textSubtitle.top)
                },
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = subTitle,
                modifier = Modifier.constrainAs(textSubtitle) {
                    start.linkTo(imgType.end, margin = 8.dp)
                    top.linkTo(textTitle.bottom)
                    bottom.linkTo(parent.bottom)
                },
                style = MaterialTheme.typography.bodySmall
            )

            if (isCheckable)
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = {
                        onCheckedChanged(it)
                    },
                    modifier = Modifier.constrainAs(radioSelected) {
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
                )
        }
    }

}

