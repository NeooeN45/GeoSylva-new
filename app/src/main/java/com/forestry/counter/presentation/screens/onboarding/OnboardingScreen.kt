package com.forestry.counter.presentation.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EmojiNature
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.forestry.counter.R
import com.forestry.counter.presentation.theme.Primary
import kotlinx.coroutines.launch

private enum class PageCategory(val labelFr: String, val color: Color) {
    WELCOME("Bienvenue", Color(0xFF2E7D32)),
    INVENTORY("Inventaire", Color(0xFF00695C)),
    ANALYSIS("Analyse", Color(0xFF1565C0)),
    ECOLOGY("Écologie", Color(0xFF4527A0)),
    EXPORT("Export", Color(0xFF6A1B9A)),
    PRIVACY("RGPD", Color(0xFF37474F))
}

// Une fonctionnalité repliée dans une page-groupe (voir [OnboardingPage.subFeatures]) :
// une carte dépliable, pas une page entière à faire défiler.
private data class SubFeature(
    val icon: ImageVector,
    val titleRes: Int,
    val descRes: Int,
    val bulletResIds: List<Int> = emptyList(),
)

private data class OnboardingPage(
    val icon: ImageVector,
    val titleRes: Int,
    val descRes: Int,
    val bulletResIds: List<Int> = emptyList(),
    // Couleur de marque du thème, et non un vert littéral : la palette est la
    // seule source de vérité (voir `theme/Color.kt`).
    val accentColor: Color = Primary,
    val category: PageCategory = PageCategory.INVENTORY,
    val isHero: Boolean = false,
    // Pages-groupe (Collecte, Analyse, Écologie) : plusieurs fonctionnalités
    // proches réunies en cartes dépliables sur une seule page, au lieu d'une
    // page à part par fonctionnalité — moins de "Suivant" à enchaîner pour
    // parcourir le même contenu.
    val subFeatures: List<SubFeature> = emptyList(),
)

