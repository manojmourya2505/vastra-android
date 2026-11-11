package com.inouiw.vastra.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.inouiw.vastra.data.AppStore
import com.inouiw.vastra.data.Route
import com.inouiw.vastra.ui.VIcons
import com.inouiw.vastra.ui.gbp
import com.inouiw.vastra.ui.theme.Ink
import com.inouiw.vastra.ui.theme.Muted
import com.inouiw.vastra.ui.theme.Paper
import com.inouiw.vastra.ui.theme.Soft

@Composable
fun PlacedScreen(store: AppStore, code: String, pickup: String, total: Int) {
    // Ring draws first, then the tick. Both run once when the screen opens.
    val ring = remember { Animatable(0f) }
    val tick = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(200)
        ring.animateTo(1f, tween(1000, easing = LinearEasing))
        tick.animateTo(1f, tween(500))
    }

    Column(Modifier.fillMaxSize().background(Color.White).padding(horizontal = 24.dp)) {
        Text(
            "Order placed",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 22.dp),
        )

        Column(Modifier.weight(1f).padding(top = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Canvas(Modifier.padding(top = 10.dp, bottom = 22.dp).size(104.dp)) {
                val s = size.width / 120f
                drawArc(
                    color = Ink,
                    startAngle = -90f,
                    sweepAngle = 360f * ring.value,
                    useCenter = false,
                    topLeft = Offset(10f * s, 10f * s),
                    size = androidx.compose.ui.geometry.Size(100f * s, 100f * s),
                    style = Stroke(width = 2f * s, cap = StrokeCap.Round),
                )
                // Tick: first stroke covers 38,62 → 54,78; second covers 54,78 → 84,46.
                val a = Offset(38f * s, 62f * s)
                val b = Offset(54f * s, 78f * s)
                val c = Offset(84f * s, 46f * s)
                val l1 = 22.6f * s
                val l2 = 42.4f * s
                val drawn = tick.value * (l1 + l2)
                val end1 = if (drawn <= l1) a + (b - a) * (drawn / l1) else b
                drawLine(Ink, a, end1, strokeWidth = 3f * s, cap = StrokeCap.Round)
                if (drawn > l1) {
                    val f = (drawn - l1) / l2
                    drawLine(Ink, b, b + (c - b) * f, strokeWidth = 3f * s, cap = StrokeCap.Round)
                }
            }

            Text("ORDER #$code", fontSize = 12.sp, letterSpacing = 0.14.em, color = Muted)
            Text(
                "You're all set",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.01).em,
                color = Ink,
                modifier = Modifier.padding(top = 6.dp),
            )
            Text(
                "Collect from the store. Pay at the counter when you pick up.",
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = Soft,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, start = 10.dp, end = 10.dp),
            )

            // Pickup code card
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFF5F5F5))
                    .padding(vertical = 18.dp, horizontal = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("PICKUP CODE", fontSize = 11.sp, letterSpacing = 0.14.em, color = Muted)
                Text(pickup, fontSize = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.3.em, color = Ink, modifier = Modifier.padding(top = 4.dp))
                Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Badge("Pay in store")
                    Badge("Ready in ~2 hrs")
                }
            }

            // Collect at / total
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFF5F5F5))
                    .padding(18.dp),
            ) {
                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Collect at", fontSize = 14.sp, color = Soft)
                    Text("Vastra Store, Lower Parel", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink)
                }
                Row(
                    Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("To pay at counter", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Text(gbp(total), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Ink)
                }
            }
        }

        // Footer buttons
        Column(Modifier.fillMaxWidth().padding(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Ink)
                    .clickable { store.push(Route.Store) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text("Store location", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Paper)
                Spacer(Modifier.width(10.dp))
                Icon(VIcons.Pin, null, tint = Paper, modifier = Modifier.size(18.dp))
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(50))
                    .border(1.dp, Ink, RoundedCornerShape(50))
                    .clickable { store.reset(Route.Home) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text("Back to home", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Ink)
            }
        }
    }
}

@Composable
private fun Badge(text: String) {
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .border(1.dp, Color(0xFFE5E5E5), RoundedCornerShape(50))
            .background(Color.White)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Ink)
    }
}
