package com.vocab300.app.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import com.vocab300.app.domain.Language
import java.util.Locale

sealed class TtsAvailability {
    object Unknown : TtsAvailability()
    object Available : TtsAvailability()
    object Unavailable : TtsAvailability()
}

/**
 * Bọc android.speech.tts.TextToSpeech để phát âm tiếng Anh / tiếng Hàn.
 * Nếu thiết bị không có giọng đọc phù hợp, KHÔNG throw/crash — chỉ trả về trạng thái Unavailable
 * để UI hiển thị thông báo và cho phép bỏ qua câu nghe.
 */
class TtsManager(context: Context) {

    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null
    private var initSuccess = false

    private val availabilityByLanguage = mutableMapOf<Language, Boolean>()

    fun init(onReady: () -> Unit = {}) {
        if (tts != null) {
            onReady()
            return
        }
        runCatching {
            tts = TextToSpeech(appContext) { status ->
                initSuccess = status == TextToSpeech.SUCCESS
                onReady()
            }
        }.onFailure {
            initSuccess = false
            onReady()
        }
    }

    /** Kiểm tra thiết bị có giọng đọc phù hợp cho ngôn ngữ này không. An toàn, không throw. */
    fun isLanguageAvailable(language: Language): Boolean {
        availabilityByLanguage[language]?.let { return it }
        val engine = tts
        val available = if (!initSuccess || engine == null) {
            false
        } else {
            runCatching {
                val locale = localeFor(language)
                val result = engine.isLanguageAvailable(locale)
                result == TextToSpeech.LANG_AVAILABLE ||
                    result == TextToSpeech.LANG_COUNTRY_AVAILABLE ||
                    result == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE
            }.getOrDefault(false)
        }
        availabilityByLanguage[language] = available
        return available
    }

    /**
     * Phát âm [text] bằng ngôn ngữ [language]. Trả về true nếu yêu cầu phát được gửi thành công,
     * false nếu không khả dụng (UI nên hiển thị thông báo bỏ qua). Không bao giờ throw ra ngoài.
     */
    fun speak(text: String, language: Language): Boolean {
        val engine = tts ?: return false
        if (!isLanguageAvailable(language)) return false
        return runCatching {
            engine.language = localeFor(language)
            engine.stop()
            engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "vocab300_utt_${System.currentTimeMillis()}")
            true
        }.getOrDefault(false)
    }

    fun shutdown() {
        runCatching {
            tts?.stop()
            tts?.shutdown()
        }
        tts = null
    }

    private fun localeFor(language: Language): Locale = when (language) {
        Language.EN -> Locale.US
        Language.KO -> Locale.KOREAN
    }
}
