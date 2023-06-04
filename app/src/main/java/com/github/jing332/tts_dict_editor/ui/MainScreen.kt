package com.github.jing332.tts_dict_editor.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.github.jing332.tts_dict_editor.BuildConfig
import com.github.jing332.tts_dict_editor.R
import com.github.jing332.tts_dict_editor.ui.home.HomeScreen
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
) {
    val navController = LocalNavController.current
    val scope = rememberCoroutineScope()
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch {
            drawerState.close()
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


@Composable
fun DrawerMenuItem(text: String, icon: @Composable() (() -> Unit), onClick: () -> Unit) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier.padding(16.dp),
        onClick = onClick,
        shape = RoundedCornerShape(25),
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_image_24),
                contentDescription = "",
                modifier = Modifier
                    .size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}
