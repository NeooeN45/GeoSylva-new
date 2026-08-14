package com.forestry.counter.presentation.coachmark

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.forestry.counter.R
import com.forestry.counter.presentation.theme.GsShape
import com.forestry.counter.presentation.theme.Space
import kotlin.math.roundToInt

/**
 * Visite guidée modale des onglets principaux.
 *
 * Le [Dialog] isole le focus d'accessibilité et interdit toute interaction
 * avec l'écran sous-jacent. La bulle reste bornée et défilable pour les
 * petits écrans, le paysage, l'écran partagé et les grandes polices.
 */
@Composable
fun CoachMarkOverlay(
    anchors: CoachMarkAnchorRegistry,
    step: CoachMarkStep?,
    stepIndex: Int,
    totalSteps: Int,
    onNext: () -> Unit,
    onSkip: () -> Unit,
) {
    val currentStep = step ?: return
    val title = stringResource(currentStep.titleRes)
    val description = stringResource(currentStep.descRes)
    val focusRequester = remember(currentStep.anchorKey) { FocusRequester() }

    Dialog(
        onDismissRequest = onSkip,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .semantics {
                    paneTitle = title
                    liveRegion = LiveRegionMode.Polite
                },
        ) {
            val density = LocalDensity.current
            val screenWidthPx = with(density) { maxWidth.toPx() }
            val anchor = anchors.rectFor(currentStep.anchorKey)
            val isLast = stepIndex == totalSteps - 1

            // Le gestionnaire de gestes absorbe aussi les taps dans la zone
            // transparente du spotlight : la navigation située dessous ne
            // peut jamais être actionnée pendant la visite.
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) { detectTapGestures(onTap = {}) }
                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen },
            ) {
                drawRect(color = Color.Black.copy(alpha = 0.72f))
                if (anchor != null) {
                    val spotlightPadding = 8.dp.toPx()
                    drawRoundRect(
                        color = Color.Transparent,
                        topLeft = Offset(
                            anchor.left - spotlightPadding,
                            anchor.top - spotlightPadding,
                        ),
                        size = Size(
                            anchor.width + spotlightPadding * 2,
                            anchor.height + spotlightPadding * 2,
                        ),
                        cornerRadius = CornerRadius(18.dp.toPx()),
                        blendMode = BlendMode.Clear,
                    )
                }
            }

            val horizontalShiftPx = anchor?.let { rect ->
                val screenCenter = screenWidthPx / 2f
                val halfBubbleWidth = with(density) { 180.dp.toPx() }
                val maxShift = (screenWidthPx / 2f - halfBubbleWidth).coerceAtLeast(0f)
                (rect.center.x - screenCenter).coerceIn(-maxShift, maxShift)
            } ?: 0f
            val bubbleBottomPadding = minOf(108.dp, maxHeight / 4)
            val bubbleMaxHeight = (maxHeight - bubbleBottomPadding - Space.lg)
                .coerceAtLeast(160.dp)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding(),
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = GsShape.lg,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = Space.lg)
                        .padding(bottom = bubbleBottomPadding)
                        .widthIn(max = 360.dp)
                        .fillMaxWidth()
                        .heightIn(max = bubbleMaxHeight)
                        .offset { IntOffset(horizontalShiftPx.roundToInt(), 0) }
                        .focusRequester(focusRequester)
                        .focusable(),
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(Space.md),
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = Space.xxs, bottom = Space.sm),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                repeat(totalSteps) { dotIndex ->
                                    Box(
                                        modifier = Modifier
                                            .padding(vertical = 4.dp)
                                            .size(if (dotIndex == stepIndex) 8.dp else 6.dp)
                                            .background(
                                                color = if (dotIndex == stepIndex) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                                },
                                                shape = CircleShape,
                                            ),
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = onSkip) {
                                    Text(stringResource(R.string.coachmark_skip))
                                }
                                Button(onClick = onNext, shape = RoundedCornerShape(50)) {
                                    Text(
                                        if (isLast) stringResource(R.string.coachmark_finish)
                                        else stringResource(R.string.onboarding_next),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        LaunchedEffect(currentStep.anchorKey) {
            focusRequester.requestFocus()
        }
    }
}
