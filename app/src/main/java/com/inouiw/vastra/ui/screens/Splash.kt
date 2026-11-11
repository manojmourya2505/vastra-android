package com.inouiw.vastra.ui.screens

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.inouiw.vastra.data.AppStore
import com.inouiw.vastra.data.Route
import com.inouiw.vastra.ui.theme.Ink
import com.inouiw.vastra.ui.theme.Paper
import com.inouiw.vastra.ui.theme.Shade
import com.inouiw.vastra.ui.theme.Silver
import com.inouiw.vastra.ui.theme.Soft
import kotlinx.coroutines.delay

private val ease = CubicBezierEasing(0.2f, 0.9f, 0.3f, 1f)

/** 0..1 progress of [elapsed] through a CSS-style animation that starts at [start] ms and lasts [dur] ms. */
private fun prog(elapsed: Long, start: Int, dur: Int): Float =
    ((elapsed - start).toFloat() / dur).coerceIn(0f, 1f)

/** Diamond outline of the mark, in the prototype's 1024-unit space. */
private fun diamond(scale: Float, ox: Float, oy: Float, pts: Boolean = true): Path = Path().apply {
    moveTo(512f * scale + ox, 190f * scale + oy)
    lineTo(792f * scale + ox, 430f * scale + oy)
    lineTo(512f * scale + ox, 840f * scale + oy)
    lineTo(232f * scale + ox, 430f * scale + oy)
    close()
}

@Composable
fun SplashScreen(store: AppStore) {
    // Frame clock in milliseconds since the splash started.
    val elapsed by produceState(0L) {
        val start = withFrameNanos { it }
        while (value < 4000L) {
            withFrameNanos { value = (it - start) / 1_000_000L }
        }
    }

    LaunchedEffect(Unit) {
        delay(4000)
        while (!store.ready) delay(50)
        store.replace(
            if (!store.introSeen) Route.Intro else if (store.loggedIn) Route.Home else Route.Login,
        )
    }

    Column(
        Modifier.fillMaxSize().background(Paper),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Canvas(Modifier.size(296.dp)) {
            val s = size.width / 1024f
            val outline = diamond(s, 0f, 0f)
            val measure = PathMeasure()
            measure.setPath(outline, true)

            // Dashed base stitch, fades in over 0.4s.
            drawPath(
                outline,
                Color(0xFFBDBDBD).copy(alpha = prog(elapsed, 0, 400)),
                style = Stroke(width = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f))),
            )

            // Black thread runs once around the outline (1.6s, after 0.2s), then fades out at 1.8s.
            val thread = ease.transform(prog(elapsed, 200, 1600))
            val threadAlpha = 1f - prog(elapsed, 1800, 300)
            if (threadAlpha > 0f && thread > 0f) {
                val partial = Path()
                measure.getSegment(0f, measure.length * thread, partial, true)
                drawPath(partial, Ink.copy(alpha = threadAlpha), style = Stroke(width = 9f, cap = StrokeCap.Round))
            }

            // Three facets sew in one after another at 1.8s, 1.95s and 2.1s.
            val cx = size.width / 2f
            val cy = 430f * s
            fun facet(start: Int, color: Color, pts: List<Offset>) {
                val p = prog(elapsed, start, 600)
                if (p <= 0f) return
                val k = 0.92f + 0.08f * ease.transform(p)
                withTransform({ scale(k, k, pivot = Offset(cx, cy)) }) {
                    drawPath(
                        Path().apply {
                            moveTo(pts[0].x, pts[0].y); lineTo(pts[1].x, pts[1].y); lineTo(pts[2].x, pts[2].y); close()
                        },
                        color.copy(alpha = p),
                    )
                }
            }
            val top = Offset(512f * s, 190f * s)
            val right = Offset(792f * s, 430f * s)
            val bottom = Offset(512f * s, 840f * s)
            val left = Offset(232f * s, 430f * s)
            val mid = Offset(512f * s, 430f * s)
            facet(1800, Ink, listOf(top, left, right))
            facet(1950, Shade, listOf(left, mid, bottom))
            facet(2100, Silver, listOf(right, mid, bottom))
        }

        // Thread underline: 1px black line grows to 300dp over 1s from 2.6s.
        Box(
            Modifier
                .height(1.dp)
                .width(300.dp * ease.transform(prog(elapsed, 2600, 1000)))
                .background(Ink),
        )

        // Wordmark: each letter drops in and un-rotates, 0.12s apart from 2.8s.
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            "VASTRA".forEachIndexed { i, ch ->
                val p = prog(elapsed, 2800 + i * 120, 600)
                Text(
                    ch.toString(),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.32.em,
                    color = Ink,
                    modifier = Modifier.graphicsLayer {
                        alpha = p
                        translationY = (1f - ease.transform(p)) * -14.dp.toPx()
                        rotationZ = (1f - ease.transform(p)) * -6f
                    },
                )
            }
        }

        // Tagline fades in over 1s from 3.4s.
        Text(
            "WEAR THE MOMENT",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.em,
            color = Soft,
            textAlign = TextAlign.Center,
            modifier = Modifier.graphicsLayer { alpha = prog(elapsed, 3400, 1000) },
        )

        if (!store.ready) {
            Text(
                store.loadError ?: "Connecting to Vastra…",
                fontSize = 11.sp,
                color = if (store.loadError != null) Ink else Soft,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp),
            )
        }
    }
}
