package com.inouiw.vastra.ui.screens

import androidx.compose.ui.platform.LocalContext

import android.graphics.Bitmap
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.inouiw.vastra.data.AppStore
import com.inouiw.vastra.data.Route
import com.inouiw.vastra.data.Stylist
import com.inouiw.vastra.ui.BuildOverlay
import com.inouiw.vastra.ui.TopBar
import com.inouiw.vastra.ui.ShimmerFrame
import com.inouiw.vastra.ui.gbp
import com.inouiw.vastra.ui.theme.Ink
import com.inouiw.vastra.ui.theme.Muted
import com.inouiw.vastra.ui.theme.Paper
import java.io.File
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlinx.coroutines.launch

/** The three angles, in stack order. Keys match the stylist server. */
private val lookAngles = listOf(
    "three-quarter" to "Three-quarter",
    "side" to "Side",
    "back" to "Back",
)

private val loaderSteps = listOf("Reading the dress", "Building 3 angles", "Fitting it to your size", "Almost ready")

/**
 * Shows the shopper's own photo dressed in the chosen outfit, from three angles, as a swipeable card stack.
 * The pictures are requested as soon as the screen opens. A loader covers the stack until they are all ready;
 * the top bar stays tappable so the shopper can leave at any time.
 */
@Composable
fun LooksScreen(store: AppStore) {
    val outfit = store.lookOutfit
    val photoPath = store.photoPath
    val context = LocalContext.current
    val looks = remember(outfit) { mutableStateMapOf<String, Bitmap?>() }
    val allReady = lookAngles.all { looks.containsKey(it.first) }
    var loaded by remember(outfit) { mutableStateOf(false) }
    LaunchedEffect(allReady) { if (allReady) loaded = true }
    var head by remember(outfit) { mutableIntStateOf(0) }
    val allInBag = outfit?.items?.all { store.inBag(it.id) } ?: false

    LaunchedEffect(outfit) {
        if (outfit == null) return@LaunchedEffect
        val photo = photoPath?.let { path -> try { File(path).readBytes() } catch (e: Exception) { null } }
        if (photo == null) {
            lookAngles.forEach { (key, _) -> looks[key] = null }
            return@LaunchedEffect
        }
        val pieces = outfit.items.map { "${it.name} in ${it.colour.lowercase()}" }
        val garments = Stylist.garmentBytes(context, outfit.items)
        Stylist.tryOnSet(photo, garments, pieces, lookAngles.map { it.first }) { key, bmp -> looks[key] = bmp }
    }

    Column(Modifier.fillMaxSize().background(Paper).padding(horizontal = 20.dp)) {
        TopBar(title = "Your looks", onBack = { store.back() })

        if (outfit == null) {
            Text("Choose an outfit first.", fontSize = 14.sp, color = Muted, modifier = Modifier.padding(top = 20.dp))
            return@Column
        }

        Text(outfit.label, fontSize = 12.sp, letterSpacing = 0.14.em, color = Muted, modifier = Modifier.padding(top = 14.dp))
        Text(
            outfit.items.joinToString(" + ") { it.name },
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Ink,
            modifier = Modifier.padding(top = 4.dp),
        )

        Box(Modifier.fillMaxWidth().weight(1f).padding(top = 14.dp)) {
            if (loaded) {
                CardStack(
                    looks = looks,
                    head = head,
                    onNext = { head = (head + 1) % lookAngles.size },
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                ShimmerFrame(Modifier.fillMaxSize(), "Dressing you in this look\u2026")
            }
        }

        if (loaded) {
            Text(
                "${head + 1} / ${lookAngles.size}",
                fontSize = 12.sp,
                color = Muted,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                textAlign = TextAlign.Center,
            )
            Text(
                "Swipe left or right to see the next angle",
                fontSize = 12.sp,
                color = Muted,
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                textAlign = TextAlign.Center,
            )
            Text(
                lookAngles[head].second,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Ink,
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                textAlign = TextAlign.Center,
            )
            Row(
                Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                lookAngles.indices.forEach { i ->
                    Box(
                        Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (i == head) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (i == head) Ink else Color(0xFFD6D6D6)),
                    )
                }
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(top = 14.dp, bottom = 16.dp)
                .height(56.dp)
                .clip(RoundedCornerShape(50))
                .background(if (allReady) Ink else Color(0xFFBDBDBD))
                .clickable(enabled = allReady) {
                    if (allInBag) {
                        store.push(Route.Bag)
                    } else {
                        outfit.items.forEach { if (!store.inBag(it.id)) store.addToBag(it.id) }
                    }
                }
                .padding(horizontal = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(if (!allReady) "Building your views\u2026" else if (allInBag) "Go to bag" else "Add outfit to bag", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Paper)
            Text(gbp(outfit.total), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Paper)
        }
    }
}

/**
 * Bumble-style stack: the three angles sit one behind another; drag the top card sideways
 * and it flies off to reveal the next angle.
 */
@Composable
private fun CardStack(
    looks: Map<String, Bitmap?>,
    head: Int,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val n = lookAngles.size
    val dragX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val nextState by rememberUpdatedState(onNext)

    Box(modifier) {
        // Back cards first, so the top card is drawn last.
        for (pos in 2 downTo 0) {
            val (key, label) = lookAngles[(head + pos) % n]
            val bmp = looks[key]
            val failed = looks.containsKey(key) && bmp == null
            val top = pos == 0
            val cardModifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(if (top) dragX.value.roundToInt() else 0, (pos * 10).dp.roundToPx()) }
                .graphicsLayer {
                    val s = 1f - pos * 0.04f
                    scaleX = s
                    scaleY = s
                    rotationZ = if (top) dragX.value / 18f else 0f
                }
                .shadow(12.dp, RoundedCornerShape(22.dp))
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFFF5F5F5))
            Box(
                if (top) {
                    cardModifier.pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                scope.launch {
                                    if (abs(dragX.value) > 120f) {
                                        dragX.animateTo(sign(dragX.value) * 1200f, tween(260))
                                        nextState()
                                        dragX.snapTo(0f)
                                    } else {
                                        dragX.animateTo(0f, spring())
                                    }
                                }
                            },
                            onDragCancel = { scope.launch { dragX.animateTo(0f, spring()) } },
                            onHorizontalDrag = { _, d -> scope.launch { dragX.snapTo(dragX.value + d) } },
                        )
                    }
                } else {
                    cardModifier
                },
                contentAlignment = Alignment.Center,
            ) {
                when {
                    bmp != null -> Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "You in this outfit, $label",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    failed -> Text("Could not make this angle.", fontSize = 13.sp, color = Muted)
                    else -> CircularProgressIndicator(color = Ink, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}
