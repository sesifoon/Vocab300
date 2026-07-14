package com.vocab300.app.data.repository

import com.vocab300.app.data.local.dao.VocabularyDao
import com.vocab300.app.data.local.entity.VocabularyWordEntity
import kotlinx.coroutines.flow.Flow

class VocabularyRepository(private val vocabularyDao: VocabularyDao) {

    fun observeAll(): Flow<List<VocabularyWordEntity>> = vocabularyDao.observeAll()

    fun observeByLanguage(language: String): Flow<List<VocabularyWordEntity>> =
        vocabularyDao.observeByLanguage(language)

    suspend fun getByLanguage(language: String): List<VocabularyWordEntity> =
        vocabularyDao.getByLanguage(language)

    suspend fun getById(id: String): VocabularyWordEntity? = vocabularyDao.getById(id)

    suspend fun getByIds(ids: List<String>): List<VocabularyWordEntity> = vocabularyDao.getByIds(ids)

    suspend fun search(query: String): List<VocabularyWordEntity> = vocabularyDao.search(query)

    suspend fun totalCount(): Int = vocabularyDao.count()
}
