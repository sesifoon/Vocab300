package com.vocab300.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 2,
    exportSchema = false
)
abstract class Vocab300Database : RoomDatabase() {

    abstract fun vocabularyDao(): VocabularyDao
    abstract fun progressDao(): ProgressDao
    abstract fun lessonProgressDao(): LessonProgressDao

    companion object {
        private const val DB_NAME = "vocab300.db"

        /**
         * v1.1.0: thêm các cột mở rộng cho vocabulary_words (romanization, exampleTranslationVi,
         * difficulty, tags, abbreviation, aliases, keywords, sourceVersion) để chuẩn bị cho bộ dữ
         * liệu lớn hơn 300 từ trong tương lai. CHỈ thêm cột mới (ADD COLUMN), KHÔNG xoá/đổi kiểu
         * cột nào, KHÔNG đụng tới word_progress hay lesson_progress -> tiến độ người dùng hiện có
         * (XP, streak, mastery, lịch sử) được bảo toàn hoàn toàn khi cập nhật app.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE vocabulary_words ADD COLUMN romanization TEXT")
                db.execSQL("ALTER TABLE vocabulary_words ADD COLUMN exampleTranslationVi TEXT")
                db.execSQL("ALTER TABLE vocabulary_words ADD COLUMN difficulty TEXT")
                db.execSQL("ALTER TABLE vocabulary_words ADD COLUMN tags TEXT")
                db.execSQL("ALTER TABLE vocabulary_words ADD COLUMN abbreviation INTEGER")
                db.execSQL("ALTER TABLE vocabulary_words ADD COLUMN aliases TEXT")
                db.execSQL("ALTER TABLE vocabulary_words ADD COLUMN keywords TEXT")
                db.execSQL("ALTER TABLE vocabulary_words ADD COLUMN sourceVersion TEXT NOT NULL DEFAULT 'v1'")
            }
        }

        @Volatile
        private var INSTANCE: Vocab300Database? = null

        fun getInstance(context: Context): Vocab300Database {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    Vocab300Database::class.java,
                    DB_NAME
                )
                    .addMigrations(MIGRATION_1_2)
                    .build().also { INSTANCE = it }
            }
        }
    }
}
