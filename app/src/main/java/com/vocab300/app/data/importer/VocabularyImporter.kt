package com.vocab300.app.data.importer

import android.content.Context
import com.vocab300.app.data.datastore.UserPrefsManager
import com.vocab300.app.data.local.dao.VocabularyDao
import com.vocab300.app.data.local.entity.VocabularyWordEntity
import com.vocab300.app.domain.ImportDecision
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

/**
 * Đọc file assets/vocabulary.json (đúng 300 từ đã chuẩn hoá từ file nguồn của người dùng)
 * và import vào Room MỘT LẦN DUY NHẤT khi ứng dụng chạy lần đầu.
 *
 * Cơ chế chống import trùng:
 * 1. Kiểm tra cờ `import_done` trong DataStore.
 * 2. Kiểm tra thêm số dòng hiện có trong bảng vocabulary_words (phòng trường hợp cờ bị mất
 *    nhưng DB vẫn còn dữ liệu, hoặc ngược lại).
 * Quyết định cuối cùng nằm ở [ImportDecision.shouldImport] (logic thuần, có unit test riêng).
 */
class VocabularyImporter(
    private val context: Context,
    private val vocabularyDao: VocabularyDao,
    private val userPrefsManager: UserPrefsManager
) {

    companion object {
        const val ASSET_FILE_NAME = "vocabulary.json"
        const val EXPECTED_WORD_COUNT = 300
    }

    suspend fun importIfNeeded() = withContext(Dispatchers.IO) {
        val importFlagSet = userPrefsManager.isImportDone()
        val currentCount = vocabularyDao.count()

        if (!ImportDecision.shouldImport(importFlagSet, currentCount, EXPECTED_WORD_COUNT)) {
            // Đảm bảo cờ được set đúng nếu DB đã đủ dữ liệu nhưng cờ vì lý do nào đó chưa set
            if (currentCount >= EXPECTED_WORD_COUNT && !importFlagSet) {
                userPrefsManager.setImportDone()
            }
            return@withContext
        }

        val jsonText = context.assets.open(ASSET_FILE_NAME).bufferedReader(Charsets.UTF_8).use { it.readText() }
        val jsonArray = JSONArray(jsonText)
        val words = mutableListOf<VocabularyWordEntity>()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            words.add(
                VocabularyWordEntity(
                    id = obj.getString("id"),
                    language = obj.getString("language"),
                    word = obj.getString("word"),
                    meaningVi = obj.getString("meaningVi"),
                    pronunciation = obj.optStringOrNull("pronunciation"),
                    example = obj.optStringOrNull("example"),
                    category = obj.getString("category"),
                    imageKey = obj.optStringOrNull("imageKey"),
                    sequenceOrder = obj.getInt("sequenceOrder")
                )
            )
        }

        vocabularyDao.insertAll(words)
        userPrefsManager.setImportDone()
    }
}

private fun org.json.JSONObject.optStringOrNull(key: String): String? {
    if (isNull(key) || !has(key)) return null
    val value = optString(key, "")
    return value.ifBlank { null }
}
