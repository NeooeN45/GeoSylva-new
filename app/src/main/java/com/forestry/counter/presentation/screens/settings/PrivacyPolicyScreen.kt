package com.forestry.counter.presentation.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.forestry.counter.R

/**
 * Niveau de titre markdown détecté pour une ligne de la politique.
 */
private enum class MarkdownHeadingLevel { TITLE, SECTION, SUBSECTION, NONE }

/**
 * Représente une ligne de la politique de confidentialité avec son niveau
 * de titre pour un rendu différencié (gras / normal).
 */
private data class PolicyLine(
    val text: String,
    val headingLevel: MarkdownHeadingLevel,
)

private const val HASH_TITLE = "# "
private const val HASH_SECTION = "## "
private const val HASH_SUBSECTION = "### "

/**
 * Écran affichant la politique de confidentialité complète de GeoSylva.
 *
 * Le contenu est embarqué comme ressource raw (`privacy_policy.md`) et
 * affiché en texte brut dans une LazyColumn. Les titres markdown (`#`,
 * `##`, `###`) sont détectés et rendus en gras pour faciliter la lecture.
 * Un rendu markdown complet serait excessif pour ce document statique.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val policyLines = remember {
        loadPrivacyPolicyLines(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.view_privacy_policy)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(policyLines) { line -> PolicyLineItem(line) }
        }
    }
}

@Composable
private fun PolicyLineItem(line: PolicyLine) {
    if (line.text.isBlank()) {
        // Ligne vide : on affiche un petit espacement pour aérer la lecture.
        Text(text = " ", style = MaterialTheme.typography.bodySmall)
        return
    }
    when (line.headingLevel) {
        MarkdownHeadingLevel.TITLE -> Text(
            text = line.text,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        MarkdownHeadingLevel.SECTION -> Text(
            text = line.text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        MarkdownHeadingLevel.SUBSECTION -> Text(
            text = line.text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        MarkdownHeadingLevel.NONE -> Text(
            text = line.text,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

/**
 * Charge la politique depuis la ressource raw et découpe chaque ligne
 * en détectant le niveau de titre markdown.
 */
private fun loadPrivacyPolicyLines(context: android.content.Context): List<PolicyLine> {
    val rawText = runCatching {
        context.resources.openRawResource(R.raw.privacy_policy).bufferedReader()
            .use { it.readText() }
    }.getOrElse { return emptyList() }

    return rawText.lines().map { rawLine ->
        when {
            rawLine.startsWith(HASH_TITLE) -> PolicyLine(
                text = rawLine.removePrefix(HASH_TITLE).trim(),
                headingLevel = MarkdownHeadingLevel.TITLE,
            )
            rawLine.startsWith(HASH_SECTION) -> PolicyLine(
                text = rawLine.removePrefix(HASH_SECTION).trim(),
                headingLevel = MarkdownHeadingLevel.SECTION,
            )
            rawLine.startsWith(HASH_SUBSECTION) -> PolicyLine(
                text = rawLine.removePrefix(HASH_SUBSECTION).trim(),
                headingLevel = MarkdownHeadingLevel.SUBSECTION,
            )
            else -> PolicyLine(
                text = rawLine,
                headingLevel = MarkdownHeadingLevel.NONE,
            )
        }
    }
}
