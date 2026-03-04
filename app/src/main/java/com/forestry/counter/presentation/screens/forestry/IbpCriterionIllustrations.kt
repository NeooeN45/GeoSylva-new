package com.forestry.counter.presentation.screens.forestry

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import com.forestry.counter.domain.model.IbpCriterionId
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun IbpCriterionIllustration(criterionId: IbpCriterionId, modifier: Modifier = Modifier.size(80.dp)) {
    when (criterionId) {
        IbpCriterionId.E1  -> IllustrationE1(modifier)
        IbpCriterionId.E2  -> IllustrationE2(modifier)
        IbpCriterionId.GB  -> IllustrationGB(modifier)
        IbpCriterionId.BMS -> IllustrationBMS(modifier)
        IbpCriterionId.BMC -> IllustrationBMC(modifier)
        IbpCriterionId.DMH -> IllustrationDMH(modifier)
        IbpCriterionId.VS  -> IllustrationVS(modifier)
        IbpCriterionId.CF  -> IllustrationCF(modifier)
        IbpCriterionId.CO  -> IllustrationCO(modifier)
        IbpCriterionId.HC  -> IllustrationHC(modifier)
    }
}

/* ──────────────── E1 — Essence à très forte valeur biologique ──────────────── */
@Composable
private fun IllustrationE1(modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width; val h = size.height
        val cx = w / 2; val cy = h / 2
        val trunkColor = Color(0xFF5D4037)
        val leafColor = Color(0xFF2E7D32)
        val starColor = Color(0xFFF9A825)
        // Trunk
        drawRect(trunkColor, topLeft = Offset(cx - w * .07f, h * .55f), size = Size(w * .14f, h * .40f))
        // Crown (large ancient oak)
        drawCircle(leafColor, radius = h * .32f, center = Offset(cx, h * .38f))
        drawCircle(leafColor.copy(alpha = .6f), radius = h * .22f, center = Offset(cx - w * .18f, h * .45f))
        drawCircle(leafColor.copy(alpha = .6f), radius = h * .22f, center = Offset(cx + w * .18f, h * .45f))
        // Star badge
        drawStar(starColor, center = Offset(cx + w * .27f, h * .12f), outerR = w * .14f, innerR = w * .06f)
    }
}

/* ──────────────── E2 — Nombre d'essences indigènes ──────────────── */
@Composable
private fun IllustrationE2(modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width; val h = size.height
        val trunkColor = Color(0xFF5D4037)
        val colors = listOf(Color(0xFF2E7D32), Color(0xFF388E3C), Color(0xFF43A047), Color(0xFF66BB6A))
        val positions = listOf(.18f, .37f, .60f, .82f)
        val heights = listOf(.55f, .45f, .5f, .40f)
        val radii = listOf(.14f, .12f, .13f, .11f)
        positions.forEachIndexed { i, x ->
            val cx = w * x; val cy = h * heights[i]; val r = h * radii[i]
            drawRect(trunkColor, topLeft = Offset(cx - w * .03f, cy + r * .6f), size = Size(w * .06f, h * .38f - r * .6f))
            drawCircle(colors[i], radius = r, center = Offset(cx, cy))
        }
        // Ground line
        drawLine(Color(0xFF795548), Offset(0f, h * .94f), Offset(w, h * .94f), strokeWidth = 3f)
    }
}

