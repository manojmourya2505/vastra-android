package com.inouiw.vastra.ui.screens

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.inouiw.vastra.data.AppStore
import com.inouiw.vastra.data.Catalog
import com.inouiw.vastra.data.Route
import com.inouiw.vastra.ui.GarmentArt
import com.inouiw.vastra.ui.VIcons
import com.inouiw.vastra.ui.theme.Ink
import com.inouiw.vastra.ui.theme.Line
import com.inouiw.vastra.ui.theme.Muted
import com.inouiw.vastra.ui.theme.Paper
import com.inouiw.vastra.ui.theme.Soft
import com.inouiw.vastra.ui.theme.Stone
import kotlinx.coroutines.launch

private val ease = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)
private val serif = FontFamily.Serif

private fun prog(elapsed: Long, start: Int, dur: Int): Float =
    ((elapsed - start).toFloat() / dur).coerceIn(0f, 1f)


@Composable
fun IntroScreen(store: AppStore) {
    val titles = store.introSlides.map { it.title }
    val copies = store.introSlides.map { it.copy }
    val pager = rememberPagerState(pageCount = { titles.size })
    val scope = rememberCoroutineScope()
    val last = titles.lastIndex
    // Clock for the slide animations, restarted each time the active slide changes.
    val elapsed by produceState(0L, pager.currentPage) {
        val start = withFrameNanos { it }
        value = 0L
        while (value < 4000L) {
            withFrameNanos { value = (it - start) / 1_000_000L }
        }
    }

    Box(Modifier.fillMaxSize().background(Paper)) {
        Column(Modifier.fillMaxSize().padding(horizontal = 0.dp)) {
            // .top: logo left, SKIP right, 24dp side padding
            Row(
                Modifier.fillMaxWidth().padding(start = 24.dp, top = 22.dp, end = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Vastra mark: black top facet, dark grey lower-left, silver lower-right.
                    Canvas(Modifier.size(22.dp)) {
                        val s = size.width / 1024f
                        fun facet(color: Color, a: Pair<Float, Float>, b: Pair<Float, Float>, c: Pair<Float, Float>) {
                            drawPath(
                                androidx.compose.ui.graphics.Path().apply {
                                    moveTo(a.first * s, a.second * s); lineTo(b.first * s, b.second * s); lineTo(c.first * s, c.second * s); close()
                                },
                                color,
                            )
                        }
                        facet(Ink, 512f to 190f, 792f to 430f, 232f to 430f)
                        facet(Color(0xFF3A3A3A), 232f to 430f, 512f to 430f, 512f to 840f)
                        facet(Color(0xFF9A9A9A), 792f to 430f, 512f to 430f, 512f to 840f)
                    }
                    Text("VASTRA", fontFamily = serif, fontSize = 19.sp, letterSpacing = 0.18.em, color = Ink)
                }
                Text(
                    "SKIP",
                    fontSize = 11.sp,
                    letterSpacing = 0.25.em,
                    color = Muted,
                    modifier = Modifier.clickable { store.markIntroSeen(); store.replace(Route.Login) }.padding(vertical = 8.dp),
                )
            }

            HorizontalPager(state = pager, modifier = Modifier.weight(1f).fillMaxWidth()) { page ->
                val active = page == pager.currentPage
                Column(Modifier.fillMaxSize().padding(start = 28.dp, top = 22.dp, end = 28.dp)) {
                    Box(Modifier.fillMaxWidth().height(340.dp)) {
                        if (page == 0) SlideOne(if (active) elapsed else 0L, store.introPrompt, store.introLooks)
                        if (page == 1) SlideTwo(if (active) elapsed else 0L)
                        if (page == 2) SlideThree(if (active) elapsed else 0L)
                    }
                    Column(Modifier.padding(top = 30.dp)) {
                        Text(
                            titles[page],
                            fontFamily = serif,
                            fontSize = 30.sp,
                            lineHeight = 33.sp,
                            letterSpacing = 0.01.em,
                            modifier = Modifier.graphicsLayer { alpha = if (active) prog(elapsed, 250, 800) else 0f },
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            copies[page],
                            fontSize = 15.sp,
                            lineHeight = 24.sp,
                            color = Soft,
                            modifier = Modifier.graphicsLayer { alpha = if (active) prog(elapsed, 700, 1000) else 0f },
                        )
                    }
                }
            }
        }

        // .controls: ring button, bottom-left, 28dp in
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .padding(start = 28.dp, bottom = 34.dp)
                .size(76.dp)
                .clickable {
                    if (pager.currentPage < last) scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
                    else { store.markIntroSeen(); store.replace(Route.Login) }
                },
            contentAlignment = Alignment.Center,
        ) {
            val fraction = (pager.currentPage + 1f) / titles.size
            Canvas(Modifier.size(76.dp)) {
                val r = 34.5f * size.width / 76f
                val c = size.width / 2f
                drawCircle(Line, radius = r, center = Offset(c, c), style = Stroke(width = 2f))
                drawArc(
                    color = Ink,
                    startAngle = -90f,
                    sweepAngle = 360f * fraction,
                    useCenter = false,
                    topLeft = Offset(c - r, c - r),
                    size = Size(r * 2, r * 2),
                    style = Stroke(width = 2f, cap = StrokeCap.Round),
                )
            }
            Box(Modifier.size(58.dp).clip(CircleShape).background(Ink), contentAlignment = Alignment.Center) {
                val isLast = pager.currentPage == last
                Icon(if (isLast) VIcons.Check else VIcons.Arrow, if (isLast) "Finish" else "Next", tint = Paper, modifier = Modifier.size(22.dp))
            }
        }
    }
}

