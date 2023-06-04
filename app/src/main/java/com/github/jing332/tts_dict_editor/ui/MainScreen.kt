package com.github.jing332.tts_dict_editor.ui

import android.app.Activity
import android.os.Bundle
import android.os.Parcelable
import android.os.SystemClock
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import com.github.jing332.tts_dict_editor.BuildConfig
import com.github.jing332.tts_dict_editor.R
import com.github.jing332.tts_dict_editor.app
import com.github.jing332.tts_dict_editor.ui.home.HomeScreen
import com.talhafaki.composablesweettoast.util.SweetToastUtil
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

@Composable
fun MainScreen(
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    onFinishedActivity: () -> Unit,
) {
//    var lastBackDownTime by remember { mutableLongStateOf(0L) }
    var toastMsg by remember { mutableStateOf("") }
    if (toastMsg.isNotEmpty()) {
        SweetToastUtil.SweetInfo(
            message = toastMsg,
            Toast.LENGTH_SHORT,
            PaddingValues(bottom = 32.dp)
        )
        toastMsg = ""
    }

    val snackbarHostState = LocalSnackbarHostState.current
    val navController = LocalNavController.current
    val scope = rememberCoroutineScope()
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch {
            drawerState.close()
        }
    }
    var lastBackDownTime by remember { mutableLongStateOf(0L) }
    BackHandler(enabled = drawerState.isClosed) {
        val duration = 2000
        SystemClock.elapsedRealtime().let {
            if (it - lastBackDownTime <= duration) {
                onFinishedActivity.invoke()
            } else {
                lastBackDownTime = it
                scope.launch {
                    withTimeout(duration.toLong()) {
                        snackbarHostState.showSnackbar("再按一次退出")
                    }
                }
            }
        }
    }
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                Modifier
                    .fillMaxHeight()
                    .width(300.dp)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(12.dp),
                navController = navController
            )
        }
    ) {
        HomeScreen(drawerState = drawerState)
    }
}

@Composable
private fun DrawerContent(modifier: Modifier, navController: NavHostController) {
    val drawerItemIcon = @Composable { img: ImageVector, contentDescription: String ->
        Icon(
            img,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }

    val drawerItem = @Composable { img: ImageVector, targetScreen: AppNavRoutes ->
        NavigationDrawerItem(
            icon = { drawerItemIcon(img, stringResource(id = targetScreen.titleResId)) },
            label = { Text(text = stringResource(id = targetScreen.titleResId)) },
            selected = false,
            onClick = { navController.navigateSingleTop(targetScreen.route) }
        )
    }

    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(24.dp))

        Row {
            Image(
                painterResource(id = R.drawable.ic_new_launcher_foreground),
                stringResource(id = R.string.app_name),
                modifier = Modifier.size(64.dp)
            )
            Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.titleMedium
                )
                SelectionContainer {
                    Text(
                        text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 4.dp)
        )

        drawerItem(Icons.Filled.Info, AppNavRoutes.About)
    }
}


fun NavHostController.navigateSingleTop(route: String, popUpToMain: Boolean = false) {
    val navController = this
    navController.navigate(route) {
        // 先清空其他栈，使得返回时能直接回到主界面
        if (popUpToMain) {
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
                inclusive = false
                //currentScreen = TranslateScreen.MainScreen
            }
        }
        //从名字就能看出来 跟activity的启动模式中的SingleTop模式一样 避免在栈顶创建多个实例
        launchSingleTop = true
        //切换状态的时候保存页面状态
        restoreState = true
    }
}