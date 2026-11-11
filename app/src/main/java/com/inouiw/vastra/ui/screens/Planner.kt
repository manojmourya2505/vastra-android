package com.inouiw.vastra.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.inouiw.vastra.data.AppStore
import com.inouiw.vastra.data.Catalog
import com.inouiw.vastra.data.Plan
import com.inouiw.vastra.data.Stylist
import com.inouiw.vastra.data.Route
import com.inouiw.vastra.ui.BottomNav
import com.inouiw.vastra.ui.BuildOverlay
import com.inouiw.vastra.ui.Tab
import com.inouiw.vastra.ui.VIcons
import com.inouiw.vastra.ui.gbp
import com.inouiw.vastra.ui.theme.Ink
import com.inouiw.vastra.ui.theme.Muted

private data class Occasion(val id: String, val label: String, val sub: String, val icon: Int)

private val occasions = listOf(
    Occasion("dinner", "Dinner", "Evening out", 0),
    Occasion("office", "Office", "Work days", 1),
    Occasion("weekend", "Weekend", "Off duty", 2),
    Occasion("nightwear", "Nightwear", "At home", 3),
)
private val whenOptions = listOf("Tonight", "Tomorrow", "This week")
private val quickBudgets = listOf(30 to "Under £30", 50 to "£50", 100 to "£100", 150 to "£150")

private const val BUDGET_MIN = 10f
private const val BUDGET_MAX = 200f
private const val BUDGET_STEP = 5f

@Composable
fun PlannerScreen(store: AppStore) {
    var occ by remember(store.plan) { mutableStateOf(store.plan.occ) }
    var whenLabel by remember(store.plan) { mutableStateOf(store.plan.whenLabel) }
    var budget by remember(store.plan) { mutableStateOf(store.plan.budget.toFloat()) }
    var building by remember { mutableStateOf(false) }
    var request by remember { mutableStateOf("") }

    Box(Modifier.fillMaxSize().background(Color.White)) {
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            // .top: title left, Build looks pill right
            Row(
                Modifier.fillMaxWidth().padding(top = 22.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Plan", fontSize = 22.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.01).em, color = Ink)
                Row(
                    Modifier
                        .height(38.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Ink)
                        .clickable {
                            building = true
                            store.stylistOutfits = null
                            Stylist.suggest(request, Plan(occ, whenLabel, budget.toInt()), Catalog.items.toList(), Stylist.profileFor(store)) { store.stylistOutfits = it }
                        }
                        .padding(start = 14.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(VIcons.Spark, null, tint = Color.White, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(7.dp))
                    Text("Build looks", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(bottom = 120.dp)) {
                Text(
                    "What's the occasion?",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.02).em,
                    lineHeight = 32.sp,
                    color = Ink,
                    modifier = Modifier.padding(top = 22.dp),
                )

                SectionLabel("Describe it", "")
                OutlinedTextField(
                    value = request,
                    onValueChange = { request = it },
                    placeholder = { Text("e.g. cousin's wedding reception, outdoors in May", fontSize = 14.sp, color = Muted) },
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = Ink),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                SectionLabel("Occasion", "")
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    occasions.chunked(2).forEach { pair ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            pair.forEach { o ->
                                val on = occ == o.id
                                Column(
                                    Modifier
                                        .weight(1f)
                                        .height(112.dp)
                                        .clip(RoundedCornerShape(22.dp))
                                        .background(if (on) Ink else Color(0xFFF5F5F5))
                                        .clickable { occ = o.id }
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Box(
                                        Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (on) Color(0xFF1F1F1F) else Color.White),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        OccasionIcon(o.icon, if (on) Color.White else Ink)
                                    }
                                    Column {
                                        Text(o.label, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (on) Color.White else Ink)
                                        Text(o.sub, fontSize = 11.sp, color = if (on) Color.White.copy(alpha = 0.6f) else Muted)
                                    }
                                }
                            }
                        }
                    }
                }

                SectionLabel("When", whenLabel)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFF5F5F5))
                        .padding(4.dp),
                ) {
                    whenOptions.forEach { w ->
                        val on = whenLabel == w
                        Box(
                            Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(50))
                                .background(if (on) Ink else Color.Transparent)
                                .clickable { whenLabel = w },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(w, fontSize = 13.sp, fontWeight = if (on) FontWeight.Bold else FontWeight.SemiBold, color = if (on) Color.White else Muted)
                        }
                    }
                }

                SectionLabel("Budget", "")
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFFF5F5F5))
                        .padding(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 16.dp),
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            gbp(budget.toInt()),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.01).em,
                            color = Ink,
                        )
                        Text("  max", fontSize = 12.sp, color = Muted, modifier = Modifier.padding(bottom = 6.dp))
                    }
                    BudgetSlider(
                        value = budget,
                        onChange = { budget = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("£10", fontSize = 11.sp, color = Muted)
                        Text("£200", fontSize = 11.sp, color = Muted)
                    }
                    Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        quickBudgets.forEach { (value, label) ->
                            val on = budget.toInt() == value
                            Box(
                                Modifier
                                    .height(32.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(if (on) Ink else Color.White)
                                    .border(1.dp, if (on) Ink else Color(0xFFD6D6D6), RoundedCornerShape(50))
                                    .clickable { budget = value.toFloat() }
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (on) Color.White else Ink)
                            }
                        }
                    }
                }

                Text(
                    "We build three looks from pieces that fit this occasion and stay within your budget.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = Muted,
                    modifier = Modifier.padding(top = 14.dp),
                )
            }
        }

        BottomNav(
            current = Tab.Planner,
            bagCount = store.bagCount(),
            onTab = { store.tab(it.route()) },
            modifier = Modifier.align(Alignment.BottomCenter).padding(start = 20.dp, end = 20.dp, bottom = 22.dp),
        )

        if (building) {
            BuildOverlay(
                title = "Building your looks",
                steps = listOf("Reading your occasion", "Choosing silhouettes", "Matching your budget", "Almost ready"),
                durationMs = 14_000,
            ) {
                store.updatePlan(Plan(occ, whenLabel, budget.toInt()))
                store.push(Route.Suggest)
            }
        }
    }
}

