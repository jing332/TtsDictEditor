package com.github.jing332.tts_dict_editor.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.github.jing332.tts_dict_editor.data.entites.DictFile
import kotlinx.coroutines.flow.Flow

@Dao
interface DictFileDao {
    @get:Query("SELECT * FROM dict_files ORDER BY `order` ASC")
    val all: List<DictFile>

    @get:Query("SELECT * FROM dict_files ORDER BY `order` ASC")
    val flowAll: Flow<List<DictFile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg args: DictFile)

    @Update
    fun update(vararg args: DictFile)

    @Delete
    fun delete(vararg args: DictFile)
}