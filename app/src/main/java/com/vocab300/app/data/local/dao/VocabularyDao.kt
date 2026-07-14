package com.vocab300.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vocab300.app.data.local.entity.VocabularyWordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabularyDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(words: List<VocabularyWordEntity>)

    @Query("SELECT COUNT(*) FROM vocabulary_words")
    suspend fun count(): Int

    @Query("SELECT * FROM vocabulary_words ORDER BY language, sequenceOrder")
    fun observeAll(): Flow<List<VocabularyWordEntity>>

    @Query("SELECT * FROM vocabulary_words WHERE language = :language ORDER BY sequenceOrder")
    suspend fun getByLanguage(language: String): List<VocabularyWordEntity>

    @Query("SELECT * FROM vocabulary_words WHERE language = :language ORDER BY sequenceOrder")
    fun observeByLanguage(language: String): Flow<List<VocabularyWordEntity>>

    @Query("SELECT * FROM vocabulary_words WHERE id = :id")
    suspend fun getById(id: String): VocabularyWordEntity?

    @Query("SELECT * FROM vocabulary_words WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<VocabularyWordEntity>

    @Query(
        "SELECT * FROM vocabulary_words WHERE word LIKE '%' || :query || '%' " +
            "OR meaningVi LIKE '%' || :query || '%' ORDER BY language, sequenceOrder"
    )
    suspend fun search(query: String): List<VocabularyWordEntity>
}
