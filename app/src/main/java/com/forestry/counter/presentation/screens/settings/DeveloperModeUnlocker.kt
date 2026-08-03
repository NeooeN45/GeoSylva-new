package com.forestry.counter.presentation.screens.settings

sealed interface DeveloperUnlockResult {
    data class Progress(val remainingTaps: Int) : DeveloperUnlockResult
    data object Enabled : DeveloperUnlockResult
    data object Open : DeveloperUnlockResult
}

/** Compteur local inspiré des options développeur Android. */
class DeveloperModeUnlocker(
    private val requiredTaps: Int = 8,
    private val resetAfterMillis: Long = 4_000L,
) {
    private var taps = 0
    private var lastTapAtMillis = 0L

    fun registerTap(nowMillis: Long, alreadyEnabled: Boolean): DeveloperUnlockResult {
        if (alreadyEnabled) return DeveloperUnlockResult.Open
        if (nowMillis - lastTapAtMillis > resetAfterMillis) taps = 0
        lastTapAtMillis = nowMillis
        taps += 1
        val remaining = (requiredTaps - taps).coerceAtLeast(0)
        return if (remaining == 0) {
            taps = 0
            DeveloperUnlockResult.Enabled
        } else {
            DeveloperUnlockResult.Progress(remaining)
        }
    }
}
