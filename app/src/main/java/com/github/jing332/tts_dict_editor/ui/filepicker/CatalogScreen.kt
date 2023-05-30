package com.github.jing332.tts_dict_editor.ui.filepicker

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.jing332.tts_dict_editor.R
import java.io.File

@Composable
fun CatalogScreen(
    path: String,
    selectedList: List<File>,
    onEnterDir: (FileItemModel) -> Unit,
    onSelectListChange: (List<File>) -> Unit,
    vm: CatalogScreenViewModel = viewModel(key = path),
) {
    val listState = rememberLazyListState()
    val models = remember { vm.models }
    LaunchedEffect(vm.hashCode()) {
        println("to path: ${vm.hashCode()} $path")
        if (!vm.isLoadFinished)
            vm.loadModels(path)

        vm.listState?.let {
            listState.scrollToItem(it.firstVisibleItemIndex, it.firstVisibleItemScrollOffset)
        }
    }

    DisposableEffect(vm.hashCode()) {
        onDispose {
            println("dispose: ${vm.hashCode()}")
            vm.listState = LazyListState(
                listState.firstVisibleItemIndex,
                listState.firstVisibleItemScrollOffset
            )
        }
    }

    LazyColumn(
        state = listState
    ) {
        items(models, key = { it.id }, itemContent = { model ->
            val onCheckChangeFun = remember {
                { checked: Boolean ->
                    println("selectedList: ${selectedList.size}")
                    onSelectListChange.invoke(
                        selectedList.toMutableList().apply {
                            if (checked) {
                                add(model.file)
                            } else {
                                remove(model.file)
                            }
                            println(checked)
                            println(size)
                        }
                    )
                }
            }
            Item(
                title = model.name,
                subTitle = if (model.file.absolutePath == CatalogScreenViewModel.UPPER_PATH_NAME)
                    "返回上一级"
                else "文件:${model.fileCount} \t 文件夹:${model.folderCount}",
                isCheckable = model.isCheckable,
                isChecked = selectedList.contains(model.file),
                isDirectory = model.isDirectory,
                onCheckedChange = onCheckChangeFun,
                onClickItem = {
                    if (model.isDirectory) onEnterDir.invoke(model)
                    else onCheckChangeFun.invoke(selectedList.contains(model.file).not())
                }
            )


        })
    }
}

@Composable
private fun Item(
    title: String,
    subTitle: String,
    isCheckable: Boolean,
    isChecked: Boolean,
    isDirectory: Boolean,
    onClickItem: () -> Unit,
    onCheckedChange: (Boolean) -> Unit
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClickItem.invoke() }
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12))
    ) {
        val (textTitle, textSubtitle, imgType, radioSelected) = createRefs()
        Image(
            painter = painterResource(id = if (isDirectory) R.drawable.baseline_folder_24 else R.drawable.baseline_insert_drive_file_24),
            contentDescription = "类型",
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
            modifier = Modifier
                .constrainAs(imgType) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
                .size(24.dp)
        )
        Text(
            text = title,
            modifier = Modifier.constrainAs(textTitle) {
                start.linkTo(imgType.end, margin = 8.dp)
                top.linkTo(parent.top)
                bottom.linkTo(textSubtitle.top)
            },
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = subTitle,
            modifier = Modifier.constrainAs(textSubtitle) {
                start.linkTo(imgType.end, margin = 8.dp)
                top.linkTo(textTitle.bottom)
                bottom.linkTo(parent.bottom)
            },
            style = MaterialTheme.typography.bodySmall
        )

        if (isCheckable)
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.constrainAs(radioSelected) {
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
            )
    }
}