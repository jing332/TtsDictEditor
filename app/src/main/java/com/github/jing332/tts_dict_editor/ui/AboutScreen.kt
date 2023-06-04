package com.github.jing332.tts_dict_editor.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.github.jing332.tts_dict_editor.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen() {
    val navController = LocalNavController.current
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.about)) }, navigationIcon = {
                IconButton(onClick = {
                    navController.popBackStack()
                }) {
                    Icon(Icons.Filled.ArrowBack, stringResource(id = R.string.back))
                }
            })
        }
    ) {
        Surface(modifier = Modifier.padding(it)) {
            Text("About")
        }
    }
}