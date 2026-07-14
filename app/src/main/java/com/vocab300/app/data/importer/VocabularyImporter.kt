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
 * Đọc file assets/vocabulary.json và import vào Room.
 *
 * v1.1.0: KHÔNG còn phụ thuộc vào số lượng từ cố định (300). Quyết định import dựa trên
 * `sourceVersion` của bộ dữ liệu (xem [ImportDecision.shouldImport]):
 * - Cài lần đầu (chưa có sourceVersion nào được lưu) -> import toàn bộ.
 * - `sourceVersion` trong asset thay đổi (VD: nâng từ 300 lên 1.000 từ) -> chạy lại import.
 *   Vì dùng `OnConflictStrategy.IGNORE`, các từ đã tồn tại (cùng `id`) KHÔNG bị ghi đè hay
 *   trùng lặp — chỉ những từ MỚI (id chưa có trong DB) được thêm vào. Tiến độ học (mastery, XP,
 *   streak, lịch ôn) của các từ cũ được bảo toàn hoàn toàn vì bảng word_progress không bị đụng tới.
 * - `sourceVersion` không đổi -> bỏ qua, không parse lại JSON mỗi lần mở app.
 *
 * DEFAULT_SOURCE_VERSION dùng khi 1 bản ghi trong asset không khai báo sourceVersion (an toàn cho
 * dữ liệu cũ chưa có trường này).
 */
class VocabularyImporter(
    private val context: Context,
    private val vocabularyDao: VocabularyDao,
    private val userPrefsManager: UserPrefsManager
) {

    companion object {
        const val ASSET_FILE_NAME = "vocabulary.json"
        const val DEFAULT_SOURCE_VERSION = "v1"
    }

    suspend fun importIfNeeded() = withContext(Dispatchers.IO) {
        val jsonText = context.assets.open(ASSET_FILE_NAME).bufferedReader(Charsets.UTF_8).use { it.readText() }
        val jsonArray = JSONArray(jsonText)

        // sourceVersion đại diện cho cả bộ dữ liệu = sourceVersion của bản ghi đầu tiên (mọi bản ghi
        // trong 1 lần xuất dữ liệu nên có cùng sourceVersion; xem DATA_IMPORT_GUIDE.md).
        val currentSourceVersion = if (jsonArray.length() > 0) {
            jsonArray.getJSONObject(0).optStringOrNull("sourceVersion") ?: DEFAULT_SOURCE_VERSION
        } else {
            DEFAULT_SOURCE_VERSION
        }

        val storedSourceVersion = userPrefsManager.getImportedSourceVersion()

        if (!ImportDecision.shouldImport(storedSourceVersion, currentSourceVersion)) {
            return@withContext
        }

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
                    sequenceOrder = obj.getInt("sequenceOrder"),
                    romanization = obj.optStringOrNull("romanization"),
                    exampleTranslationVi = obj.optStringOrNull("exampleTranslationVi"),
                    difficulty = obj.optStringOrNull("difficulty"),
                    tags = obj.optStringOrNull("tags"),
                    abbreviation = if (obj.has("abbreviation") && !obj.isNull("abbreviation")) obj.getBoolean("abbreviation") else null,
                    aliases = obj.optStringOrNull("aliases"),
                    keywords = obj.optStringOrNull("keywords"),
                    sourceVersion = obj.optStringOrNull("sourceVersion") ?: DEFAULT_SOURCE_VERSION
                )
            )
        }

        // IGNORE: từ đã có (cùng id) được giữ nguyên -> không ghi đè, không mất liên kết tiến độ.
        vocabularyDao.insertAll(words)
        userPrefsManager.setImportedSourceVersion(currentSourceVersion)
    }
}

private fun org.json.JSONObject.optStringOrNull(key: String): String? {
    if (isNull(key) || !has(key)) return null
    val value = optString(key, "")
    return value.ifBlank { null }
}
