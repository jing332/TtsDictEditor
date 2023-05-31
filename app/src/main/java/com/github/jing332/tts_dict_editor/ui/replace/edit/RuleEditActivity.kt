package com.github.jing332.tts_dict_editor.ui.replace.edit

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Checkbox
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
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.github.jing332.tts_dict_editor.R
import com.github.jing332.tts_dict_editor.const.IntentKeys
import com.github.jing332.tts_dict_editor.ui.Widgets
import com.github.jing332.tts_dict_editor.help.ReplaceRule
import com.github.jing332.tts_dict_editor.ui.theme.AppTheme

@Suppress("DEPRECATION")
class RuleEditActivity : ComponentActivity() {
    private val vm: RuleEditViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val rule = intent.getParcelableExtra<ReplaceRule>(IntentKeys.KEY_DATA)
        vm.init(rule)

        setContent {
            AppTheme {
                Widgets.TransparentSystemBars()
                Scaffold(
                    topBar = {
                        TopAppBar(
                            modifier = Modifier.fillMaxWidth(),
                            title = { Text(text = stringResource(id = R.string.app_name)) },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            actions = {
                                IconButton(
                                    onClick = {
                                        setResult(RESULT_OK, Intent().apply {
                                            putExtra(IntentKeys.KEY_DATA, vm.getRule())
                                        })
                                    }) {
                                    Icon(Icons.Filled.Save, stringResource(id = R.string.add))
                                }

                                IconButton(onClick = {
                                }) {
                                    Icon(
                                        Icons.Filled.MoreVert,
                                        stringResource(id = R.string.more_options)
                                    )
                                }
                            }
                        )
                    },
                    content = { pad ->
                        Surface(modifier = Modifier.padding(pad)) {
                            screen(
                                vm.nameState.value, vm::setName,
                                vm.patternState.value, vm::setPattern,
                                vm.replacementState.value, vm::setReplacement,
                                vm.isRegexState.value, vm::setIsRegex,
                            )
                        }
                    }

                )
            }
        }
    }

    @Composable
    fun screen(
        nameValue: String, onNameValueChange: (String) -> Unit,
        patternValue: String, onReplaceValueChange: (String) -> Unit,
        replacementValue: String, onReplacementValueChange: (String) -> Unit,
        isRegex: Boolean, onIsRegexChange: (Boolean) -> Unit,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                label = { Text(stringResource(R.string.name)) },
                value = nameValue,
                onValueChange = onNameValueChange,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                label = { Text(stringResource(R.string.pattern)) },
                value = patternValue,
                onValueChange = onReplaceValueChange,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                label = { Text(stringResource(R.string.replacement)) },
                value = replacementValue,
                onValueChange = onReplacementValueChange,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clickable {
                        onIsRegexChange(!isRegex)
                    }
            ) {
                Checkbox(
                    checked = isRegex,
                    onCheckedChange = onIsRegexChange,
                )
                Text(
                    text = stringResource(R.string.use_regex),
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
    }
}