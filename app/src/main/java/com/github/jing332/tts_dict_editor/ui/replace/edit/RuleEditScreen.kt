@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.jing332.tts_dict_editor.ui.replace.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.jing332.tts_dict_editor.R
import com.github.jing332.tts_dict_editor.help.ReplaceRule
import com.github.jing332.tts_dict_editor.help.ReplaceRuleGroup
import com.github.jing332.tts_dict_editor.ui.widget.DropMenuTextField

@Composable
fun RuleEditScreen(
    rule: ReplaceRule,
    groups: List<ReplaceRuleGroup>,
    onResult: (ReplaceRule?) -> Unit
) {
    val vm: RuleEditViewModel = viewModel()
    LaunchedEffect(rule) {
        vm.init(rule, groups)
    }
    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                modifier = Modifier.fillMaxWidth(),
                navigationIcon = {
                    IconButton(onClick = { onResult.invoke(null) }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                },
                title = { Text(text = stringResource(id = R.string.edit_replace_rule)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(
                        onClick = {
                            onResult.invoke(vm.getRule())
                        }) {
                        Icon(Icons.Filled.Save, stringResource(id = R.string.save))
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
            Surface(
                modifier = Modifier
                    .padding(pad)
                    .verticalScroll(rememberScrollState())
            ) {
                Screen(
                    group = vm.groupKeyState.value,
                    groupKeys = vm.groupsState,
                    groupValues = vm.groupsState.map { it.name },
                    onGroupChange = vm::setGroup,

                    vm.nameState.value, vm::setName,
                    vm.patternState.value, vm::setPattern,
                    vm.replacementState.value, vm::setReplacement,
                    vm.isRegexState.value, vm::setIsRegex,
                    onTest = {
                        (try {
                            vm.doReplace(it)
                        } catch (e: Exception) {
                            e.message ?: ""
                        })
                    }
                )
            }
        }

    )
}

@Composable
private fun Screen(
    group: ReplaceRuleGroup,
    groupKeys: List<ReplaceRuleGroup>,
    groupValues: List<String>,
    onGroupChange: (ReplaceRuleGroup) -> Unit,

    nameValue: String,
    onNameValueChange: (String) -> Unit,
    patternValue: String,
    onReplaceValueChange: (String) -> Unit,
    replacementValue: String,
    onReplacementValueChange: (String) -> Unit,
    isRegex: Boolean,
    onIsRegexChange: (Boolean) -> Unit,

    onTest: (String) -> String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DropMenuTextField(
            label = { Text(text = stringResource(R.string.belonging_group)) },
            key = group,
            keys = groupKeys,
            values = groupValues,
            onKeyChange = { onGroupChange.invoke(it as ReplaceRuleGroup) }
        )

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
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 8.dp)
            )
        }

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )


        var testText by remember { mutableStateOf("") }
        var testResult by remember { mutableStateOf("") }

        OutlinedTextField(
            label = { Text(stringResource(R.string.test_text)) },
            value = testText,
            onValueChange = {
                testText = it
                testResult = onTest.invoke(it)
            },
            modifier = Modifier.fillMaxWidth()
        )

        if (testText.isNotEmpty())
            Text(stringResource(R.string.label_result))
        SelectionContainer {
            Text(text = testResult, style = MaterialTheme.typography.bodyMedium)
        }
    }
}