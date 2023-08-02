package io.github.lumyuan.turingbox.ui.theme

import android.content.Context
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.github.jing332.tts_dict_editor.help.AppConfig
import com.github.jing332.tts_dict_editor.ui.theme.AppTheme
import com.github.jing332.tts_dict_editor.ui.theme.Typography
import com.github.jing332.tts_dict_editor.ui.theme.blueTheme
import com.github.jing332.tts_dict_editor.ui.theme.brownTheme
import com.github.jing332.tts_dict_editor.ui.theme.cyanTheme
import com.github.jing332.tts_dict_editor.ui.theme.defaultTheme
import com.github.jing332.tts_dict_editor.ui.theme.dynamicColorTheme
import com.github.jing332.tts_dict_editor.ui.theme.grayTheme
import com.github.jing332.tts_dict_editor.ui.theme.greenTheme
import com.github.jing332.tts_dict_editor.ui.theme.orangeTheme
import com.github.jing332.tts_dict_editor.ui.theme.pinkTheme
import com.github.jing332.tts_dict_editor.ui.theme.purpleTheme
import com.github.jing332.tts_dict_editor.ui.theme.redTheme
import com.github.jing332.tts_dict_editor.ui.theme.themeAnimation
import com.gyf.immersionbar.ImmersionBar

/**
 * 获取当前主题
 */
@Composable
fun appTheme(
    themeType: AppTheme,
    darkTheme: Boolean = isSystemInDarkTheme(),
    context: Context = LocalContext.current
): ColorScheme =
    when (themeType) {
        AppTheme.DEFAULT -> defaultTheme(darkTheme)
        AppTheme.DYNAMIC_COLOR -> dynamicColorTheme(darkTheme, context)
        AppTheme.GREEN -> greenTheme(darkTheme)
        AppTheme.RED -> redTheme(darkTheme)
        AppTheme.PINK -> pinkTheme(darkTheme)
        AppTheme.BLUE -> blueTheme(darkTheme)
        AppTheme.CYAN -> cyanTheme(darkTheme)
        AppTheme.ORANGE -> orangeTheme(darkTheme)
        AppTheme.PURPLE -> purpleTheme(darkTheme)
        AppTheme.BROWN -> brownTheme(darkTheme)
        AppTheme.GRAY -> grayTheme(darkTheme)
    }

//全局主题状态
private val themeTypeState: MutableState<AppTheme> by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
    mutableStateOf(AppTheme.DEFAULT)
}

@Composable
private fun InitTheme() {
    val theme = try {
        AppConfig.theme.value
    } catch (e: Exception) {
        e.printStackTrace()
        AppTheme.DEFAULT
    }
    setAppTheme(themeType = theme)
}

/**
 * 设置主题
 */
fun setAppTheme(themeType: AppTheme) {
    themeTypeState.value = themeType
    AppConfig.theme.value = themeType
}

/**
 * 获取当前主题
 */
fun getAppTheme(): AppTheme = themeTypeState.value

/**
 * 根Context
 */
@Suppress("DEPRECATION")
@Composable
fun DictEditorTheme(
    modifier: Modifier = Modifier,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    //初始化主题
    InitTheme()

    //获取当前主题
    val targetTheme = appTheme(themeType = themeTypeState.value)

    //沉浸式状态栏
    ImmersionBar.with(LocalView.current.context as ComponentActivity)
        .transparentStatusBar()
        .transparentNavigationBar()
        .statusBarDarkFont(!darkTheme)
        .navigationBarDarkIcon(!darkTheme)
//        .keyboardEnable(true)
        .keyboardMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        .init()

    MaterialTheme(
        colorScheme = themeAnimation(targetTheme = targetTheme),
        typography = Typography
    ) {
        Surface(
            modifier = modifier,
            color = MaterialTheme.colorScheme.background,
            content = content
        )
    }
}