/** Track, filled portion and a white thumb with a black ring. Snaps to £5 steps. */
@Composable
private fun BudgetSlider(value: Float, onChange: (Float) -> Unit, modifier: Modifier = Modifier) {
    var width by remember { mutableFloatStateOf(1f) }
    fun snap(x: Float): Float {
        val f = (x / width).coerceIn(0f, 1f)
        val raw = BUDGET_MIN + f * (BUDGET_MAX - BUDGET_MIN)
        return (Math.round(raw / BUDGET_STEP) * BUDGET_STEP).coerceIn(BUDGET_MIN, BUDGET_MAX)
    }
    val frac = ((value - BUDGET_MIN) / (BUDGET_MAX - BUDGET_MIN)).coerceIn(0f, 1f)

    Box(
        modifier
            .height(28.dp)
            .onSizeChanged { width = it.width.toFloat() }
            .pointerInput(Unit) { detectTapGestures { onChange(snap(it.x)) } }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ ->
                    change.consume()
                    onChange(snap(change.position.x))
                }
            },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val thumbR = 12.dp.toPx()
            val trackH = 6.dp.toPx()
            val y = size.height / 2f
            val usable = size.width - 2 * thumbR
            val cx = thumbR + usable * frac
            drawRoundRect(
                Color(0xFFD6D6D6),
                Offset(0f, y - trackH / 2),
                Size(size.width, trackH),
                CornerRadius(trackH / 2),
            )
            drawRoundRect(
                Ink,
                Offset(0f, y - trackH / 2),
                Size(cx, trackH),
                CornerRadius(trackH / 2),
            )
            drawCircle(Color.White, thumbR, Offset(cx, y))
            drawCircle(Ink, thumbR - 1.dp.toPx(), Offset(cx, y), style = Stroke(2.dp.toPx()))
        }
    }
}

@Composable
private fun SectionLabel(title: String, note: String) {
    Row(
        Modifier.fillMaxWidth().padding(top = 28.dp, bottom = 12.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Ink)
        if (note.isNotEmpty()) Text(note, fontSize = 12.sp, color = Muted)
    }
}

/** Simple line icons for the four occasions, drawn with the same geometry as the prototype. */
@Composable
private fun OccasionIcon(index: Int, tint: Color) {
    androidx.compose.foundation.Canvas(Modifier.size(18.dp)) {
        val s = size.width / 24f
        val st = Stroke(width = 1.7f * s, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
        when (index) {
            0 -> { // cocktail glass
                val p = androidx.compose.ui.graphics.Path().apply {
                    moveTo(8f * s, 3f * s); lineTo(16f * s, 3f * s); lineTo(15f * s, 9f * s); quadraticTo(15f * s, 12f * s, 12f * s, 12f * s)
                    quadraticTo(9f * s, 12f * s, 9f * s, 9f * s); close()
                }
                drawPath(p, tint, style = st)
                drawLine(tint, Offset(12f * s, 12f * s), Offset(12f * s, 20f * s), strokeWidth = 1.7f * s, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                drawLine(tint, Offset(9f * s, 21f * s), Offset(15f * s, 21f * s), strokeWidth = 1.7f * s, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            }
            1 -> { // briefcase
                drawRoundRect(tint, Offset(3f * s, 7f * s), Size(18f * s, 13f * s), CornerRadius(2f * s), style = st)
                val p = androidx.compose.ui.graphics.Path().apply {
                    moveTo(9f * s, 7f * s); lineTo(9f * s, 5f * s); quadraticTo(9f * s, 3f * s, 11f * s, 3f * s); lineTo(13f * s, 3f * s)
                    quadraticTo(15f * s, 3f * s, 15f * s, 5f * s); lineTo(15f * s, 7f * s)
                }
                drawPath(p, tint, style = st)
            }
            2 -> { // sun
                drawCircle(tint, radius = 4f * s, center = Offset(12f * s, 12f * s), style = st)
                for (i in 0 until 8) {
                    val a = Math.toRadians((i * 45).toDouble())
                    val r1 = 7.5f; val r2 = 9.8f
                    drawLine(
                        tint,
                        Offset(12f * s + (r1 * Math.cos(a)).toFloat() * s, 12f * s + (r1 * Math.sin(a)).toFloat() * s),
                        Offset(12f * s + (r2 * Math.cos(a)).toFloat() * s, 12f * s + (r2 * Math.sin(a)).toFloat() * s),
                        strokeWidth = 1.7f * s, cap = androidx.compose.ui.graphics.StrokeCap.Round,
                    )
                }
            }
            else -> { // moon
                val p = androidx.compose.ui.graphics.Path().apply {
                    moveTo(20f * s, 14.5f * s)
                    arcTo(androidx.compose.ui.geometry.Rect(4f * s, 4f * s, 20f * s, 20f * s), 30f, 300f, false)
                    close()
                }
                drawPath(p, tint, style = st)
            }
        }
    }
}