/** Slide 1: typed prompt, three look cards fan out, and a "totals checked" badge. */
@Composable
private fun SlideOne(elapsed: Long, prompt: String, looks: List<String>) {
    val chars = (prompt.length * prog(elapsed, 400, 1800)).toInt()
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, top = 18.dp, end = 18.dp)
                .border(0.dp, Line),
        ) {
            Text(prompt.take(chars), fontSize = 13.sp, color = Ink)
            Box(Modifier.fillMaxWidth().height(1.dp).background(Line).padding(top = 0.dp))
        }
        val cards = listOf(
            Triple(looks.getOrElse(0) { "" }, Brush.linearGradient(listOf(Color.Black, Color(0xFF333333))), Triple(-92f, -10f, -10f) to 1100),
            Triple(looks.getOrElse(1) { "" }, Brush.linearGradient(listOf(Color(0xFF9A9A9A), Color(0xFFD6D6D6), Color.Black)), Triple(0f, -18f, 0f) to 1100),
            Triple(looks.getOrElse(2) { "" }, Brush.linearGradient(listOf(Color(0xFFCFCFCF), Color(0xFF3A3A3A), Color(0xFF9A9A9A))), Triple(92f, -10f, 10f) to 1100),
        )
        cards.forEachIndexed { i, (label, swatch, fanAndDelay) ->
            val (dx, dy, rot) = fanAndDelay.first
            val start = 900 + i * 200
            val p = ease.transform(prog(elapsed, start, 1100))
            Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 26.dp)
                    .width(132.dp)
                    .height(196.dp)
                    .graphicsLayer {
                        alpha = p
                        translationX = dx.dp.toPx() * p
                        translationY = (120f * (1f - p) + dy * p).dp.toPx()
                        rotationZ = rot * p
                    }
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFE5E5E5), Color(0xFFF2F2F2))))
                    .border(1.dp, Stone, RoundedCornerShape(12.dp))
                    .padding(10.dp),
            ) {
                Box(Modifier.fillMaxWidth().height(118.dp).clip(RoundedCornerShape(7.dp)).background(swatch))
                Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Ink, modifier = Modifier.padding(top = 9.dp))
                Text("WITHIN BUDGET", fontSize = 10.sp, letterSpacing = 0.1.em, color = Muted, modifier = Modifier.padding(top = 3.dp))
            }
        }
        Text(
            "TOTALS CHECKED",
            fontSize = 10.sp,
            letterSpacing = 0.2.em,
            color = Ink,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = 18.dp)
                .border(1.dp, Stone, RoundedCornerShape(20.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .graphicsLayer { alpha = prog(elapsed, 2400, 500) },
        )
    }
}

