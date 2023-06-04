package com.github.jing332.tts_dict_editor.ui

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.github.jing332.tts_dict_editor.data.appDb
import com.github.jing332.tts_dict_editor.data.entites.DictFile
import com.github.jing332.tts_dict_editor.ui.edit.DictFileEditActivity
import com.github.jing332.tts_dict_editor.ui.edit.DictFileEditScreen
import com.github.jing332.tts_dict_editor.ui.home.DictFileParamType
import com.github.jing332.tts_dict_editor.ui.theme.AppTheme
import com.github.jing332.tts_dict_editor.ui.widget.Widgets.TransparentSystemBars
import com.github.jing332.tts_server_android.util.longToast
import com.talhafaki.composablesweettoast.util.SweetToastUtil
import kotlinx.coroutines.launch

val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("NavController has not been initialized! ")
}
val LocalSnackbarHostState = staticCompositionLocalOf<SnackbarHostState> {
    error("SnackbarHostState has not been initialized! ")
}

class MainActivity : ComponentActivity() {
    private val vm: MainActivityViewModel by viewModels()

    private val mDictFileActivityLauncher = registerForActivityResult(
        AppActivityResultContracts.parcelableDataActivity<DictFile>(DictFileEditActivity::class.java)
    ) {
        it?.let { dictFile ->
            appDb.dictFileDao.insert(dictFile)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            AppTheme {
                TransparentSystemBars()
                Surface {
                    AppNavigation()
                }
            }
        }

        importDictFileFromIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        importDictFileFromIntent(intent)
    }


    private fun importDictFileFromIntent(intent: Intent?) {
        intent?.data?.let { uri ->
            longToast("⚠ 使用此方式导入的文件为临时权限，重启本程序后将无法读写。")
            mDictFileActivityLauncher.launch(DictFile(filePath = uri.toString()))

            intent.data = null
        }
    }


    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    fun AppNavigation(
        navController: NavHostController = rememberNavController(),
        drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    ) {
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        CompositionLocalProvider(
            LocalNavController provides navController,
            LocalSnackbarHostState provides snackbarHostState
        ) {
            NavHost(
                navController,
                startDestination = AppNavRoutes.DictFileManager.route
            ) {
                composable(AppNavRoutes.DictFileManager.route) {
                    MainScreen { finish() }
                }

                composable(AppNavRoutes.DictFileEdit.route,
                    arguments = listOf(navArgument("dictFile") {
                        type = DictFileParamType()
                    }
                    )) {
                    @Suppress("DEPRECATION")
                    val dictFile: DictFile? = it.arguments?.getParcelable("dictFile")
                    if (dictFile == null) {
                        navController.popBackStack()
                        return@composable
                    }
                    DictFileEditScreen(dictFile)
                }

                composable(AppNavRoutes.About.route) {
                    AboutScreen()
                }
            }
        }
    }
}