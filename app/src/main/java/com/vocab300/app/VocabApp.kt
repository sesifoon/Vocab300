package com.vocab300.app

import android.app.Application
import com.vocab300.app.data.datastore.UserPrefsManager
import com.vocab300.app.data.importer.VocabularyImporter
import com.vocab300.app.data.local.Vocab300Database
import com.vocab300.app.data.repository.ProgressRepository
import com.vocab300.app.data.repository.VocabularyRepository
import com.vocab300.app.tts.TtsManager

/**
 * Application class + service locator thủ công.
 * Dự án cố tình KHÔNG dùng Hilt/Dagger để giữ kiến trúc đơn giản, dễ đọc, đúng phạm vi yêu cầu
 * (MVVM + Room + DataStore, không thêm framework DI ngoài yêu cầu).
 */
class VocabApp : Application() {

    lateinit var database: Vocab300Database
        private set
    lateinit var userPrefsManager: UserPrefsManager
        private set
    lateinit var vocabularyRepository: VocabularyRepository
        private set
    lateinit var progressRepository: ProgressRepository
        private set
    lateinit var vocabularyImporter: VocabularyImporter
        private set
    lateinit var ttsManager: TtsManager
        private set

    override fun onCreate() {
        super.onCreate()

        database = Vocab300Database.getInstance(this)
        userPrefsManager = UserPrefsManager(this)
        vocabularyRepository = VocabularyRepository(database.vocabularyDao())
        progressRepository = ProgressRepository(
            progressDao = database.progressDao(),
            lessonProgressDao = database.lessonProgressDao(),
            userPrefsManager = userPrefsManager
        )
        vocabularyImporter = VocabularyImporter(
            context = this,
            vocabularyDao = database.vocabularyDao(),
            userPrefsManager = userPrefsManager
        )
        ttsManager = TtsManager(this)
        ttsManager.init()
    }

    override fun onTerminate() {
        ttsManager.shutdown()
        super.onTerminate()
    }
}
