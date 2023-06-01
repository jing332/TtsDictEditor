package com.github.jing332.tts_dict_editor.data.entites

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize


@Parcelize
@Entity("dict_files")
data class DictFile(
    @PrimaryKey
    val id: Long = System.currentTimeMillis(),

    val name: String = "",
    /* Content URI */
    val filePath: String = "",
    val order: Int = 0,
) : Parcelable