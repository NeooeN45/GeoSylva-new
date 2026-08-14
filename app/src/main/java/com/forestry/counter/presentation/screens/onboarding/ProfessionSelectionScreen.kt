package com.forestry.counter.presentation.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.forestry.counter.R
import com.forestry.counter.presentation.theme.GsShape
import com.forestry.counter.presentation.theme.Motion
import com.forestry.counter.presentation.theme.Space

/**
 * Sélection du métier — juste après connexion ou "Continuer hors ligne",
 * avant l'onboarding (spec de session : demandé pour orienter de futures
 * adaptations d'UI/UX par métier). Liste prédéfinie interactive, plus un
 * champ libre pour le métier absent de la liste : cette valeur remonte
 * telle quelle (voir [ProfessionOption.OTHER_CODE] et
 * `UserPreferencesManager.userProfessionCustomText`) pour qu'on puisse
 * l'ajouter à la liste si plusieurs personnes la demandent.
 */
private data class ProfessionOption(
    val code: String,
    val labelRes: Int,
    val icon: ImageVector,
) {
    companion object {
        const val OTHER_CODE = "autre"
    }
}

private val PROFESSION_OPTIONS = listOf(
    ProfessionOption("forestier_onf_cnpf", R.string.profession_forestier_public, Icons.Filled.Forest),
    ProfessionOption("expert_forestier", R.string.profession_expert_forestier, Icons.Filled.WorkspacePremium),
    ProfessionOption("exploitant_forestier", R.string.profession_exploitant, Icons.Filled.Agriculture),
    ProfessionOption("technicien_forestier", R.string.profession_technicien, Icons.Filled.Engineering),
    ProfessionOption("proprietaire_forestier", R.string.profession_proprietaire, Icons.Filled.HomeWork),
    ProfessionOption("etudiant", R.string.profession_etudiant, Icons.Filled.School),
    ProfessionOption("chercheur_enseignant", R.string.profession_chercheur, Icons.Filled.Science),
    ProfessionOption(ProfessionOption.OTHER_CODE, R.string.profession_autre, Icons.Filled.MoreHoriz),
)

@Composable
fun ProfessionSelectionScreen(
    onComplete: (code: String, customText: String?) -> Unit,
) {
    var selectedCode by rememberSaveable { mutableStateOf<String?>(null) }
    var customText by rememberSaveable { mutableStateOf("") }
    val isOther = selectedCode == ProfessionOption.OTHER_CODE
    val canContinue = selectedCode != null && (!isOther || customText.isNotBlank())

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.forest_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Space.screenH, vertical = Space.xl),
        ) {
                Surface(
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.72f),
                    shape = GsShape.lg,
                ) {
                    Column(modifier = Modifier.padding(Space.md)) {
                        Text(
                            text = stringResource(R.string.profession_title),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(R.string.profession_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = Space.xxs),
                        )
                    }
                }

                Column(
                    modifier = Modifier.padding(top = Space.lg),
                    verticalArrangement = Arrangement.spacedBy(Space.sm),
                ) {
                    // Grille manuelle (2 colonnes) plutôt qu'une
                    // LazyVerticalGrid : 8 éléments fixes dans une page déjà
                    // défilante — pas besoin de virtualisation, et ça évite
                    // le calcul de hauteur imposée par une grille paresseuse
                    // imbriquée dans un `verticalScroll`.
                    PROFESSION_OPTIONS.chunked(2).forEach { rowOptions ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Space.sm),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            rowOptions.forEach { option ->
                                ProfessionCard(
                                    option = option,
                                    selected = selectedCode == option.code,
                                    onClick = { selectedCode = option.code },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (rowOptions.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = isOther,
                    enter = fadeIn(Motion.springSnappy()) + expandVertically(Motion.springSnappy()),
                    exit = fadeOut(Motion.springSnappy()) + shrinkVertically(Motion.springSnappy()),
                ) {
                    OutlinedTextField(
                        value = customText,
                        onValueChange = { customText = it },
                        label = { Text(stringResource(R.string.profession_custom_label)) },
                        placeholder = { Text(stringResource(R.string.profession_custom_placeholder)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Space.md),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.72f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.72f),
                        ),
                        shape = GsShape.md,
                    )
                }

                Text(
                    text = stringResource(R.string.profession_privacy_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Space.md),
                )

                Button(
                    onClick = {
                        val code = selectedCode ?: return@Button
                        onComplete(code, if (isOther) customText.trim() else null)
                    },
                    enabled = canContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Space.lg),
                    shape = GsShape.pill,
                ) {
                    Text(stringResource(R.string.continue_label))
                }
            }
        }
    }

@Composable
private fun ProfessionCard(
    option: ProfessionOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) Motion.PRESS_SCALE else 1f,
        animationSpec = Motion.springSnappy(),
        label = "professionCardScale",
    )

    Card(
        onClick = onClick,
        modifier = modifier
            .aspectRatio(1.05f)
            .scale(scale),
        shape = GsShape.lg,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f)
            },
        ),
        border = if (selected) {
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null,
        interactionSource = interactionSource,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (selected) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(Space.xs)
                        .size(22.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Space.sm),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    },
                    modifier = Modifier.size(40.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = option.icon,
                            contentDescription = null,
                            tint = if (selected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            },
                        )
                    }
                }
                Text(
                    text = stringResource(option.labelRes),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = Space.xs),
                )
            }
        }
    }
}
