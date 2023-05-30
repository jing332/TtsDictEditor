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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import com.github.jing332.tts_dict_editor.R
import com.github.jing332.tts_dict_editor.ui.Widgets
import com.github.jing332.tts_dict_editor.ui.theme.AppTheme
import com.github.jing332.tts_server_android.util.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.net.URLDecoder
import java.net.URLEncoder

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
                            pickerScreen { title = it }
                        }
                    }

                )
            }
        }
    }

    @Composable
    fun pickerScreen(onCurrentPathChanged: (String) -> Unit = {}) {
        Column {
            var confirmBtnEnabled by remember {
                mutableStateOf(false)
            }
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
                    } ?: "").ifEmpty { Environment.getExternalStorageDirectory().absolutePath }
                    onCurrentPathChanged.invoke(path)

                    var selectedList by remember {
                        mutableStateOf(listOf<File>())
                    }

                    CatalogScreen(
                        path = path,
                        selectedList = selectedList,
                        onSelectListChange = { list ->
                            selectedList = list
                            confirmBtnEnabled = list.isNotEmpty()
                        },
                        onEnterDir = { model ->
                            val file = model.file
                            if (file.absolutePath == CatalogScreenViewModel.UPPER_PATH_NAME) {
                                navController.popBackStack()
                            } else {
                                navController.navigate(
                                    "picker/${URLEncoder.encode(file.absolutePath, "UTF-8")}",
                                    navOptions {
                                        launchSingleTop = false
                                        restoreState = true
                                    })
                            }
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

