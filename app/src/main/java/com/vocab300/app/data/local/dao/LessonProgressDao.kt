package com.vocab300.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vocab300.app.data.local.entity.LessonProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: LessonProgressEntity)

    @Query("SELECT * FROM lesson_progress WHERE language = :language ORDER BY lessonIndex")
    fun observeByLanguage(language: String): Flow<List<LessonProgressEntity>>

    @Query("SELECT * FROM lesson_progress WHERE language = :language ORDER BY lessonIndex")
    suspend fun getByLanguage(language: String): List<LessonProgressEntity>

    @Query("SELECT * FROM lesson_progress WHERE language = :language AND lessonIndex = :lessonIndex")
    suspend fun getOne(language: String, lessonIndex: Int): LessonProgressEntity?

    @Query("DELETE FROM lesson_progress")
    suspend fun clearAll()
}
