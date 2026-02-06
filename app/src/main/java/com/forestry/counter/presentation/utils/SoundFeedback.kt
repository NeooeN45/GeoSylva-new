package com.forestry.counter.presentation.utils

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

class SoundFeedback {
    private val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 80)

    fun click() {
        try {
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
        } catch (_: Exception) {}
    }
}

@Composable
fun rememberSoundFeedback(): SoundFeedback {
    return remember { SoundFeedback() }
}
