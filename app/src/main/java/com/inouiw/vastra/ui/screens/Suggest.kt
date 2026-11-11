package com.inouiw.vastra.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.inouiw.vastra.data.AppStore
import com.inouiw.vastra.data.Catalog
import com.inouiw.vastra.data.Outfit
import com.inouiw.vastra.data.Outfits
import com.inouiw.vastra.data.Route
import com.inouiw.vastra.ui.GarmentArt
import com.inouiw.vastra.ui.TopBar
import com.inouiw.vastra.ui.gbp
import com.inouiw.vastra.ui.theme.Ink
import com.inouiw.vastra.ui.theme.Muted
import com.inouiw.vastra.ui.theme.Paper

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.inouiw.vastra.data.Stylist
import java.io.File

private val occasionName = mapOf("dinner" to "Dinner", "office" to "Office", "weekend" to "Weekend", "nightwear" to "Nightwear")

@Composable
fun SuggestScreen(store: AppStore) {
    val plan = store.plan
    val catalog = Catalog.items.toList()
    val outfits = remember(plan, catalog, store.stylistOutfits) { store.stylistOutfits ?: Outfits.build(plan, catalog) }
    val occ = occasionName[plan.occ] ?: "Dinner"
    var chosen by remember { mutableStateOf<Outfit?>(null) }

    Column(Modifier.fillMaxSize().background(Paper).padding(horizontal = 20.dp)) {
        TopBar(
            title = chosen?.label ?: "Looks",
            onBack = { if (chosen != null) chosen = null else store.back() },
        )

        val picked = chosen
        if (picked == null) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
                Text("$occ · ${plan.whenLabel}", fontSize = 12.sp, letterSpacing = 0.14.em, color = Muted, modifier = Modifier.padding(top = 14.dp))
                Text(
                    "Looks under ${gbp(plan.budget)}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.02).em,
                    lineHeight = 32.sp,
                    color = Ink,
                    modifier = Modifier.padding(top = 6.dp),
                )
                if (outfits.isEmpty()) {
                    Text(
                        "No complete outfit in stock fits this occasion and budget. Go back and raise the budget.",
                        fontSize = 14.sp,
                        color = Muted,
                        modifier = Modifier.fillMaxWidth().padding(top = 30.dp),
                    )
                } else {
                    outfits.forEach { o -> OutfitCard(o) { chosen = o } }
                }
            }
        } else {
            OutfitDetail(store, picked, Modifier.weight(1f))
        }
    }
}

/** One outfit: its label, a picture of each piece, the names and the total. */
@Composable
private fun OutfitCard(o: Outfit, onOpen: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 14.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFFF5F5F5))
            .clickable(onClick = onOpen)
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(o.label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Ink, modifier = Modifier.weight(1f))
            Text(gbp(o.total), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
        }
        Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            o.items.forEach { p ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(110.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    GarmentArt(p, Modifier.fillMaxSize(0.8f))
                }
            }
        }
        Text(o.items.joinToString(" + ") { it.name }, fontSize = 12.sp, color = Muted, modifier = Modifier.padding(top = 10.dp))
        if (o.reason.isNotEmpty()) Text(o.reason, fontSize = 12.sp, color = Ink, modifier = Modifier.padding(top = 6.dp))
    }
}

/** One outfit in full: the try-on picture (once made), the angle choice, the pieces and the add-to-bag button. */
@Composable
private fun OutfitDetail(store: AppStore, o: Outfit, modifier: Modifier) {
    val allInBag = o.items.all { store.inBag(it.id) }
    var note by remember(o) { mutableStateOf("") }

    fun tryOn() {
        if (store.photoPath == null) {
            note = "Add your photo in setup first."
            return
        }
        note = ""
        store.lookOutfit = o
        store.push(Route.Looks)
    }

    Column(modifier.fillMaxWidth()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = 14.dp)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(50))
                    .border(1.dp, Ink, RoundedCornerShape(50))
                    .clickable { tryOn() },
                contentAlignment = Alignment.Center,
            ) {
                Text("Try it on me", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink)
            }
            if (note.isNotEmpty()) Text(note, fontSize = 12.sp, color = Muted, modifier = Modifier.padding(top = 8.dp))

            o.items.forEach { p ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFF5F5F5))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier.size(84.dp).clip(RoundedCornerShape(14.dp)).background(Color.White),
                        contentAlignment = Alignment.Center,
                    ) {
                        GarmentArt(p, Modifier.fillMaxSize(0.85f))
                    }
                    Column(Modifier.weight(1f).padding(start = 14.dp)) {
                        Text(p.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink)
                        Text("${p.colour} · ${p.category}", fontSize = 12.sp, color = Muted, modifier = Modifier.padding(top = 2.dp))
                        Text(gbp(p.price), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink, modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Outfit total", fontSize = 14.sp, color = Muted, modifier = Modifier.weight(1f))
                Text(gbp(o.total), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
            }
        }

        Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(top = 12.dp, bottom = 16.dp)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Ink)
                    .clickable {
                        if (allInBag) {
                            store.push(Route.Bag)
                        } else {
                            o.items.forEach { if (!store.inBag(it.id)) store.addToBag(it.id) }
                        }
                    }
                    .padding(horizontal = 22.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(if (allInBag) "Go to bag" else "Add outfit to bag", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Paper)
                Text(gbp(o.total), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Paper)
            }
        }
    }
}
