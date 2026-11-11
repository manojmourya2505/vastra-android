package com.inouiw.vastra.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inouiw.vastra.ui.theme.Ink
import com.inouiw.vastra.ui.theme.Muted
import com.inouiw.vastra.ui.theme.Paper
import kotlinx.coroutines.delay

/** The Vastra mark drawn as the build loader: stitches appear, the thread sews the outline, facets settle. Loops every 3.6s. */
@Composable
fun BuildLogo(modifier: Modifier = Modifier) {
    val elapsed by produceState(0L) {
        val start = withFrameNanos { it }
        while (true) {
            withFrameNanos { value = ((it - start) / 1_000_000L) % 3600L }
        }
    }
    val outline = remember { Path().apply { moveTo(512f, 190f); lineTo(792f, 430f); lineTo(512f, 840f); lineTo(232f, 430f); close() } }
    val top = remember { Path().apply { moveTo(512f, 190f); lineTo(232f, 430f); lineTo(792f, 430f); close() } }
    val left = remember { Path().apply { moveTo(232f, 430f); lineTo(512f, 430f); lineTo(512f, 840f); close() } }
    val right = remember { Path().apply { moveTo(792f, 430f); lineTo(512f, 430f); lineTo(512f, 840f); close() } }

    fun p(start: Long, dur: Long): Float = ((elapsed - start).toFloat() / dur).coerceIn(0f, 1f)

    Canvas(modifier.size(170.dp)) {
        val s = size.width / 1024f
        // 44dp in the 170dp box, expressed in the 1024 viewBox units
        val unit = 1024f / size.width
        val drop = -44.dp.toPx() * unit
        val side = 44.dp.toPx() * unit
        val low = 14.dp.toPx() * unit
        withTransform({ scale(s, s, pivot = Offset.Zero) }) {
            // stitch guide
            drawPath(
                outline,
                Color(0xFFBDBDBD).copy(alpha = p(0, 400)),
                style = Stroke(width = 3f, cap = StrokeCap.Round, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(12f, 10f))),
            )
            // thread sews the outline
            val threadAlpha = 1f - p(1700, 300)
            val pm = android.graphics.PathMeasure(outline.asAndroidPath(), false)
            val dst = android.graphics.Path()
            pm.getSegment(0f, pm.length * p(200, 1400), dst, true)
            drawPath(dst.asComposePath(), Ink.copy(alpha = threadAlpha), style = Stroke(width = 9f, cap = StrokeCap.Round))
            // facets settle in
            withTransform({ translate(0f, drop * (1f - p(1700, 700))) }) {
                drawPath(top, Color.Black, alpha = p(1700, 700))
            }
            withTransform({ translate(side * (1f - p(1850, 700)) * -1f, low * (1f - p(1850, 700))) }) {
                drawPath(left, Color(0xFF3A3A3A), alpha = p(1850, 700))
            }
            withTransform({ translate(side * (1f - p(2000, 700)), low * (1f - p(2000, 700))) }) {
                drawPath(right, Color(0xFF9A9A9A), alpha = p(2000, 700))
            }
        }
    }
}

/** Full-screen build screen: three garment cards fanned together and shining, a checklist that ticks off as the work runs, and a bar that fills over [durationMs]. */
@Composable
fun BuildOverlay(title: String, steps: List<String>, durationMs: Int, ready: Boolean = true, onDone: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    val bar = remember { Animatable(0f) }
    var timerDone by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        bar.animateTo(1f, tween(durationMs, easing = LinearEasing))
        timerDone = true
    }
    // Close only when the timer has run AND the real work (the pictures) is ready.
    LaunchedEffect(timerDone, ready) {
        if (timerDone && ready && !finished) {
            finished = true
            onDone()
        }
    }
    LaunchedEffect(Unit) {
        val interval = durationMs.toLong() / steps.size
        while (step < steps.lastIndex) {
            delay(interval)
            step++
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Paper)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            )
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        FanCards(Modifier.size(260.dp, 230.dp))
        Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Ink, modifier = Modifier.padding(top = 18.dp))
        Column(
            Modifier.padding(top = 22.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            steps.forEachIndexed { i, label -> StepRow(label, done = i < step, current = i == step) }
        }
        Box(
            Modifier
                .padding(top = 26.dp)
                .fillMaxWidth(0.7f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFFEDEDED)),
        ) {
            Box(Modifier.fillMaxWidth(bar.value).height(6.dp).background(Ink))
        }
        Text(
            "This can take up to a minute. Keep this screen open.",
            fontSize = 11.sp,
            color = Muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 14.dp),
        )
    }
}

/** One checklist line: a tick when done, a small spinner on the current step, and faded text for the steps still to come. */
@Composable
private fun StepRow(label: String, done: Boolean, current: Boolean) {
    Row(
        Modifier.fillMaxWidth().alpha(if (done || current) 1f else 0.35f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(22.dp).clip(CircleShape).background(if (done) Ink else Color(0xFFEDEDED)),
            contentAlignment = Alignment.Center,
        ) {
            if (done) {
                Text("✓", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Paper)
            } else if (current) {
                CircularProgressIndicator(Modifier.size(12.dp), color = Ink, strokeWidth = 2.dp)
            }
        }
        Text(
            label,
            fontSize = 15.sp,
            fontWeight = if (current) FontWeight.SemiBold else FontWeight.Normal,
            color = Ink,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}

/** Three garment cards fanned together. They sway gently and a soft shine crosses them. */
@Composable
private fun FanCards(modifier: Modifier) {
    val t = rememberInfiniteTransition(label = "fan")
    val sway by t.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "sway",
    )
    val shine by t.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Restart),
        label = "shine",
    )
    val cards = listOf(
        Pair(-16f, Color(0xFFBDBDBD)),
        Pair(16f, Color(0xFF6B6B6B)),
        Pair(0f, Ink),
    )
    Box(modifier, contentAlignment = Alignment.Center) {
        cards.forEach { (rot, color) ->
            Box(
                Modifier
                    .size(150.dp, 200.dp)
                    .graphicsLayer { rotationZ = rot + sway }
                    .clip(RoundedCornerShape(22.dp))
                    .background(color)
                    .drawWithContent {
                        drawContent()
                        val w = size.width
                        val x = -w + shine * 2f * w
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.3f), Color.Transparent),
                                start = Offset(x, 0f),
                                end = Offset(x + w, size.height),
                            ),
                        )
                    },
            )
        }
    }
}
