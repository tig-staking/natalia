package com.tigstaking.natalia.game.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface SpanishSpeechStatus {
    data object Loading : SpanishSpeechStatus
    data object Ready : SpanishSpeechStatus
    data class Unavailable(val reason: String) : SpanishSpeechStatus
}

/** Small lifecycle-owned wrapper for the platform Spanish voice. */
class SpanishSpeechController(context: Context) : AutoCloseable {
    private val appContext = context.applicationContext
    private val mutableStatus = MutableStateFlow<SpanishSpeechStatus>(SpanishSpeechStatus.Loading)
    val status: StateFlow<SpanishSpeechStatus> = mutableStatus.asStateFlow()

    private var textToSpeech: TextToSpeech? = null

    init {
        textToSpeech = TextToSpeech(appContext, ::onInitialized)
    }

    private fun onInitialized(result: Int) {
        val engine = textToSpeech ?: return
        if (result != TextToSpeech.SUCCESS) {
            mutableStatus.value = SpanishSpeechStatus.Unavailable("Nie udało się uruchomić syntezatora mowy.")
            return
        }

        val spanishSpain = Locale.forLanguageTag("es-ES")
        val availability = engine.isLanguageAvailable(spanishSpain)
        if (availability < TextToSpeech.LANG_AVAILABLE) {
            mutableStatus.value = SpanishSpeechStatus.Unavailable("Brak głosu hiszpańskiego (es-ES) w urządzeniu.")
            return
        }

        val selected = engine.setLanguage(spanishSpain)
        mutableStatus.value = if (selected < TextToSpeech.LANG_AVAILABLE) {
            SpanishSpeechStatus.Unavailable("Głos hiszpański (es-ES) nie jest dostępny.")
        } else {
            SpanishSpeechStatus.Ready
        }
    }

    fun speak(text: String): Boolean {
        if (text.isBlank() || mutableStatus.value !is SpanishSpeechStatus.Ready) return false
        val result = runCatching {
            textToSpeech?.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "natalia-spanish-word",
            ) ?: TextToSpeech.ERROR
        }.getOrDefault(TextToSpeech.ERROR)
        if (result != TextToSpeech.SUCCESS) {
            mutableStatus.value = SpanishSpeechStatus.Unavailable("Nie udało się odtworzyć wymowy.")
            return false
        }
        return true
    }

    override fun close() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}