/* ──────────────── GB — Gros et très gros bois vivants ──────────────── */
@Composable
private fun IllustrationGB(modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width; val h = size.height
        val cx = w / 2; val cy = h * .45f
        val trunkColor = Color(0xFF4E342E)
        val leafColor = Color(0xFF1B5E20)
        val ringColor = Color(0xFF6D4C41)
        // Large trunk = big DBH
        drawRect(trunkColor, topLeft = Offset(cx - w * .18f, h * .5f), size = Size(w * .36f, h * .45f))
        // Annual rings hint in trunk cross-section
        drawCircle(ringColor.copy(alpha = .3f), radius = w * .18f, center = Offset(cx, h * .78f))
        drawCircle(ringColor.copy(alpha = .2f), radius = w * .12f, center = Offset(cx, h * .78f))
        drawCircle(ringColor.copy(alpha = .2f), radius = w * .06f, center = Offset(cx, h * .78f))
        // Big crown
        drawCircle(leafColor, radius = h * .35f, center = Offset(cx, h * .30f))
        drawCircle(leafColor.copy(alpha = .7f), radius = h * .25f, center = Offset(cx - w * .20f, h * .38f))
        drawCircle(leafColor.copy(alpha = .7f), radius = h * .25f, center = Offset(cx + w * .20f, h * .38f))
        // Ruler / measure indicator
        drawLine(Color(0xFFFFD600), Offset(cx - w * .18f, h * .93f), Offset(cx + w * .18f, h * .93f), strokeWidth = 4f, cap = StrokeCap.Round)
        drawLine(Color(0xFFFFD600), Offset(cx - w * .18f, h * .89f), Offset(cx - w * .18f, h * .97f), strokeWidth = 3f, cap = StrokeCap.Round)
        drawLine(Color(0xFFFFD600), Offset(cx + w * .18f, h * .89f), Offset(cx + w * .18f, h * .97f), strokeWidth = 3f, cap = StrokeCap.Round)
    }
}

/* ──────────────── BMS — Bois mort sur pied ──────────────── */
@Composable
private fun IllustrationBMS(modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width; val h = size.height
        val cx = w * .38f
        val deadColor = Color(0xFF78909C)
        val darkDead = Color(0xFF455A64)
        // Dead trunk (no leaves, ragged top)
        drawRect(deadDark(deadColor), topLeft = Offset(cx - w * .08f, h * .10f), size = Size(w * .16f, h * .82f))
        // Broken top
        val topPath = Path().apply {
            moveTo(cx - w * .08f, h * .10f)
            lineTo(cx - w * .18f, h * .02f)
            lineTo(cx, h * .15f)
            lineTo(cx + w * .12f, h * .04f)
            lineTo(cx + w * .08f, h * .10f)
            close()
        }
        drawPath(topPath, deadColor)
        // Woodpecker hole / cavity
        drawOval(darkDead, topLeft = Offset(cx - w * .06f, h * .35f), size = Size(w * .12f, h * .09f))
        // Fungus bracket
        drawArc(Color(0xFFD7CCC8), startAngle = 180f, sweepAngle = 180f, useCenter = true, topLeft = Offset(cx + w * .08f, h * .55f), size = Size(w * .18f, h * .10f))
        // Second dead tree background
        val cx2 = w * .72f
        drawRect(deadColor.copy(alpha = .5f), topLeft = Offset(cx2 - w * .05f, h * .25f), size = Size(w * .10f, h * .65f))
        // Ground
        drawLine(Color(0xFF795548), Offset(0f, h * .94f), Offset(w, h * .94f), strokeWidth = 3f)
    }
}
private fun deadDark(c: Color) = c.copy(alpha = .85f)

