package com.github.jing332.tts_dict_editor.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.github.jing332.tts_dict_editor.data.entites.DictFile

class DictFileEditViewModel : ViewModel() {
    private val _nameState = mutableStateOf("")
    val nameState: State<String>
        get() = _nameState

    private val _filePathState = mutableStateOf("")
    val filePathState: State<String>
        get() = _filePathState

    private lateinit var mDictFile: DictFile

    fun updateName(text: String) {
        _nameState.value = text
    }

    fun updateFilePath(path:String){
        _filePathState.value = path
    }

    fun init(dictFile: DictFile) {
        mDictFile = dictFile
        _nameState.value = dictFile.name
        _filePathState.value = dictFile.filePath
    }

    fun getDictFile(): DictFile {
        return mDictFile.copy(name = nameState.value, filePath = filePathState.value)
    }
}