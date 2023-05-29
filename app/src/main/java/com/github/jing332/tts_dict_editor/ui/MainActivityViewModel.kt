package com.github.jing332.tts_dict_editor.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData

class MainActivityViewModel : ViewModel() {
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