/* ──────────────── BMC — Bois mort au sol ──────────────── */
@Composable
private fun IllustrationBMC(modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width; val h = size.height
        val logColor = Color(0xFF6D4C41)
        val mossColor = Color(0xFF558B2F)
        val ringColor = Color(0xFF4E342E)
        // Ground
        drawRect(Color(0xFF8D6E63).copy(alpha = .3f), topLeft = Offset(0f, h * .72f), size = Size(w, h * .28f))
        // Log 1 (main, large)
        val logPath = Path().apply {
            moveTo(w * .05f, h * .65f)
            lineTo(w * .95f, h * .58f)
            lineTo(w * .95f, h * .74f)
            lineTo(w * .05f, h * .81f)
            close()
        }
        drawPath(logPath, logColor)
        // End ring (cross-section)
        drawOval(ringColor, topLeft = Offset(w * .02f, h * .59f), size = Size(w * .16f, h * .24f))
        drawOval(Color(0xFF795548), topLeft = Offset(w * .05f, h * .63f), size = Size(w * .10f, h * .16f))
        drawOval(Color(0xFF5D4037), topLeft = Offset(w * .07f, h * .67f), size = Size(w * .06f, h * .08f))
        // Moss on log
        drawCircle(mossColor.copy(alpha = .7f), radius = w * .07f, center = Offset(w * .35f, h * .56f))
        drawCircle(mossColor.copy(alpha = .7f), radius = w * .06f, center = Offset(w * .55f, h * .54f))
        drawCircle(mossColor.copy(alpha = .7f), radius = w * .07f, center = Offset(w * .72f, h * .55f))
        // Log 2 (smaller, background)
        drawRect(logColor.copy(alpha = .4f), topLeft = Offset(w * .12f, h * .82f), size = Size(w * .70f, h * .10f))
    }
}

/* ──────────────── DMH — Dendromicrohabitats ──────────────── */
@Composable
private fun IllustrationDMH(modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width; val h = size.height
        val cx = w / 2
        val trunkColor = Color(0xFF5D4037)
        val leafColor = Color(0xFF388E3C)
        val cavityColor = Color(0xFF1A237E)
        val ivyColor = Color(0xFF33691E)
        // Trunk
        drawRect(trunkColor, topLeft = Offset(cx - w * .10f, h * .22f), size = Size(w * .20f, h * .70f))
        // Crown
        drawCircle(leafColor, radius = h * .28f, center = Offset(cx, h * .22f))
        // Cavity (hole)
        drawOval(Color.Black.copy(alpha = .7f), topLeft = Offset(cx - w * .09f, h * .40f), size = Size(w * .14f, h * .12f))
        drawOval(cavityColor.copy(alpha = .5f), topLeft = Offset(cx - w * .07f, h * .41f), size = Size(w * .10f, h * .09f))
        // Woodpecker hole top
        drawCircle(Color.Black.copy(alpha = .8f), radius = w * .045f, center = Offset(cx + w * .11f, h * .31f))
        // Ivy / creeper on trunk
        val ivyPath = Path().apply {
            moveTo(cx + w * .10f, h * .90f)
            cubicTo(cx + w * .18f, h * .75f, cx + w * .05f, h * .65f, cx + w * .14f, h * .50f)
        }
        drawPath(ivyPath, ivyColor, style = Stroke(width = 5f, cap = StrokeCap.Round))
        drawCircle(ivyColor, radius = w * .04f, center = Offset(cx + w * .14f, h * .50f))
        drawCircle(ivyColor, radius = w * .03f, center = Offset(cx + w * .16f, h * .70f))
        // Sap run (coulée de sève)
        drawLine(Color(0xFFFFA000), Offset(cx - w * .04f, h * .27f), Offset(cx - w * .04f, h * .42f), strokeWidth = 3f, cap = StrokeCap.Round)
        // Ground line
        drawLine(Color(0xFF795548), Offset(0f, h * .94f), Offset(w, h * .94f), strokeWidth = 3f)
    }
}

