package com.forestry.counter.presentation.components

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import com.forestry.counter.R
import com.forestry.counter.presentation.theme.GsShape

/**
 * Suggestion de français au tout premier lancement — écran de connexion.
 *
 * GeoSylva force le français par défaut (voir `UserPreferences.appLanguage`) :
 * ce dialogue ne se déclenche donc que dans le cas restant — un utilisateur
 * qui a explicitement choisi une autre langue dans les réglages, alors que
 * son téléphone reste localisé en France. Il ne s'affiche qu'une fois, que
 * la réponse soit oui ou non.
 *
 * La détection porte sur la **région système**, pas sur la langue affichée
 * par l'application : `Locale.getDefault()` refléterait la langue que
 * GeoSylva a déjà imposée, pas le réglage réel du téléphone. Seules les
 * locales système globales (`Resources.getSystem()`) restent hors de portée
 * de `AppCompatDelegate.setApplicationLocales`, qui ne s'applique qu'à
 * l'application courante.
 */
fun shouldSuggestFrench(context: Context, currentAppLanguage: String): Boolean {
    if (currentAppLanguage == "fr") return false
    val systemLocales = ConfigurationCompat.getLocales(
        android.content.res.Resources.getSystem().configuration
    )
    for (i in 0 until systemLocales.size()) {
        val locale = systemLocales[i] ?: continue
        if (locale.country.equals("FR", ignoreCase = true)) return true
    }
    return false
}

@Composable
fun LanguageSuggestionDialog(
    onAccept: () -> Unit,
    onDecline: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDecline,
        shape = GsShape.lg,
        title = { Text(stringResource(R.string.language_suggestion_title)) },
        text = { Text(stringResource(R.string.language_suggestion_body)) },
        confirmButton = {
            TextButton(onClick = onAccept) {
                Text(stringResource(R.string.language_suggestion_accept))
            }
        },
        dismissButton = {
            TextButton(onClick = onDecline) {
                Text(stringResource(R.string.language_suggestion_decline))
            }
        },
    )
}
