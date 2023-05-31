package com.github.jing332.tts_dict_editor.ui.replace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.github.jing332.tts_dict_editor.R
import com.github.jing332.tts_dict_editor.help.GroupWithReplaceRule
import com.github.jing332.tts_dict_editor.help.ReplaceRuleGroup
import com.github.jing332.tts_dict_editor.help.ReplaceRule
import com.github.jing332.tts_dict_editor.ui.Widgets
import com.github.jing332.tts_dict_editor.ui.theme.AppTheme

class RuleManagerActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

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
                                IconButton(onClick = {

                                }) {
                                    Icon(Icons.Filled.Add, stringResource(id = R.string.add))
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
                            screen()
                        }
                    })
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun screen() {
        val groups = mutableListOf<GroupWithReplaceRule>()
        (1..1000).forEach {
            val list = mutableListOf<ReplaceRule>()
            (1..10).forEach { i ->
                list.add(ReplaceRule(name = "替换规则-$i", id = i.toLong()))
            }

            groups.add(
                GroupWithReplaceRule(
                    group = ReplaceRuleGroup("QWQ-$it", id = it.toLong()),
                    list = list
                )
            )
        }

        // Keys
        val expandedGroups =
            remember { mutableStateListOf(*groups.map { it.group.id }.toTypedArray()) }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            for ((index, groupWithRule) in groups.withIndex()) {
                stickyHeader(key = "group_${index}_${groupWithRule.group.id}") {
                    GroupItem(
                        name = groupWithRule.group.name,
                        isExpanded = expandedGroups.contains(groupWithRule.group.id)
                    ) {
                        if (expandedGroups.contains(groupWithRule.group.id))
                            expandedGroups.remove(groupWithRule.group.id)
                        else
                            expandedGroups.add(groupWithRule.group.id)
                    }
                }

                groupWithRule.list.forEach { replaceRule ->
                    item(key = "${index}_$${replaceRule.id}") {
                        if (expandedGroups.contains(groupWithRule.group.id))
                            ReplaceRuleItem(
                                replaceRule.name,
                                modifier = Modifier.animateItemPlacement()
                            )
                    }
                }

            }
        }
    }

    @Composable
    fun GroupItem(name: String, isExpanded: Boolean, onClick: () -> Unit) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onClick.invoke() },
        ) {
            Image(
                modifier = Modifier.align(Alignment.CenterVertically),
                painter = painterResource(id = if (isExpanded) R.drawable.ic_arrow_expand else R.drawable.ic_arrow_collapse),
                contentDescription = if (isExpanded) "已展开" else "已收起",
            )
            Text(
                text = name,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterVertically)
                    .weight(1f),
                fontWeight = FontWeight.W700,
                color = Color(0xFF0079D3)
            )
            IconButton(onClick = {}, modifier = Modifier.padding(end = 10.dp)) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = stringResource(id = R.string.more_options),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

        }
    }

    @Composable
    fun ReplaceRuleItem(name: String, modifier: Modifier) {
        Row(modifier = modifier.fillMaxSize()) {
            Text(
                name,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )

            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = stringResource(id = R.string.more_options),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

    }


}