/** Slide 2: the figure with three fit tags. The figure is drawn from the catalogue silhouette. */
@Composable
private fun SlideTwo(elapsed: Long) {
    Box(Modifier.fillMaxSize()) {
        GarmentArt(Catalog.byId("dress1")!!, Modifier.align(Alignment.Center).size(190.dp, 240.dp))
        SlideTag("FIT · TRUE TO SIZE", Modifier.align(Alignment.TopStart).padding(start = 16.dp, top = 28.dp), prog(elapsed, 2200, 500))
        SlideTag("SIZE M", Modifier.align(Alignment.TopEnd).padding(end = 16.dp, top = 150.dp), prog(elapsed, 2500, 500))
        SlideTag("LOOK 2 · TRIED ON", Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 26.dp), prog(elapsed, 2800, 500))
    }
}

/** Slide 3: three tiles fly into a bag, which bounces. */
@Composable
private fun SlideThree(elapsed: Long) {
    Box(Modifier.fillMaxSize()) {
        val tiles = listOf(
            Triple(Modifier.align(Alignment.TopStart).padding(start = 28.dp, top = 26.dp), Brush.linearGradient(listOf(Color.Black, Color(0xFF333333))), Pair(108f, 0)),
            Triple(Modifier.align(Alignment.TopCenter).padding(top = 26.dp), Brush.linearGradient(listOf(Color(0xFF9A9A9A), Color(0xFFD6D6D6))), Pair(0f, 300)),
            Triple(Modifier.align(Alignment.TopEnd).padding(end = 28.dp, top = 26.dp), Brush.linearGradient(listOf(Color(0xFF3A3A3A), Color(0xFF151515))), Pair(-108f, 600)),
        )
        tiles.forEachIndexed { i, (mod, brush, dest) ->
            val start = 600 + i * 300
            val p = prog(elapsed, start, 1000)
            val e = ease.transform(p)
            Box(
                mod
                    .size(62.dp, 76.dp)
                    .graphicsLayer {
                        alpha = 1f - p
                        translationX = dest.first.dp.toPx() * p
                        translationY = 150.dp.toPx() * p
                        scaleX = 1f - 0.8f * e
                        scaleY = 1f - 0.8f * e
                    }
                    .clip(RoundedCornerShape(8.dp))
                    .background(brush)
                    .border(1.dp, Stone, RoundedCornerShape(8.dp)),
            )
        }
        Canvas(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 50.dp)
                .size(88.dp, 82.dp)
                .graphicsLayer {
                    val b = prog(elapsed, 2000, 500)
                    val k = 1f + 0.12f * (1f - kotlin.math.abs(2 * b - 1f))
                    scaleX = if (b in 0f..1f) k else 1f
                    scaleY = scaleX
                },
        ) {
            val s = size.width / 88f
            drawPath(
                androidx.compose.ui.graphics.Path().apply {
                    moveTo(28f * s, 26f * s); quadraticTo(28f * s, 6f * s, 44f * s, 6f * s); quadraticTo(60f * s, 6f * s, 60f * s, 26f * s)
                },
                Color.Black,
                style = Stroke(width = 3f * s, cap = StrokeCap.Round),
            )
            drawRoundRect(Color.Black, topLeft = Offset(8f * s, 26f * s), size = Size(72f * s, 50f * s), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * s))
            drawRect(Color(0xFF9A9A9A), topLeft = Offset(8f * s, 26f * s), size = Size(72f * s, 8f * s))
        }
    }
}

@Composable
private fun SlideTag(text: String, modifier: Modifier, alpha: Float) {
    Text(
        text,
        fontSize = 10.sp,
        letterSpacing = 0.2.em,
        color = Ink,
        modifier = modifier
            .graphicsLayer { this.alpha = alpha }
            .border(1.dp, Stone, RoundedCornerShape(20.dp))
            .padding(horizontal = 11.dp, vertical = 7.dp),
    )
}
