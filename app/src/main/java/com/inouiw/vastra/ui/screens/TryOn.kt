package com.inouiw.vastra.ui.screens

import androidx.compose.ui.platform.LocalContext

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.inouiw.vastra.data.AppStore
import com.inouiw.vastra.data.Catalog
import com.inouiw.vastra.data.Product
import com.inouiw.vastra.data.Route
import com.inouiw.vastra.ui.BottomNav
import com.inouiw.vastra.ui.GarmentArt
import com.inouiw.vastra.ui.Photo
import android.graphics.Bitmap
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box as BoxAlias
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.inouiw.vastra.data.Stylist
import java.io.File
import com.inouiw.vastra.ui.Tab
import com.inouiw.vastra.ui.TopBar
import com.inouiw.vastra.ui.VIcons
import com.inouiw.vastra.ui.gbp
import com.inouiw.vastra.ui.theme.Ink
import com.inouiw.vastra.ui.theme.Muted
import com.inouiw.vastra.ui.theme.Paper
import com.inouiw.vastra.ui.theme.Stone
import com.inouiw.vastra.ui.theme.Soft

private val sizes = listOf("XS", "S", "M", "L", "XL")
private val lanes = listOf(
    Triple("Dresses", "Full look", "Dresses"),
    Triple("Tops", "Shirts, tees, blazers", "Tops"),
    Triple("Bottoms", "Trousers, skirts, jeans", "Bottoms"),
    Triple("Footwear", "Heels, loafers, sneakers", "Footwear"),
)

