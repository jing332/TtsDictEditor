package com.github.jing332.tts_dict_editor.ui.filepicker

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drake.net.utils.withIO
import kotlinx.coroutines.launch
import java.io.File

class FilePickerViewModel : ViewModel() {
    private val _models = mutableStateListOf<ItemModel>()
    val models: SnapshotStateList<ItemModel>
        get() = _models

    val hasSelectedModels = mutableStateOf(false)
    val currentPath = mutableStateOf("")

    @Suppress("HasPlatformType")
    @Throws(IllegalArgumentException::class)
    suspend fun updateModels(aDir: String) = withIO {
        println(aDir)
        val dir =
            if (aDir == "/..") File(currentPath.value).parentFile?.absolutePath ?: currentPath.value
            else aDir
        currentPath.value = dir

        val dirFile = File(dir)
        if (!dirFile.isDirectory)
            throw IllegalArgumentException("dir is not a directory")

        _models.clear()
        dirFile.listFiles()?.apply {
            _models.add(
                ItemModel(
                    name = "上一级目录",
                    file = File("/.."),
                    isCheckable = false
                )
            )
            addModels(filter { it.isDirectory })
            addModels(filter { it.isFile })
        }
    }

    private fun addModels(list: List<File>) {
        list.forEach { file ->
            _models.add(
                ItemModel(
                    file, file.name,
                    fileCount = file.listFiles()?.filter { it.isFile }?.size ?: 0,
                    folderCount = file.listFiles()?.filter { it.isDirectory }?.size ?: 0,
                )
            )
        }
    }

    fun updateModelsSelected(model: ItemModel, it: Boolean) {
        println("ViewModle: $it")
        val i = _models.indexOf(model)
        if (i != -1) {
            _models.toList().forEachIndexed { index, itemModel ->
                if (itemModel.isChecked) {
                    _models[index] = itemModel.copy(isChecked = false)
                }
            }
            _models[i] = model.copy(isChecked = it)

            hasSelectedModels.value = it
        }
//        viewModelScope.launch {
//            _models.value.find { it == model }?.isSelected = it
//            _models.emit(_models.value)
//        }
    }

    fun enterDir(model: ItemModel) {
        if (!model.isDirectory) throw IllegalArgumentException("model is not a directory")

        viewModelScope.launch { updateModels(model.file.absolutePath) }
    }
}