package com.vocab300.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vocab300.app.data.local.entity.WordProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: WordProgressEntity)

    @Query("SELECT * FROM word_progress WHERE wordId = :wordId")
    suspend fun getByWordId(wordId: String): WordProgressEntity?

    @Query("SELECT * FROM word_progress")
    fun observeAll(): Flow<List<WordProgressEntity>>

    @Query("SELECT * FROM word_progress")
    suspend fun getAll(): List<WordProgressEntity>

    @Query("SELECT * FROM word_progress WHERE wordId IN (:wordIds)")
    suspend fun getByWordIds(wordIds: List<String>): List<WordProgressEntity>

    @Query("SELECT * FROM word_progress WHERE isFavorite = 1")
    fun observeFavorites(): Flow<List<WordProgressEntity>>

    @Query("DELETE FROM word_progress")
    suspend fun clearAll()
}