@Composable
fun TryOnScreen(store: AppStore, itemId: String?) {
    // The pieces currently on the figure. A dress replaces tops and bottoms, and the reverse.
    var worn by remember { mutableStateOf(listOfNotNull(itemId?.let { Catalog.byId(it) })) }
    var size by remember { mutableStateOf(store.size) }

    // Three views of the shopper's photo in the chosen pieces: front, side and back.
    val angles = listOf("three-quarter" to "Three-quarter", "side" to "Side", "back" to "Back")
    val context = LocalContext.current
    val wornKey = worn.joinToString(",") { it.id }
    val looks = remember(wornKey) { mutableStateMapOf<String, Bitmap?>() }
    val photoBytes = remember(store.photoPath) {
        store.photoPath?.let { path -> try { File(path).readBytes() } catch (e: Exception) { null } }
    }
    LaunchedEffect(wornKey) {
        val pieces = worn.map { "${it.name} in ${it.colour.lowercase()}" }
        val photo = photoBytes ?: return@LaunchedEffect
        if (pieces.isEmpty()) return@LaunchedEffect
        val garments = Stylist.garmentBytes(context, worn)
        Stylist.tryOnSet(photo, garments, pieces, angles.map { it.first }) { key, bmp -> looks[key] = bmp }
    }

    fun place(p: Product) {
        worn = when (p.category) {
            "Dresses" -> listOf(p)
            "Tops", "Bottoms" -> worn.filter { it.category != "Dresses" && it.category != p.category } + p
            else -> worn.filter { it.category != "Footwear" } + p
        }
    }

    Box(Modifier.fillMaxSize().background(Paper)) {
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            TopBar(title = "Try on", onBack = { store.back() })

            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(bottom = 120.dp)) {
                // Stage: your photo when one is saved, otherwise the chosen pieces drawn on the figure.
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(460.dp)
                        .padding(top = 16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFF0F0F0)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (store.photoPath != null && worn.isNotEmpty()) {
                        LookPages(looks, angles)
                    } else if (store.photoPath != null) {
                        Photo(store.photoPath, Modifier.fillMaxSize())
                    } else {
                        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center) {
                            worn.forEach { p -> GarmentArt(p, Modifier.size(150.dp, 190.dp)) }
                        }
                    }
                    // Thumbnails of what is on, top-right
                    Column(
                        Modifier.align(Alignment.TopEnd).padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        worn.forEach { p ->
                            Box(
                                Modifier
                                    .size(52.dp, 60.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White)
                                    .border(1.dp, Stone, RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                GarmentArt(p, Modifier.size(40.dp, 48.dp))
                            }
                        }
                    }
                    Text(
                        if (worn.isEmpty()) "Nothing selected" else "Selected: " + worn.joinToString(" + ") { it.name },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Ink)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }

                lanes.forEach { (cat, note, key) ->
                    Row(Modifier.fillMaxWidth().padding(top = 20.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(cat, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink)
                        Text(note, fontSize = 12.sp, color = Muted)
                    }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Catalog.items.filter { it.category == key }.forEach { p ->
                            val on = worn.any { it.id == p.id }
                            Column(
                                Modifier
                                    .width(118.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White)
                                    .border(if (on) 1.5.dp else 1.dp, if (on) Ink else Stone, RoundedCornerShape(20.dp))
                                    .clickable { place(p) }
                                    .padding(8.dp),
                            ) {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(112.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFFF4F4F4)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    GarmentArt(p, Modifier.size(76.dp, 92.dp))
                                }
                                Text(p.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink, maxLines = 1, modifier = Modifier.padding(top = 8.dp))
                                Text(gbp(p.price), fontSize = 11.sp, color = Muted)
                            }
                        }
                    }
                }

                Row(Modifier.fillMaxWidth().padding(top = 22.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Size", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Text("From your profile · $size", fontSize = 12.sp, color = Muted)
                }
                Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    sizes.forEach { s ->
                        val on = s == size
                        Box(
                            Modifier
                                .height(40.dp)
                                .clip(RoundedCornerShape(50))
                                .background(if (on) Ink else Color.White)
                                .border(1.dp, if (on) Ink else Stone, RoundedCornerShape(50))
                                .clickable { size = s; store.size = s }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(s, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (on) Color.White else Ink)
                        }
                    }
                }

                // Add every selected piece to the bag in the chosen size, then go to the bag.
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 22.dp)
                        .height(56.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (worn.isEmpty()) Color(0xFFD6D6D6) else Ink)
                        .clickable(enabled = worn.isNotEmpty()) {
                            worn.forEach { store.addToBag(it.id, size) }
                            store.push(Route.Bag)
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text("Add to bag", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Paper)
                    Spacer(Modifier.width(10.dp))
                    Icon(VIcons.Bag, null, tint = Paper, modifier = Modifier.size(20.dp))
                }
            }
        }

        BottomNav(
            current = Tab.Planner,
            bagCount = store.bagCount(),
            onTab = { store.tab(it.route()) },
            modifier = Modifier.align(Alignment.BottomCenter).padding(start = 20.dp, end = 20.dp, bottom = 22.dp),
        )
    }
}

/** Swipeable pictures of the shopper in the chosen pieces: front, side and back, with dots. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LookPages(looks: Map<String, Bitmap?>, angles: List<Pair<String, String>>) {
    val pager = rememberPagerState(pageCount = { angles.size })
    Box(Modifier.fillMaxSize()) {
        HorizontalPager(state = pager, modifier = Modifier.fillMaxSize()) { page ->
            val (key, label) = angles[page]
            val bmp = looks[key]
            val finished = looks.containsKey(key)
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                when {
                    bmp != null -> Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "$label view",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                    )
                    finished -> Text("Could not make this view.", fontSize = 12.sp, color = Muted)
                    else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Ink, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
                        Text("Dressing you…", fontSize = 12.sp, color = Muted, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }
        Row(
            Modifier.align(Alignment.TopCenter).padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            angles.indices.forEach { i ->
                val on = pager.currentPage == i
                Box(
                    Modifier
                        .size(if (on) 9.dp else 7.dp)
                        .clip(CircleShape)
                        .background(if (on) Ink else Color(0xFFD6D6D6)),
                )
            }
        }
    }
}
