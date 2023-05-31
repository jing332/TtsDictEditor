package com.github.jing332.tts_dict_editor.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.github.jing332.tts_dict_editor.data.appDb
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

class MainActivityViewModel : ViewModel() {
    val dictFilesFlow by lazy { appDb.dictFileDao.flowAll}


    fun getModels(): LiveData<List<ListGroupModel<DictFileItemModel>>> {
        return liveData {
            val listGroup = mutableListOf<ListGroupModel<DictFileItemModel>>()
            for (i in 0..100) {
                val list = mutableListOf<DictFileItemModel>()
                for (itemIndex in 0..10) {
                    list.add(DictFileItemModel("$i-$itemIndex-name", "filePath"))
                }
                listGroup.add(ListGroupModel("$i-name", list))
            }

            emit(listGroup)
        }
    }

}