package com.github.jing332.tts_dict_editor.ui.replace.edit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.jing332.tts_dict_editor.R
import com.github.jing332.tts_dict_editor.const.IntentKeys
import com.github.jing332.tts_dict_editor.help.ReplaceRule
import com.github.jing332.tts_dict_editor.help.ReplaceRuleGroup
import com.github.jing332.tts_dict_editor.ui.replace.NavRoutes
import com.github.jing332.tts_dict_editor.ui.theme.AppTheme
import com.github.jing332.tts_dict_editor.ui.widget.Widgets

@Suppress("DEPRECATION")
class RuleEditActivity : ComponentActivity() {
    companion object {
        const val KEY_GROUPS = "groups"
    }

    private val vm: RuleEditViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val rule = intent.getParcelableExtra<ReplaceRule>(IntentKeys.KEY_DATA)
        val groups =
            intent.getParcelableArrayExtra(KEY_GROUPS)?.map { it as ReplaceRuleGroup }
        vm.init(rule, groups ?: listOf(ReplaceRuleGroup(getString(R.string.default_group))))

        setContent {
            AppTheme {
                Widgets.TransparentSystemBars()


            }

        }
    }
}