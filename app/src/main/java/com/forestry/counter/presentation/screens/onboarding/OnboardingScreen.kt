package com.forestry.counter.presentation.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.forestry.counter.R
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val icon: ImageVector,
    val titleRes: Int,
    val descRes: Int,
    val bulletResIds: List<Int> = emptyList(),
    val accentColor: Color = Color(0xFF4CAF50)
)

private val pages = listOf(
    OnboardingPage(
        Icons.Default.Park,
        R.string.onboarding_welcome_title,
        R.string.onboarding_welcome_desc,
        listOf(R.string.onboarding_welcome_b1, R.string.onboarding_welcome_b2, R.string.onboarding_welcome_b3),
        Color(0xFF2E7D32)
    ),
    OnboardingPage(
        Icons.Default.Forest,
        R.string.onboarding_forest_title,
        R.string.onboarding_forest_desc,
        listOf(R.string.onboarding_forest_b1, R.string.onboarding_forest_b2, R.string.onboarding_forest_b3),
        Color(0xFF1B5E20)
    ),
    OnboardingPage(
        Icons.Default.Straighten,
        R.string.onboarding_measure_title,
        R.string.onboarding_measure_desc,
        listOf(R.string.onboarding_measure_b1, R.string.onboarding_measure_b2, R.string.onboarding_measure_b3),
        Color(0xFF00695C)
    ),
    OnboardingPage(
        Icons.Default.GpsFixed,
        R.string.onboarding_gps_title,
        R.string.onboarding_gps_desc,
        listOf(R.string.onboarding_gps_b1, R.string.onboarding_gps_b2, R.string.onboarding_gps_b3),
        Color(0xFF0277BD)
    ),
    OnboardingPage(
        Icons.Default.Map,
        R.string.onboarding_map_title,
        R.string.onboarding_map_desc,
        listOf(R.string.onboarding_map_b1, R.string.onboarding_map_b2, R.string.onboarding_map_b3),
        Color(0xFF4527A0)
    ),
    OnboardingPage(
        Icons.Default.BarChart,
        R.string.onboarding_synthesis_title,
        R.string.onboarding_synthesis_desc,
        listOf(R.string.onboarding_synthesis_b1, R.string.onboarding_synthesis_b2, R.string.onboarding_synthesis_b3),
        Color(0xFFE65100)
    ),
    OnboardingPage(
        Icons.Default.PictureAsPdf,
        R.string.onboarding_export_title,
        R.string.onboarding_export_desc,
        listOf(R.string.onboarding_export_b1, R.string.onboarding_export_b2, R.string.onboarding_export_b3),
        Color(0xFFC62828)
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.size - 1
    val currentAccent = pages[pagerState.currentPage].accentColor

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Skip button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                AnimatedVisibility(
                    visible = !isLastPage,
                    enter = fadeIn(tween(200)),
                    exit = fadeOut(tween(200))
                ) {
                    TextButton(onClick = onComplete) {
                        Text(stringResource(R.string.onboarding_skip))
                    }
                }
            }

            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                val pageOffset = (pagerState.currentPage - page) +
                        pagerState.currentPageOffsetFraction
                OnboardingPageContent(
                    page = pages[page],
                    pageOffset = pageOffset
                )
            }

            // Page indicators
            Row(
                modifier = Modifier.padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 28.dp else 8.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "indicatorWidth"
                    )
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) currentAccent
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                            )
                    )
                }
            }

            // Page counter
            Text(
                "${pagerState.currentPage + 1} / ${pages.size}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action button
            Button(
                onClick = {
                    if (isLastPage) {
                        onComplete()
                    } else {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = currentAccent
                )
            ) {
                Text(
                    text = if (isLastPage) stringResource(R.string.onboarding_start)
                    else stringResource(R.string.onboarding_next),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    pageOffset: Float = 0f
) {
    val scale by animateFloatAsState(
        targetValue = if (pageOffset == 0f) 1f else 0.85f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "pageScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (pageOffset == 0f) 1f else 0.5f,
        animationSpec = tween(300),
        label = "pageAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated icon circle with gradient
        Surface(
            modifier = Modifier.size(130.dp),
            shape = CircleShape,
            tonalElevation = 8.dp,
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                page.accentColor.copy(alpha = 0.2f),
                                page.accentColor.copy(alpha = 0.08f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = page.accentColor
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(page.titleRes),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(page.descRes),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        if (page.bulletResIds.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    page.bulletResIds.forEach { resId ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 7.dp)
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(page.accentColor)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(resId),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