/* ──────────────── VS — Végétation du sous-bois ──────────────── */
@Composable
private fun IllustrationVS(modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width; val h = size.height
        val groundColor = Color(0xFF795548)
        val shrubColor = Color(0xFF558B2F)
        val herbColor = Color(0xFF8BC34A)
        val treeColor = Color(0xFF1B5E20)
        val trunkColor = Color(0xFF5D4037)
        // Background trees (tall, light)
        listOf(.20f, .80f).forEach { x ->
            drawRect(trunkColor.copy(alpha = .3f), topLeft = Offset(w * x - w * .04f, h * .05f), size = Size(w * .08f, h * .90f))
            drawCircle(treeColor.copy(alpha = .25f), radius = h * .20f, center = Offset(w * x, h * .10f))
        }
        // Ground
        drawRect(groundColor.copy(alpha = .2f), topLeft = Offset(0f, h * .75f), size = Size(w, h * .25f))
        drawLine(groundColor, Offset(0f, h * .76f), Offset(w, h * .76f), strokeWidth = 2f)
        // Shrub layer (main subject)
        listOf(.18f, .40f, .62f, .82f).forEachIndexed { i, x ->
            val cy = h * (.68f - (i % 2) * .07f)
            val r = w * (.14f - (i % 2) * .02f)
            drawCircle(shrubColor, radius = r, center = Offset(w * x, cy))
            drawCircle(shrubColor.copy(alpha = .7f), radius = r * .7f, center = Offset(w * x - r * .4f, cy - r * .3f))
        }
        // Herb layer
        listOf(.10f, .28f, .50f, .70f, .88f).forEach { x ->
            val cx = w * x; val bot = h * .76f
            drawLine(herbColor, Offset(cx, bot), Offset(cx - w * .03f, bot - h * .09f), strokeWidth = 3f, cap = StrokeCap.Round)
            drawLine(herbColor, Offset(cx, bot), Offset(cx + w * .04f, bot - h * .12f), strokeWidth = 3f, cap = StrokeCap.Round)
            drawLine(herbColor, Offset(cx, bot), Offset(cx, bot - h * .14f), strokeWidth = 3f, cap = StrokeCap.Round)
        }
    }
}

/* ──────────────── CF — Continuité forestière ──────────────── */
@Composable
private fun IllustrationCF(modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width; val h = size.height
        val cx = w / 2; val cy = h * .50f
        // Annual rings — many = old forest
        val ringCount = 7
        val ringColors = listOf(Color(0xFF5D4037), Color(0xFF6D4C41), Color(0xFF795548), Color(0xFF8D6E63), Color(0xFF4E342E), Color(0xFF3E2723), Color(0xFF5D4037))
        for (i in ringCount downTo 1) {
            drawCircle(ringColors[i - 1].copy(alpha = .18f * i), radius = w * .46f * i / ringCount, center = Offset(cx, cy))
            drawCircle(Color.Transparent, radius = w * .46f * i / ringCount, center = Offset(cx, cy), style = Stroke(width = 1.5f))
        }
        drawCircle(Color(0xFF3E2723).copy(alpha = .8f), radius = w * .06f, center = Offset(cx, cy))
        // "100 ans" label hint — arc text replaced by tick marks
        val ages = listOf(30, 60, 100)
        ages.forEachIndexed { i, _ ->
            val r = w * .46f * (i + 1) * 2 / (ages.size * 2)
            drawLine(Color(0xFFFF6F00), Offset(cx + r, cy), Offset(cx + r + w * .04f, cy), strokeWidth = 3f)
        }
        // Clock arrow
        drawLine(Color(0xFFFF6F00).copy(alpha = .8f), Offset(cx, cy), Offset(cx + w * .28f, cy - h * .12f), strokeWidth = 4f, cap = StrokeCap.Round)
    }
}

