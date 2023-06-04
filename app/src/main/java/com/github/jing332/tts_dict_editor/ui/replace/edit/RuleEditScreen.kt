@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)

package com.github.jing332.tts_dict_editor.ui.replace.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Abc
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.jing332.tts_dict_editor.R
import com.github.jing332.tts_dict_editor.help.ReplaceRule
import com.github.jing332.tts_dict_editor.help.ReplaceRuleGroup
import com.github.jing332.tts_dict_editor.ui.replace.LocalSoftKeyboardVisible
import com.github.jing332.tts_dict_editor.ui.widget.DropMenuTextField

@Composable
fun RuleEditScreen(
    rule: ReplaceRule,
    groups: List<ReplaceRuleGroup>,
    onResult: (ReplaceRule?) -> Unit
) {
    val vm: RuleEditViewModel = viewModel()
    var isVisibleToolbar by remember { mutableStateOf(false) }
    var inputKeyState = remember { mutableStateOf("") }

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
                        onClick = { onResult.invoke(vm.getRule()) }) {
                        Icon(Icons.Filled.Save, stringResource(id = R.string.save))
                    }

                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Filled.MoreVert,
                            stringResource(id = R.string.more_options)
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (LocalSoftKeyboardVisible.current.value) {
                SoftKeyboardInputToolbar(
                    items = listOf(
                        "(", ")", "[", "]", "{", "}", "<", ">",
                        "!", "@", "#", "$", "%", "^", "&", "*",
                        "-", "+", "=", "|", "\\", "/", "?", ",",
                        ":", ";", "\"", "'", " ", "\n"
                    ).map { it to it },
                    onClick = {
                        inputKeyState.value = it
                    }
                )
            }
        },
        content = { pad ->
            Surface(
                modifier = Modifier
                    .padding(pad)
                    .verticalScroll(rememberScrollState())
            ) {
                Screen(
                    inputKeyState,
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
                    },
                )
            }
        }

    )
}

@Composable
fun SoftKeyboardInputToolbar(
    items: List<Pair<String, String>>,
    onClick: (key: String) -> Unit
) {
    LazyRow(modifier = Modifier.fillMaxWidth()) {
        itemsIndexed(items.map { it.first }) { index, key ->
            TextButton(
//                modifier = Modifier.padding(end = 1.dp),
                onClick = {
                    onClick.invoke(key)
                }) {
                Text(text = items[index].second)
            }
        }
    }
}

private object InputFieldID {
    const val NAME = 0
    const val PATTERN = 1
    const val REPLACEMENT = 2
}

/**
 * 插入文本到当前光标前方
 */
fun TextFieldValue.newValueOfInsertText(
    text: String,
    cursorPosition: Int = selection.end
): TextFieldValue {
    val newText = StringBuilder(this.text).insert(cursorPosition, text).toString()
    return TextFieldValue(newText, TextRange(cursorPosition + text.length))
}

@Composable
private fun Screen(
    insertKeyState: MutableState<String>,
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
    var currentInputFocus by remember { mutableIntStateOf(-1) }

    var nameTextFieldValue by remember { mutableStateOf(TextFieldValue(nameValue)) }
    var patternTextFieldValue by remember { mutableStateOf(TextFieldValue(patternValue)) }
    fun setPattern(value: TextFieldValue) {
        patternTextFieldValue = value
        onReplaceValueChange.invoke(value.text)
    }

    var replacementTextFieldValue by remember { mutableStateOf(TextFieldValue(replacementValue)) }
    fun setReplacement(value: TextFieldValue) {
        replacementTextFieldValue = value
        onReplacementValueChange.invoke(value.text)
    }

    if (insertKeyState.value.isNotEmpty()) {
        println("insertKeyState.value: " + insertKeyState.value)
        when (currentInputFocus) {
            InputFieldID.PATTERN -> {
                setPattern(patternTextFieldValue.newValueOfInsertText(insertKeyState.value))
            }

            InputFieldID.REPLACEMENT -> {

                setReplacement(replacementTextFieldValue.newValueOfInsertText(insertKeyState.value))
            }
        }
        insertKeyState.value = ""
    }


    var isVisiblePinyinDialog by remember { mutableStateOf(false) }
    if (isVisiblePinyinDialog)
        PinyinDialog({ isVisiblePinyinDialog = false }, onInput = {
            isVisiblePinyinDialog = false
            insertKeyState.value = it
        })

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
            modifier = Modifier
                .fillMaxWidth(),
        )

        OutlinedTextField(
            label = { Text(stringResource(R.string.pattern)) },
            value = patternTextFieldValue,
            onValueChange = { setPattern(it) },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) currentInputFocus = InputFieldID.PATTERN
                },
            trailingIcon = {
                IconButton(onClick = { isVisiblePinyinDialog = true }) {
                    Icon(Icons.Filled.Abc, stringResource(R.string.insert_pinyin))
                }
            }
        )
        OutlinedTextField(
            label = { Text(stringResource(R.string.replacement)) },
            value = replacementTextFieldValue,
            onValueChange = {
                replacementTextFieldValue = it
                onReplacementValueChange.invoke(it.text)
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused)
                        currentInputFocus = InputFieldID.REPLACEMENT
                },
            trailingIcon = {
                IconButton(onClick = { isVisiblePinyinDialog = true }) {
                    Icon(Icons.Filled.Abc, stringResource(R.string.insert_pinyin))
                }
            }
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

@Composable
private fun PinyinDialog(onDismissRequest: () -> Unit, onInput: (text: String) -> Unit) {
    val pinyinList = remember {
        listOf(
            "ā-a-1声",
            "á-a-2声",
            "ǎ-a-3声",
            "à-a-4声",
            "ê-e-?声",
            "ē-e-1声",
            "é-e-2声",
            "ě-e-3声",
            "è-e-4声",
            "ī-i-1声",
            "í-i-2声",
            "ǐ-i-3声",
            "ì-i-4声",
            "ō-o-1声",
            "ó-o-2声",
            "ǒ-o-3声",
            "ò-o-4声",
            "ū-u-1声",
            "ú-u-2声",
            "ǔ-u-3声",
            "ù-u-4声",
            "ǖ-v-1声",
            "ǘ-v-2声",
            "ǚ-v-3声",
            "ǜ-v-4声"
        )
    }

    AlertDialog(onDismissRequest = onDismissRequest) {
        Surface(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            LazyVerticalGrid(columns = GridCells.Fixed(4)) {
                items(pinyinList.map { it[0].toString() }) {
                    TextButton(onClick = { onInput.invoke(it) }) {
                        Text(
                            text = it,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }
    }

}

@Preview
@Composable
fun PreviewPinyinDialog() {
    var isVisible by remember { mutableStateOf(true) }
    if (isVisible)
        PinyinDialog({ isVisible = false }, onInput = {})
}