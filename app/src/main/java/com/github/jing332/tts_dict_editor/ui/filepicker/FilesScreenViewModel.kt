package com.github.jing332.tts_dict_editor.ui.filepicker

import android.util.Log
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import com.drake.net.utils.withIO
import com.github.jing332.tts_dict_editor.app
import com.github.jing332.tts_dict_editor.ui.filepicker.adapter.DocumentFileAdapter
import com.github.jing332.tts_dict_editor.ui.filepicker.adapter.GeneralFileAdapter
import com.github.jing332.tts_dict_editor.ui.filepicker.adapter.IFileAdapter
import com.github.jing332.tts_dict_editor.utils.FileTools
import com.github.jing332.tts_dict_editor.utils.FileUriTools.toContentUri
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.isActive
import java.io.File
import kotlin.coroutines.coroutineContext

class FilesScreenViewModel : ViewModel() {
    companion object {
        private const val TAG = "FilesScreenViewModel"
        internal const val UPPER_PATH_NAME = "/.."
    }

    var models = mutableStateListOf<FileModel>()
        private set

    var isReadErrorState = mutableStateOf(true)
        private set

    var isLoadFinished: Boolean = false
    var listState: LazyListState? = null
    private var currentPath: String = ""

    @Throws(IllegalArgumentException::class)
    suspend fun loadModels(path: String) {
        currentPath = path
        withIO {
            val adapter = if (FileTools.isAndroidDataPath(path))
                DocumentFileAdapter(
                    app,
                    DocumentFile.fromTreeUri(
                        app,
                        path.toContentUri(isTree = true)!!
                    )!!
                )
            else
                GeneralFileAdapter(File(path))

            if (!adapter.isDirectory()) {
                Log.w(TAG, "path is not a directory or not readable: $path")
                isReadErrorState.value = false

                return@withIO
            }

            models.clear()
            adapter.listFiles().apply {
                var list: List<IFileAdapter> =
                    filter { it.isDirectory() }.sortedBy { it.name() }


                addModels(list)
                addModels(filter { it.isFile() }.sortedBy { it.name() })
            }
        }

        isLoadFinished = true
    }

    private suspend fun addModels(list: List<IFileAdapter>) {
        list.forEach { adapter ->
            if (adapter is DocumentFileAdapter) {
//                println(adapter.name())
            }

            if (coroutineContext.isActive.not()) throw CancellationException()
            models.add(
                FileModel(
                    path = "$currentPath${File.separator}${adapter.name()}",
                    name = adapter.name(),
                    fileCount = adapter.fileCount(),
                    folderCount = adapter.directoryCount(),
                    isDirectory = adapter.isDirectory(),
                )
            )
        }
    }

    fun updateModelsSelected(model: FileModel, it: Boolean) {
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