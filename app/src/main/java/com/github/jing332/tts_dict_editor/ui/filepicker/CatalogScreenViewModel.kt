package com.github.jing332.tts_dict_editor.ui.filepicker

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.drake.net.utils.withIO
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.isActive
import java.io.File
import java.io.FileFilter
import kotlin.coroutines.coroutineContext

class CatalogScreenViewModel : ViewModel() {
    companion object {
        internal const val UPPER_PATH_NAME = "/.."
    }

    var models = mutableStateListOf<FileItemModel>()
        private set

    var isLoadFinished: Boolean = false
    var listState: LazyListState? = null

//
//    val models: SnapshotStateList<ItemModel>
//        get() = _models

    @Throws(IllegalArgumentException::class)
    suspend fun loadModels(path: String) {
        withIO {
            val dirFile = File(path)
            if (!dirFile.isDirectory)
                throw IllegalArgumentException("dir is not a directory: $path")

            models.clear()
            dirFile.listFiles(FileFilter {
                if (coroutineContext.isActive.not()) throw CancellationException()
                true
            })?.apply {
                models.add(
                    FileItemModel(
                        name = "上一级目录",
                        file = File(UPPER_PATH_NAME),
                        isCheckable = false
                    )
                )
                addModels(filter { it.isDirectory })
                addModels(filter { it.isFile })
            }
        }

        isLoadFinished = true
    }

    private suspend fun addModels(list: List<File>) {
        list.forEach { file ->
            if (coroutineContext.isActive.not()) throw CancellationException()
            models.add(
                FileItemModel(
                    file, file.name,
                    fileCount = file.listFiles()?.filter { it.isFile }?.size ?: 0,
                    folderCount = file.listFiles()?.filter { it.isDirectory }?.size ?: 0,
                )
            )
        }
    }

    fun updateModelsSelected(model: FileItemModel, it: Boolean) {
        println("ViewModle: $it")
        val i = models.indexOf(model)
        if (i != -1) {
            models.toList().forEachIndexed { index, itemModel ->
                if (itemModel.isChecked) {
                    models[index] = itemModel.copy(isChecked = false)
                }
            }
            models[i] = model.copy(isChecked = it)

//            hasSelectedModels.value = it
        }
//        viewModelScope.launch {
//            _models.value.find { it == model }?.isSelected = it
//            _models.emit(_models.value)
//        }
    }
}