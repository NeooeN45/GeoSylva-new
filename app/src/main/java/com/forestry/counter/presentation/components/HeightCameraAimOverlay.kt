package com.forestry.counter.presentation.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.forestry.counter.R
import com.forestry.counter.domain.location.AngleMeasurement
import kotlinx.coroutines.delay

/**
 * Viseur caméra AR plein-écran pour mesurer l'angle d'inclinaison vers la cime (ou la base)
 * d'un arbre. Affiche la caméra en arrière-plan avec un réticule, un arc de progression
 * d'auto-capture et la valeur de l'angle en temps réel.
 *
 * Le flux de capteur est déjà actif dans le dialogue parent — on passe simplement [liveAngle].
 */
@Composable
fun HeightCameraAimOverlay(
    liveAngle: AngleMeasurement?,
    isBaseAngle: Boolean,
    onCapture: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptic  = LocalHapticFeedback.current

    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
        )
    }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { permissionGranted = it }

    LaunchedEffect(Unit) {
        if (!permissionGranted) permLauncher.launch(Manifest.permission.CAMERA)
    }

    var captured          by remember { mutableStateOf(false) }
    var autoProgress      by remember { mutableStateOf(0f) }

    LaunchedEffect(captured, liveAngle) {
        if (captured) return@LaunchedEffect
        while (!captured) {
            delay(100L)
            val s = liveAngle?.stabilityScore ?: 0f
            if (s >= 0.82f) {
                autoProgress = (autoProgress + 0.067f).coerceAtMost(1f)
                if (autoProgress >= 1f) {
                    val avg = liveAngle?.avgPitchDeg?.toDouble()
                    if (avg != null) {
                        captured = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCapture(avg)
                    }
                }
            } else {
                autoProgress = (autoProgress - 0.15f).coerceAtLeast(0f)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // ── Arrière-plan : caméra ou fond noir avec message permission ────
            if (permissionGranted) {
                CameraPreviewView(modifier = Modifier.fillMaxSize())
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            stringResource(R.string.height_camera_permission_needed),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(onClick = { permLauncher.launch(Manifest.permission.CAMERA) }) {
                            Text(stringResource(R.string.height_camera_grant))
                        }
                    }
                }
            }

            // ── Canvas AR : réticule + arc de progression + ligne horizon ─────
            val stability   = liveAngle?.stabilityScore ?: 0f
            val angleDeg    = liveAngle?.avgPitchDeg ?: 0f
            val isStable    = stability >= 0.82f

            val crosshairColor = when {
                autoProgress >= 1f  -> Color(0xFF4CAF50)
                isStable            -> Color(0xFF81C784)
                stability > 0.45f   -> Color(0xFFFFEB3B)
                else                -> Color.White
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val arm    = 36.dp.toPx()
                val gap    = 10.dp.toPx()
                val sw     = 2.5f.dp.toPx()
                val ringR  = 44.dp.toPx()

                // Branches du réticule (croix à 4 branches avec espace au centre)
                val branches = listOf(
                    Offset(cx - arm, cy) to Offset(cx - gap, cy),
                    Offset(cx + gap, cy) to Offset(cx + arm, cy),
                    Offset(cx, cy - arm) to Offset(cx, cy - gap),
                    Offset(cx, cy + gap) to Offset(cx, cy + arm)
                )
                branches.forEach { (s, e) ->
                    drawLine(crosshairColor.copy(alpha = 0.9f), s, e, sw, StrokeCap.Round)
                }
                // Point central
                drawCircle(crosshairColor.copy(alpha = 0.9f), 4.dp.toPx(), Offset(cx, cy))

                // Arc de progression auto-capture (cercle vert qui se remplit)
                if (isStable && autoProgress > 0f) {
                    drawArc(
                        color = Color(0xFF4CAF50).copy(alpha = 0.85f),
                        startAngle = -90f,
                        sweepAngle = 360f * autoProgress,
                        useCenter = false,
                        topLeft = Offset(cx - ringR, cy - ringR),
                        size = Size(ringR * 2f, ringR * 2f),
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Ligne horizon de référence (montre où est l'horizontal, décalée par l'angle)
                val horizonOffsetPx = angleDeg * 3.5f.dp.toPx()
                val horizonY = cy + horizonOffsetPx
                drawLine(
                    color = Color(0xFF4CAF50).copy(alpha = 0.45f),
                    start = Offset(cx - 90.dp.toPx(), horizonY),
                    end   = Offset(cx + 90.dp.toPx(), horizonY),
                    strokeWidth = 1.5f.dp.toPx()
                )
            }

            // ── HUD haut : titre + bouton fermer ─────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel), tint = Color.White)
                }
                Text(
                    if (isBaseAngle) stringResource(R.string.height_measure_aim_base)
                    else stringResource(R.string.height_measure_aim_top),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.size(48.dp))
            }

            // ── HUD bas : angle + barre stabilité + bouton capture ───────────
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.60f))
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Valeur de l'angle en grand
                Text(
                    "${String.format("%.1f", liveAngle?.avgPitchDeg ?: 0f)}°",
                    color = crosshairColor,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold
                )

                // Barre de stabilité
                val barColor = when {
                    stability > 0.82f -> Color(0xFF4CAF50)
                    stability > 0.45f -> Color(0xFFFFEB3B)
                    else              -> Color(0xFFF44336)
                }
                LinearProgressIndicator(
                    progress = { stability },
                    modifier  = Modifier.fillMaxWidth(0.65f).height(6.dp),
                    color     = barColor,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )

                val isAutoCapturing = stability >= 0.82f && autoProgress > 0f
                Text(
                    if (isAutoCapturing)
                        stringResource(
                            R.string.height_measure_auto_capture_countdown,
                            ((1f - autoProgress) / 0.067f * 0.1f).toInt().coerceAtLeast(0)
                        )
                    else
                        stringResource(R.string.height_camera_stabilize),
                    color = if (isAutoCapturing) Color(0xFF81C784) else Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelSmall
                )

                // Bouton capture manuelle
                Button(
                    onClick = {
                        val avg = liveAngle?.avgPitchDeg?.toDouble()
                        if (avg != null) { captured = true; onCapture(avg) }
                    },
                    enabled = stability > 0.35f,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32),
                        disabledContainerColor = Color.White.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.fillMaxWidth(0.65f)
                ) {
                    Icon(Icons.Default.Adjust, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.height_measure_capture), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CameraPreviewView(modifier: Modifier = Modifier) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context        = LocalContext.current

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            val future = ProcessCameraProvider.getInstance(ctx)
            future.addListener({
                runCatching {
                    val provider = future.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview
                    )
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = modifier
    )
}
