package com.github.jing332.tts_dict_editor.ui.filepicker

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.documentfile.provider.DocumentFile
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import com.github.jing332.tts_dict_editor.ui.Widgets
import com.github.jing332.tts_dict_editor.ui.theme.AppTheme
import com.github.jing332.tts_dict_editor.utils.FilePermissionTools
import com.github.jing332.tts_dict_editor.utils.FileTools
import com.github.jing332.tts_dict_editor.utils.FileUriTools.toContentUri
import com.github.jing332.tts_server_android.util.longToast
import com.github.jing332.tts_server_android.util.toast
import java.io.File
import java.net.URLDecoder
import java.net.URLEncoder


class FilePickerActivity : ComponentActivity() {
    companion object {
        const val TAG = "FilePickerActivity"
    }

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

    @SuppressLint("WrongConstant")
    private val mFilePicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            it?.data?.let { intent ->
                val fileDoc = DocumentFile.fromTreeUri(this, intent.data!!)
                this.longToast(fileDoc?.uri.toString())
                contentResolver.takePersistableUriPermission(
                    intent.data!!,
                    intent.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                )
            }
        }

    private fun requestAndroidDataPermission() {
        mFilePicker.launch(FilePermissionTools.requestPermissionAndroidDataIntent)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        checkFileReadPermission()

        setContent {
            AppTheme {
                Widgets.TransparentSystemBars()
                var title by remember { mutableStateOf("") }
                Scaffold(
                    topBar = {
                        TopAppBar(
                            modifier = Modifier.fillMaxWidth(),
                            title = {
                                Text(
                                    text = title,
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
                            pickerScreen("/storage/emulated/0/Android/data/bin.mt.plus"/*FileTools.PATH_EXTERNAL_STORAGE + "/Android"*/) {
                                title = it
                            }
                        }
                    }

                )
            }
        }
    }

    @Composable
    fun pickerScreen(initialPath: String, onCurrentPathChanged: (String) -> Unit = {}) {
        Column {
            var confirmBtnEnabled by remember { mutableStateOf(false) }
            val navController = rememberNavController()
            NavHost(
                modifier = Modifier.weight(1f),
                navController = navController,
                startDestination = "picker/{path}"
            ) {
                composable(
                    "picker/{path}",
                    arguments = listOf(navArgument("path") {
                        defaultValue = ""
                        type = NavType.StringType
                    })
                ) {
                    val path = (it.arguments?.getString("path")?.run {
                        URLDecoder.decode(this, "UTF-8")
                    } ?: "").ifEmpty { initialPath }
                    onCurrentPathChanged.invoke(path)

                    if (FileTools.isAndroidDataPath(path)) {
                        val retUri = FilePermissionTools.isGrantedUriPermission(
                            path.toContentUri(false)!!, this@FilePickerActivity
                        )
                        if (retUri == null) {
                            requestAndroidDataPermission()
                            navController.navigateUp()
                            return@composable
                        }
                    }

                    var selectedList by remember {
                        mutableStateOf(listOf(FileModel(path = "")))
                    }

                    CatalogScreen(
                        path = path,
                        selectedList = selectedList,
                        onSelectListChange = { list ->
                            selectedList = list
                            confirmBtnEnabled = list.isNotEmpty()
                        },
                        onEnterDir = { model ->
                            if (model.path == FilesScreenViewModel.UPPER_PATH_NAME) {
                                navController.popBackStack()
                                return@CatalogScreen
                            }
                            navController.navigate(
                                "picker/${URLEncoder.encode(model.path, "UTF-8")}",
                                navOptions {
                                    launchSingleTop = false
                                    restoreState = true
                                }
                            )
                        })
                }
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                onClick =
                { toast("qwq") },
                enabled = confirmBtnEnabled
            )
            {
                Text(text = "OK")
            }
        }
    }


}