/* ──────────────── CO — Connexion aux habitats naturels ──────────────── */
@Composable
private fun IllustrationCO(modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width; val h = size.height
        val forestColor = Color(0xFF2E7D32)
        val hedgeColor = Color(0xFF388E3C)
        val arrowColor = Color(0xFF1565C0)
        // Central forest patch
        drawCircle(forestColor, radius = w * .18f, center = Offset(w / 2, h / 2))
        // Surrounding habitat patches
        val patches = listOf(Offset(w * .12f, h * .15f), Offset(w * .85f, h * .15f), Offset(w * .12f, h * .82f), Offset(w * .85f, h * .82f))
        patches.forEach { p ->
            drawCircle(hedgeColor.copy(alpha = .7f), radius = w * .13f, center = p)
        }
        // Connecting corridors (arrows)
        val center = Offset(w / 2, h / 2)
        patches.forEach { p ->
            val dx = center.x - p.x; val dy = center.y - p.y
            val len = kotlin.math.sqrt(dx * dx + dy * dy)
            val ux = dx / len; val uy = dy / len
            val start = Offset(p.x + ux * w * .14f, p.y + uy * h * .14f)
            val end = Offset(center.x - ux * w * .19f, center.y - uy * h * .19f)
            drawLine(arrowColor.copy(alpha = .8f), start, end, strokeWidth = 3f, cap = StrokeCap.Round)
            // Arrowhead
            val arrowHead = Path().apply {
                val angle = kotlin.math.atan2(uy.toDouble(), ux.toDouble()).toFloat()
                val tip = end
                moveTo(tip.x, tip.y)
                lineTo(tip.x - ux * w * .05f + uy * h * .04f, tip.y - uy * h * .05f - ux * w * .04f)
                lineTo(tip.x - ux * w * .05f - uy * h * .04f, tip.y - uy * h * .05f + ux * w * .04f)
                close()
            }
            drawPath(arrowHead, arrowColor.copy(alpha = .8f))
        }
    }
}

/* ──────────────── HC — Habitats complémentaires ──────────────── */
@Composable
private fun IllustrationHC(modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width; val h = size.height
        // Pond / mare (top left)
        drawOval(Color(0xFF1565C0).copy(alpha = .7f), topLeft = Offset(w * .04f, h * .12f), size = Size(w * .32f, h * .20f))
        drawOval(Color(0xFF42A5F5).copy(alpha = .4f), topLeft = Offset(w * .10f, h * .16f), size = Size(w * .18f, h * .10f))
        // Rock outcrop (top right)
        val rockPath = Path().apply {
            moveTo(w * .56f, h * .28f)
            lineTo(w * .65f, h * .12f)
            lineTo(w * .82f, h * .14f)
            lineTo(w * .92f, h * .28f)
            lineTo(w * .85f, h * .32f)
            lineTo(w * .60f, h * .32f)
            close()
        }
        drawPath(rockPath, Color(0xFF78909C))
        drawPath(rockPath, Color(0xFF546E7A), style = Stroke(width = 2f))
        // Wide forest edge / lisière (bottom left)
        drawRect(Color(0xFF558B2F).copy(alpha = .6f), topLeft = Offset(0f, h * .62f), size = Size(w * .38f, h * .32f))
        drawCircle(Color(0xFF2E7D32), radius = w * .10f, center = Offset(w * .12f, h * .62f))
        drawCircle(Color(0xFF388E3C), radius = w * .09f, center = Offset(w * .28f, h * .60f))
        // Pile of stones (bottom right)
        listOf(0 to 0, 1 to 0, -1 to 0, 0 to -1).forEach { (dx, dy) ->
            drawCircle(Color(0xFF90A4AE), radius = w * .05f, center = Offset(w * .78f + dx * w * .06f, h * .78f + dy * h * .06f))
        }
        // Labels hint lines
        drawLine(Color(0xFFBDBDBD).copy(alpha = .4f), Offset(w * .38f, h * .28f), Offset(w * .56f, h * .28f), strokeWidth = 1f)
        drawLine(Color(0xFFBDBDBD).copy(alpha = .4f), Offset(w * .38f, h * .68f), Offset(w * .62f, h * .68f), strokeWidth = 1f)
    }
}

/* ──────────────── Utility: draw a 5-pointed star ──────────────── */
private fun DrawScope.drawStar(color: Color, center: Offset, outerR: Float, innerR: Float) {
    val path = Path()
    for (i in 0 until 10) {
        val angle = (Math.PI / 5.0 * i - Math.PI / 2.0).toFloat()
        val r = if (i % 2 == 0) outerR else innerR
        val x = center.x + r * cos(angle)
        val y = center.y + r * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color)
}
