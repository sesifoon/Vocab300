package com.vocab300.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.vocab300.app.data.local.dao.LessonProgressDao
import com.vocab300.app.data.local.dao.ProgressDao
import com.vocab300.app.data.local.dao.VocabularyDao
import com.vocab300.app.data.local.entity.LessonProgressEntity
import com.vocab300.app.data.local.entity.VocabularyWordEntity
import com.vocab300.app.data.local.entity.WordProgressEntity

@Database(
    entities = [
        VocabularyWordEntity::class,
        WordProgressEntity::class,
        LessonProgressEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class Vocab300Database : RoomDatabase() {

    abstract fun vocabularyDao(): VocabularyDao
    abstract fun progressDao(): ProgressDao
    abstract fun lessonProgressDao(): LessonProgressDao

    companion object {
        private const val DB_NAME = "vocab300.db"

        @Volatile
        private var INSTANCE: Vocab300Database? = null

        fun getInstance(context: Context): Vocab300Database {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    Vocab300Database::class.java,
                    DB_NAME
                ).build().also { INSTANCE = it }
            }
        }
    }
}
