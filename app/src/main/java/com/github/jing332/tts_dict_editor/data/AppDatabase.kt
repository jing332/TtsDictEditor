package com.github.jing332.tts_dict_editor.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.github.jing332.tts_dict_editor.app
import com.github.jing332.tts_dict_editor.data.dao.DictFileDao
import com.github.jing332.tts_dict_editor.data.entites.DictFile

val appDb by lazy { AppDatabase.createDatabase(app) }

@Database(
    version = 1,
    entities = [DictFile::class],
)
abstract class AppDatabase : RoomDatabase() {
    abstract val dictFileDao: DictFileDao

    companion object {
        private const val DATABASE_NAME = "editor.db"

        fun createDatabase(context: Context) = Room
            .databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
            .allowMainThreadQueries()
            .build()
    }

}