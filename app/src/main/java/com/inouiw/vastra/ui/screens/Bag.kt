package com.inouiw.vastra.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.inouiw.vastra.data.AppStore
import com.inouiw.vastra.data.Catalog
import com.inouiw.vastra.data.Route
import com.inouiw.vastra.ui.BottomNav
import com.inouiw.vastra.ui.GarmentArt
import com.inouiw.vastra.ui.Tab
import com.inouiw.vastra.ui.TopBar
import com.inouiw.vastra.ui.gbp
import com.inouiw.vastra.ui.theme.Ink
import com.inouiw.vastra.ui.theme.Muted
import com.inouiw.vastra.ui.theme.Paper
import kotlinx.coroutines.launch

@Composable
fun BagScreen(store: AppStore) {
    val lines = store.bag
    val count = lines.sumOf { it.qty }
    val total = store.bagTotal()

    Box(Modifier.fillMaxSize().background(Paper)) {
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            TopBar(title = "Bag", onBack = { store.back() })

            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(bottom = 16.dp)) {
                Text(
                    "$count ${if (count == 1) "item" else "items"}",
                    fontSize = 14.sp,
                    color = Muted,
                    modifier = Modifier.padding(top = 8.dp),
                )

                if (count == 0) {
                    Column(Modifier.fillMaxWidth().padding(top = 60.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Your bag is empty.", fontSize = 14.sp, color = Muted)
                        Text(
                            "Shop now",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.padding(top = 6.dp).clickable { store.tab(Route.Home) },
                        )
                    }
                }

                lines.forEachIndexed { i, line ->
                    val item = Catalog.byId(line.id) ?: return@forEachIndexed
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFF5F5F5))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            Modifier
                                .size(60.dp, 72.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center,
                        ) {
                            GarmentArt(item, Modifier.size(46.dp, 56.dp))
                        }
                        Column(Modifier.weight(1f).padding(start = 14.dp)) {
                            Text("${item.category} · ${line.size}", fontSize = 11.sp, color = Muted)
                            Text(item.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink, modifier = Modifier.padding(top = 2.dp))
                            Row(
                                Modifier.fillMaxWidth().padding(top = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    QtyButton("−") { store.setQty(i, line.qty - 1) }
                                    Text(line.qty.toString(), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Ink, modifier = Modifier.padding(horizontal = 10.dp))
                                    QtyButton("+") { store.setQty(i, line.qty + 1) }
                                }
                                Text(gbp(item.price * line.qty), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink)
                            }
                            Text(
                                "Remove",
                                fontSize = 12.sp,
                                color = Muted,
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier.padding(top = 6.dp).clickable { store.removeAt(i) },
                            )
                        }
                    }
                }
            }

            // Total and the hold-to-checkout button, above the floating nav
            Column(Modifier.fillMaxWidth().padding(bottom = 96.dp)) {
                Row(
                    Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Total", fontSize = 14.sp, color = Soft6b)
                    Text(gbp(total), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Ink)
                }
                HoldToCheckout(enabled = count > 0) { store.push(Route.Checkout) }
            }
        }

        BottomNav(
            current = Tab.Bag,
            bagCount = count,
            onTab = { store.tab(it.route()) },
            modifier = Modifier.align(Alignment.BottomCenter).padding(start = 20.dp, end = 20.dp, bottom = 22.dp),
        )
    }
}

private val Soft6b = Color(0xFF6B6B6B)

@Composable
private fun QtyButton(label: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(Color.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 16.sp, color = Ink)
    }
}

/** Hold for 1.3 seconds to check out. Letting go early resets the fill. */
@Composable
private fun HoldToCheckout(enabled: Boolean, onComplete: () -> Unit) {
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    Box(
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(50))
            .background(if (enabled) Ink else Color(0xFFD6D6D6))
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(onPress = {
                    val fill = scope.launch {
                        progress.animateTo(1f, tween(1300, easing = LinearEasing))
                        onComplete()
                    }
                    val released = tryAwaitRelease()
                    if (!released || progress.value < 1f) {
                        fill.cancel()
                        scope.launch { progress.animateTo(0f, tween(150)) }
                    }
                })
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.fillMaxWidth(progress.value).height(56.dp).background(Color(0xFF333333)))
        Text("Hold to checkout", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Paper)
    }
}