// 6 pages au lieu de 13 : les fonctionnalités proches (structuration,
// mesure, hauteur, GPS, carte / synthèse, martelage / station, climat,
// flore, IBP) sont regroupées par thème en cartes dépliables (voir
// [GroupPageContent]) plutôt qu'étalées sur une page chacune.
private val PAGES = listOf(
    OnboardingPage(
        Icons.Default.Park,
        R.string.onboarding_welcome_title,
        R.string.onboarding_welcome_desc,
        listOf(R.string.onboarding_welcome_b1, R.string.onboarding_welcome_b2, R.string.onboarding_welcome_b3),
        Color(0xFF2E7D32), PageCategory.WELCOME, isHero = true
    ),
    OnboardingPage(
        icon = Icons.Default.Forest,
        titleRes = R.string.onboarding_group_collecte_title,
        descRes = R.string.onboarding_group_collecte_desc,
        accentColor = Color(0xFF00695C),
        category = PageCategory.INVENTORY,
        subFeatures = listOf(
            SubFeature(Icons.Default.Forest, R.string.onboarding_forest_title, R.string.onboarding_forest_desc,
                listOf(R.string.onboarding_forest_b1, R.string.onboarding_forest_b2, R.string.onboarding_forest_b3)),
            SubFeature(Icons.Default.Straighten, R.string.onboarding_measure_title, R.string.onboarding_measure_desc,
                listOf(R.string.onboarding_measure_b1, R.string.onboarding_measure_b2, R.string.onboarding_measure_b3)),
            SubFeature(Icons.Default.Height, R.string.onboarding_height_title, R.string.onboarding_height_desc,
                listOf(R.string.onboarding_height_b1, R.string.onboarding_height_b2, R.string.onboarding_height_b3)),
            SubFeature(Icons.Default.GpsFixed, R.string.onboarding_gps_title, R.string.onboarding_gps_desc,
                listOf(R.string.onboarding_gps_b1, R.string.onboarding_gps_b2, R.string.onboarding_gps_b3)),
            SubFeature(Icons.Default.Map, R.string.onboarding_map_title, R.string.onboarding_map_desc,
                listOf(R.string.onboarding_map_b1, R.string.onboarding_map_b2, R.string.onboarding_map_b3)),
        ),
    ),
    OnboardingPage(
        icon = Icons.Default.BarChart,
        titleRes = R.string.onboarding_group_analyse_title,
        descRes = R.string.onboarding_group_analyse_desc,
        accentColor = Color(0xFFE65100),
        category = PageCategory.ANALYSIS,
        subFeatures = listOf(
            SubFeature(Icons.Default.BarChart, R.string.onboarding_synthesis_title, R.string.onboarding_synthesis_desc,
                listOf(R.string.onboarding_synthesis_b1, R.string.onboarding_synthesis_b2, R.string.onboarding_synthesis_b3)),
            SubFeature(Icons.Default.Science, R.string.onboarding_marking_title, R.string.onboarding_marking_desc,
                listOf(R.string.onboarding_marking_b1, R.string.onboarding_marking_b2, R.string.onboarding_marking_b3)),
        ),
    ),
    OnboardingPage(
        icon = Icons.Default.Eco,
        titleRes = R.string.onboarding_group_ecologie_title,
        descRes = R.string.onboarding_group_ecologie_desc,
        accentColor = Color(0xFF2E7D32),
        category = PageCategory.ECOLOGY,
        subFeatures = listOf(
            SubFeature(Icons.Default.Terrain, R.string.onboarding_station_title, R.string.onboarding_station_desc,
                listOf(R.string.onboarding_station_b1, R.string.onboarding_station_b2, R.string.onboarding_station_b3)),
            SubFeature(Icons.Default.WbSunny, R.string.onboarding_corr_title, R.string.onboarding_corr_desc,
                listOf(R.string.onboarding_corr_b1, R.string.onboarding_corr_b2, R.string.onboarding_corr_b3)),
            SubFeature(Icons.Default.Eco, R.string.onboarding_flora_title, R.string.onboarding_flora_desc,
                listOf(R.string.onboarding_flora_b1, R.string.onboarding_flora_b2, R.string.onboarding_flora_b3)),
            SubFeature(Icons.Default.EmojiNature, R.string.onboarding_ibp_title, R.string.onboarding_ibp_desc,
                listOf(R.string.onboarding_ibp_b1, R.string.onboarding_ibp_b2, R.string.onboarding_ibp_b3)),
        ),
    ),
    OnboardingPage(
        Icons.Default.PictureAsPdf,
        R.string.onboarding_export_title,
        R.string.onboarding_export_desc,
        listOf(R.string.onboarding_export_b1, R.string.onboarding_export_b2, R.string.onboarding_export_b3),
        Color(0xFF4527A0), PageCategory.EXPORT
    ),
    OnboardingPage(
        Icons.Default.Security,
        R.string.onboarding_privacy_title,
        R.string.onboarding_privacy_desc,
        listOf(R.string.onboarding_privacy_b1, R.string.onboarding_privacy_b2, R.string.onboarding_privacy_b3),
        Color(0xFF37474F), PageCategory.PRIVACY
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { PAGES.size })
    val scope = rememberCoroutineScope()
    val currentPage = PAGES[pagerState.currentPage]
    val isLastPage = pagerState.currentPage == PAGES.size - 1
    val context = LocalContext.current
    var showDeclineDialog by remember { mutableStateOf(false) }

    val bgColor by animateColorAsState(
        targetValue = currentPage.accentColor.copy(alpha = 0.06f),
        animationSpec = tween(600, easing = EaseInOutCubic),
        label = "bgColor"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Ambient glow background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(bgColor, Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar: category chip + skip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedContent(
                    targetState = currentPage.category,
                    transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                    label = "categoryChip"
                ) { cat ->
                    Surface(
                        shape = CircleShape,
                        color = cat.color.copy(alpha = 0.12f),
                        modifier = Modifier.border(
                            width = 1.dp,
                            color = cat.color.copy(alpha = 0.35f),
                            shape = CircleShape
                        )
                    ) {
                        Text(
                            text = cat.labelFr,
                            style = MaterialTheme.typography.labelSmall,
                            color = cat.color,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }
                AnimatedVisibility(
                    visible = !isLastPage,
                    enter = fadeIn(tween(200)),
                    exit = fadeOut(tween(200))
                ) {
                    TextButton(onClick = onComplete) {
                        Text(
                            stringResource(R.string.onboarding_skip),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Progress bar continue
            LinearProgressIndicator(
                progress = { (pagerState.currentPage + 1).toFloat() / PAGES.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(3.dp)
                    .clip(CircleShape),
                color = currentPage.accentColor,
                trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 0.dp)
            ) { pageIdx ->
                val offsetFraction = (pagerState.currentPage - pageIdx) + pagerState.currentPageOffsetFraction
                PageContent(
                    page = PAGES[pageIdx],
                    pageOffset = offsetFraction
                )
            }

            // Dot indicators (segmented by category)
            Row(
                modifier = Modifier.padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PAGES.forEachIndexed { index, page ->
                    val isSelected = pagerState.currentPage == index
                    val isPast = index < pagerState.currentPage
                    val dotWidth by animateDpAsState(
                        targetValue = when {
                            isSelected -> 24.dp
                            else -> 7.dp
                        },
                        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
                        label = "dotWidth_$index"
                    )
                    val dotColor = when {
                        isSelected -> currentPage.accentColor
                        isPast -> currentPage.accentColor.copy(alpha = 0.45f)
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    }
                    Box(
                        modifier = Modifier
                            .height(7.dp)
                            .width(dotWidth)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }

            // Page counter
            Text(
                text = "${pagerState.currentPage + 1} / ${PAGES.size}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // CTA Button
            Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                Button(
                    onClick = {
                        if (isLastPage) {
                            onComplete()
                        } else {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = currentPage.accentColor),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    AnimatedContent(
                        targetState = isLastPage,
                        transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) },
                        label = "ctaText"
                    ) { last ->
                        Text(
                            text = if (last) stringResource(R.string.onboarding_accept)
                            else stringResource(R.string.onboarding_next),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Decline button — visible only on the RGPD consent page (last page)
            AnimatedVisibility(
                visible = isLastPage,
                enter = fadeIn(tween(250)),
                exit = fadeOut(tween(150))
            ) {
                TextButton(
                    onClick = { showDeclineDialog = true },
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_decline),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Decline confirmation dialog
    if (showDeclineDialog) {
        AlertDialog(
            onDismissRequest = { showDeclineDialog = false },
            title = { Text(stringResource(R.string.onboarding_decline)) },
            text = { Text(stringResource(R.string.onboarding_decline_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeclineDialog = false
                    (context as? Activity)?.finishAffinity()
                }) {
                    Text(stringResource(R.string.onboarding_decline))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeclineDialog = false }) {
                    Text(stringResource(R.string.onboarding_accept))
                }
            }
        )
    }
}

@Composable
private fun PageContent(page: OnboardingPage, pageOffset: Float = 0f) {
    val absOffset = kotlin.math.abs(pageOffset)
    val scale by animateFloatAsState(
        targetValue = 1f - (absOffset * 0.08f).coerceIn(0f, 0.08f),
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "pageScale"
    )
    val alpha by animateFloatAsState(
        targetValue = 1f - (absOffset * 0.55f).coerceIn(0f, 0.55f),
        animationSpec = tween(280),
        label = "pageAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { scaleX = scale; scaleY = scale; this.alpha = alpha }
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Hero icon with layered glow rings
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(156.dp)) {
            // Outer glow ring
            Box(
                modifier = Modifier
                    .size(156.dp)
                    .clip(CircleShape)
                    .background(page.accentColor.copy(alpha = 0.06f))
                    .border(1.dp, page.accentColor.copy(alpha = 0.12f), CircleShape)
            )
            // Middle ring
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(page.accentColor.copy(alpha = 0.10f))
                    .border(1.dp, page.accentColor.copy(alpha = 0.2f), CircleShape)
            )
            // Inner filled circle
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                page.accentColor.copy(alpha = 0.28f),
                                page.accentColor.copy(alpha = 0.14f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    modifier = Modifier.size(42.dp),
                    tint = page.accentColor
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        when {
            page.isHero -> HeroWelcomeContent(page)
            page.subFeatures.isNotEmpty() -> GroupPageContent(page)
            else -> StandardPageContent(page)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun HeroWelcomeContent(page: OnboardingPage) {
    Text(
        text = "GeoSylva",
        style = MaterialTheme.typography.displaySmall,
        fontWeight = FontWeight.ExtraBold,
        color = page.accentColor,
        textAlign = TextAlign.Center,
        letterSpacing = (-0.5).sp
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        // Auparavant `stringResource(page.titleRes).substringAfter(" ")`, qui
        // amputait le titre du premier mot et affichait « sur GeoSylva » sous
        // « GeoSylva ». Le sous-titre est désormais une chaîne à part entière.
        text = stringResource(R.string.onboarding_welcome_tagline),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(14.dp))
    Text(
        text = stringResource(page.descRes),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 26.sp
    )
    Spacer(modifier = Modifier.height(24.dp))
    // Feature badges row
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        modifier = Modifier.fillMaxWidth()
    ) {
        page.bulletResIds.forEach { resId ->
            FeaturePill(text = stringResource(resId), accentColor = page.accentColor)
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
    // Version badge
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    ) {
        Text(
            text = stringResource(R.string.onboarding_version_info),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun FeaturePill(text: String, accentColor: Color) {
    val truncated = if (text.length > 22) text.take(20) + "…" else text
    Surface(
        shape = CircleShape,
        color = accentColor.copy(alpha = 0.10f),
        modifier = Modifier.border(1.dp, accentColor.copy(alpha = 0.25f), CircleShape)
    ) {
        Text(
            text = truncated,
            style = MaterialTheme.typography.labelSmall,
            color = accentColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            maxLines = 1
        )
    }
}

@Composable
private fun StandardPageContent(page: OnboardingPage) {
    Text(
        text = stringResource(page.titleRes),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onBackground,
        lineHeight = 34.sp
    )
    Spacer(modifier = Modifier.height(10.dp))
    Text(
        text = stringResource(page.descRes),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 25.sp
    )
    if (page.bulletResIds.isNotEmpty()) {
        Spacer(modifier = Modifier.height(20.dp))
        FeatureCard(page = page)
    }
}

// Page-groupe : titre + description du thème, puis les fonctionnalités
// du groupe en cartes dépliables (voir [SubFeatureRow]) — remplace ce qui
// était auparavant 2 à 5 pages "Suivant" par thème par une seule page où
// on touche ce qui intéresse.
@Composable
private fun GroupPageContent(page: OnboardingPage) {
    Text(
        text = stringResource(page.titleRes),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onBackground,
        lineHeight = 34.sp
    )
    Spacer(modifier = Modifier.height(10.dp))
    Text(
        text = stringResource(page.descRes),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 25.sp
    )
    Spacer(modifier = Modifier.height(20.dp))
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        page.subFeatures.forEach { feature ->
            SubFeatureRow(feature = feature, accentColor = page.accentColor)
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
    Text(
        text = stringResource(R.string.onboarding_group_hint),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        textAlign = TextAlign.Center
    )
}

// Carte dépliable d'une fonctionnalité au sein d'une page-groupe. L'anim
// est déclenchée par l'interaction (tap → déplier/replier), jamais par le
// défilement ou le changement de page — même principe que le correctif
// Explorer : une carte doit être immédiatement disponible, pas attendre
// une animation d'apparition liée au geste qui l'amène à l'écran.
@Composable
private fun SubFeatureRow(feature: SubFeature, accentColor: Color) {
    var expanded by rememberSaveable(feature.titleRes) { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(220),
        label = "chevronRotation"
    )

    Card(
        onClick = { expanded = !expanded },
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = if (expanded) 0.4f else 0.18f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = feature.icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(feature.titleRes),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (!expanded) {
                        Text(
                            text = stringResource(feature.descRes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer { rotationZ = chevronRotation }
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(180)) + expandVertically(tween(220)),
                exit = fadeOut(tween(140)) + shrinkVertically(tween(180))
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Text(
                        text = stringResource(feature.descRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    feature.bulletResIds.forEach { bulletRes ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.padding(vertical = 3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 7.dp, end = 8.dp)
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(accentColor)
                            )
                            Text(
                                text = stringResource(bulletRes),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureCard(page: OnboardingPage) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Brush.linearGradient(
                colors = listOf(
                    page.accentColor.copy(alpha = 0.3f),
                    page.accentColor.copy(alpha = 0.08f)
                )
            )
        )
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            page.bulletResIds.forEachIndexed { index, resId ->
                Row(verticalAlignment = Alignment.Top) {
                    // Numbered badge
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(page.accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = page.accentColor,
                            fontSize = 10.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(resId),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}
