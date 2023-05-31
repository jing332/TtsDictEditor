package com.github.jing332.tts_dict_editor.ui.replace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import com.github.jing332.tts_dict_editor.R
import com.github.jing332.tts_dict_editor.data.entites.DictFile
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

                        }
                    })
            }
        }
    }


}