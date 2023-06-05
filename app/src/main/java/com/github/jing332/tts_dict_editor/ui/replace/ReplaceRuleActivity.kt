package com.github.jing332.tts_dict_editor.ui.replace

import android.graphics.Rect
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.funny.data_saver.core.LocalDataSaver
import com.github.jing332.tts_dict_editor.R
import com.github.jing332.tts_dict_editor.help.AppConfig
import com.github.jing332.tts_dict_editor.help.ReplaceRule
import com.github.jing332.tts_dict_editor.help.ReplaceRuleGroup
import com.github.jing332.tts_dict_editor.ui.navigateSingleTop
import com.github.jing332.tts_dict_editor.ui.replace.edit.RuleEditScreen
import com.github.jing332.tts_dict_editor.ui.theme.AppTheme
import com.github.jing332.tts_dict_editor.ui.widget.Widgets
import com.github.jing332.tts_dict_editor.utils.ASFUriUtils.getPath
import com.github.jing332.tts_dict_editor.utils.observeNoSticky
import kotlinx.coroutines.launch

var isKeyboardVisibleState = mutableStateOf(false)
val LocalSoftKeyboardVisible =
    staticCompositionLocalOf<MutableState<Boolean>> { isKeyboardVisibleState }

class ReplaceRuleActivity : ComponentActivity() {
    companion object {
        const val TAG = "RuleManagerActivity"
    }

    private val vm: RuleManagerViewModel by viewModels()

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val contentView = findViewById<View>(android.R.id.content)
        val rootView = contentView.rootView

        val onGlobalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            val r = Rect()
            contentView.getWindowVisibleDisplayFrame(r)
            val screenHeight = rootView.height

            // r.bottom is the position above soft keypad or device button.
            // if keypad is shown, the r.bottom is smaller than that before.
            val keypadHeight = screenHeight - r.bottom

            if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                // keyboard is opened
                if (!isKeyboardVisibleState.value) {
                    isKeyboardVisibleState.value = true
                }
            } else {
                // keyboard is closed
                if (isKeyboardVisibleState.value) {
                    isKeyboardVisibleState.value = false
                }
            }
        }
        rootView.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)

        val uri = intent.data
        if (uri == null) {
            Log.e(TAG, "onCreate: uri is null")
            finish()
            return
        }

        vm.saveTxtLiveData.observeNoSticky(this) {
            synchronized(vm.saveTxtLiveData) {
                it?.let { txt ->
                    kotlin.runCatching {
                        contentResolver.openOutputStream(uri, "wt" /*覆写*/)
                            ?.use { os -> os.write(txt.toByteArray()) }
                    }.onFailure { t ->
//                        errDialog = "保存文件错误" to t
                    }
                }
            }
        }

        setContent {
            AppTheme {
                Widgets.TransparentSystemBars()
                CompositionLocalProvider(
                    LocalDataSaver provides AppConfig.dataSaverPref,
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = NavRoutes.Manager.route
                    ) {
                        composable(NavRoutes.Manager.route) {
                            val title = intent.getStringExtra("name")
                                ?: getString(R.string.replace_rule_manager)
                            val subTitle =
                                getPath(uri)?.removePrefix(Environment.getExternalStorageDirectory().absolutePath)
                                    ?: uri.toString()
                            ReplaceRuleManagerScreen(
                                title, subTitle,
                                onFinishedActivity = { finish() },
                                onEditRule = { rule ->
                                    navController.navigateSingleTop(
                                        NavRoutes.Editor.route,
                                        Bundle().apply {
                                            putParcelable(NavRoutes.Editor.KEY_RULE, rule)
                                            putParcelableArrayList(
                                                NavRoutes.Editor.KEY_GROUPS,
                                                ArrayList(vm.groups())
                                            )
                                        }
                                    )
                                },
                                vm = vm
                            )
                        }
                        composable(NavRoutes.Editor.route) { stackEntry ->
                            val rule: ReplaceRule? =
                                stackEntry.arguments?.getParcelable(NavRoutes.Editor.KEY_RULE)
                            val groups: ArrayList<ReplaceRuleGroup>? =
                                stackEntry.arguments?.getParcelableArrayList(NavRoutes.Editor.KEY_GROUPS)

                            RuleEditScreen(rule = rule!!, groups = groups!!, onResult = {
                                navController.popBackStack()
                                it?.let { rule ->
                                    vm.updateOrAddRule(rule)
                                }
                            })
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            kotlin.runCatching {
                vm.loadRulesFromDictTxt(contentResolver.openInputStream(uri)!!)
            }.onFailure {
//                errDialog = getString(R.string.failed_to_load) to it
            }
        }

    }
}