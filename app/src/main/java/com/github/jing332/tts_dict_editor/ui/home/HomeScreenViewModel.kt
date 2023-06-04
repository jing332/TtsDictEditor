package com.github.jing332.tts_dict_editor.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.github.jing332.tts_dict_editor.data.appDb
import com.github.jing332.tts_dict_editor.ui.DictFileItemModel
import com.github.jing332.tts_dict_editor.ui.ListGroupModel

class HomeScreenViewModel : ViewModel() {
    val dictFilesFlow by lazy { appDb.dictFileDao.flowAll }

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