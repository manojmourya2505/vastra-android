package com.inouiw.vastra.ui.screens

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inouiw.vastra.data.AppStore
import com.inouiw.vastra.data.PromoSlide
import com.inouiw.vastra.data.Catalog
import com.inouiw.vastra.data.Product
import com.inouiw.vastra.data.Route
import com.inouiw.vastra.ui.BottomNav
import com.inouiw.vastra.ui.GarmentArt
import com.inouiw.vastra.ui.Photo
import com.inouiw.vastra.ui.ProductTile
import com.inouiw.vastra.ui.RoundButton
import com.inouiw.vastra.ui.Tab
import com.inouiw.vastra.ui.VIcons
import com.inouiw.vastra.ui.gbp
import com.inouiw.vastra.ui.theme.Ink
import com.inouiw.vastra.ui.theme.Line
import com.inouiw.vastra.ui.theme.Muted
import com.inouiw.vastra.ui.theme.Paper
import com.inouiw.vastra.ui.theme.Pill
import com.inouiw.vastra.ui.theme.Placeholder
import com.inouiw.vastra.ui.theme.Soft
import com.inouiw.vastra.ui.theme.Stage
import com.inouiw.vastra.ui.theme.ThumbBg
import kotlinx.coroutines.delay

private val categories = listOf("All", "Dresses", "Tops", "Bottoms", "Footwear")
private val sizes = listOf("XS", "S", "M", "L", "XL")

fun Tab.route(): Route = when (this) {
    Tab.Home -> Route.Home
    Tab.Planner -> Route.Planner
    Tab.Bag -> Route.Bag
    Tab.Profile -> Route.Profile
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(store: AppStore) {
    var category by remember { mutableStateOf("All") }
    var query by remember { mutableStateOf("") }
    var openId by remember { mutableStateOf<String?>(null) }

    val visible = Catalog.items.filter { p ->
        (category == "All" || p.category == category) &&
            (query.isBlank() || p.name.lowercase().contains(query.lowercase()))
    }

    Box(Modifier.fillMaxSize().background(Paper).statusBarsPadding()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 116.dp),
        ) {
            // .hdr2
            Row(Modifier.fillMaxWidth().padding(top = 22.dp), verticalAlignment = Alignment.CenterVertically) {
                Photo(store.photoPath, Modifier.size(48.dp).clip(CircleShape).background(Pill))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Welcome", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text("Hi there", fontSize = 14.sp, modifier = Modifier.padding(top = 2.dp))
                }
                RoundButton(VIcons.Bell, "Notifications", onClick = {})
            }

            // .srow: search pill plus black filter button
            Row(Modifier.fillMaxWidth().padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .border(1.dp, Line, RoundedCornerShape(percent = 50))
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(VIcons.Search, null, tint = Muted, modifier = Modifier.size(20.dp))
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        textStyle = TextStyle(color = Ink, fontSize = 14.sp),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        decorationBox = { inner ->
                            if (query.isEmpty()) Text("Search dresses, tops, shoes", color = Placeholder, fontSize = 14.sp)
                            inner()
                        },
                    )
                }
                Box(
                    Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(Ink),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(VIcons.Sliders, "Filters", tint = Paper, modifier = Modifier.size(20.dp))
                }
            }

            PromoCarousel(
                slides = store.promos,
                onShopDresses = { category = "Dresses" },
                onShopTops = { category = "Tops" },
                onPlan = { store.tab(Route.Planner) },
            )

            // .shead
            Row(Modifier.fillMaxWidth().padding(top = 22.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Categories", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("${visible.size} pieces", fontSize = 12.sp, color = Muted)
            }

            // .cats
            Row(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                categories.forEach { c ->
                    CategoryButton(c, selected = c == category) { category = c }
                }
            }

            // .grid
            if (visible.isEmpty()) {
                Text("Nothing matches. Try another search.", fontSize = 13.sp, color = Muted, modifier = Modifier.fillMaxWidth().padding(top = 24.dp))
            } else {
                Column(Modifier.fillMaxWidth().padding(top = 14.dp)) {
                    visible.chunked(2).forEach { row ->
                        Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            row.forEach { p ->
                                ProductTile(
                                    product = p,
                                    added = store.inBag(p.id),
                                    onAdd = { store.addToBag(p.id) },
                                    onOpen = { openId = p.id },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        BottomNav(
            current = Tab.Home,
            bagCount = store.bagCount(),
            onTab = { store.tab(it.route()) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 20.dp, end = 20.dp, bottom = 22.dp),
        )
    }

    // .detail overlay covers the whole screen, nav included
    openId?.let { id ->
        Catalog.byId(id)?.let { product ->
            ProductDetail(
                product = product,
                defaultSize = store.size,
                inBag = store.inBag(product.id),
                onClose = { openId = null },
                onToggleBag = { size ->
                    if (store.inBag(product.id)) store.removeFromBag(product.id) else store.addToBag(product.id, size)
                },
                onTryOn = { openId = null; store.push(Route.TryOn(product.id)) },
            )
        }
    }
}

/** .cat: 46dp grey circle with a garment icon, 11sp label; the selected one gets a black ring. */
@Composable
private fun CategoryButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        Modifier
            .width(52.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(52.dp), contentAlignment = Alignment.Center) {
            if (selected) Box(Modifier.size(52.dp).clip(CircleShape).background(Pill))
            Box(
                Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (selected) Paper else ThumbBg)
                    .border(1.5.dp, if (selected) Ink else Color.Transparent, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (label == "All") {
                    Icon(VIcons.Grid, null, tint = Ink, modifier = Modifier.size(22.dp))
                } else {
                    val sample = Catalog.items.first { it.category == label }
                    GarmentArt(sample, Modifier.size(22.dp, 27.dp))
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(label, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold, color = Ink)
    }
}

/** .promo: three slides moving every 3 seconds, with dots underneath. */
@Composable
private fun PromoCarousel(slides: List<PromoSlide>, onShopDresses: () -> Unit, onShopTops: () -> Unit, onPlan: () -> Unit) {
    val pager = rememberPagerState(pageCount = { 3 })
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            pager.animateScrollToPage((pager.currentPage + 1) % 3)
        }
    }
    Column(Modifier.fillMaxWidth().padding(top = 18.dp)) {
        HorizontalPager(state = pager, modifier = Modifier.fillMaxWidth()) { page ->
            val slide = slides[page]
            val bg = if (page == 0) Ink else Pill
            val fg = if (page == 0) Paper else Ink
            val btnBg = if (page == 0) Paper else Ink
            val btnFg = if (page == 0) Ink else Paper
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(172.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(bg)
                    .padding(start = 22.dp, top = 20.dp, end = 18.dp, bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(slide.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = fg, lineHeight = 22.sp)
                    Text(slide.copy, fontSize = 12.sp, color = fg.copy(alpha = 0.75f), lineHeight = 17.sp, modifier = Modifier.padding(top = 6.dp, bottom = 14.dp))
                    Box(
                        Modifier
                            .height(34.dp)
                            .clip(RoundedCornerShape(percent = 50))
                            .background(btnBg)
                            .clickable {
                                when (page) {
                                    0 -> onShopDresses()
                                    1 -> onShopTops()
                                    else -> onPlan()
                                }
                            }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(slide.button, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = btnFg)
                    }
                }
                GarmentArt(Catalog.byId(slide.art)!!, Modifier.size(110.dp, 130.dp))
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.Center) {
            repeat(3) { i ->
                Box(
                    Modifier
                        .padding(horizontal = 3.dp)
                        .height(6.dp)
                        .width(if (i == pager.currentPage) 18.dp else 6.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(if (i == pager.currentPage) Ink else Line),
                )
            }
        }
    }
}



/** .detail: the full-screen product view with size picker, Try on and add to bag. */
@Composable
private fun ProductDetail(
    product: Product,
    defaultSize: String,
    inBag: Boolean,
    onClose: () -> Unit,
    onToggleBag: (String) -> Unit,
    onTryOn: () -> Unit,
) {
    var size by remember(product.id) { mutableStateOf(defaultSize) }
    val one = mapOf("Dresses" to "dress", "Tops" to "top", "Bottoms" to "bottom", "Footwear" to "shoe")[product.category] ?: "piece"

    Column(Modifier.fillMaxSize().background(Paper).statusBarsPadding().padding(horizontal = 20.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 22.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            RoundButton(VIcons.ChevronLeft, "Back", onClose)
            Text("Product", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(44.dp))
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(bottom = 12.dp)) {
            Box(
                Modifier.fillMaxWidth().padding(top = 14.dp).height(330.dp).clip(RoundedCornerShape(28.dp)).background(ThumbBg),
                contentAlignment = Alignment.Center,
            ) {
                GarmentArt(product, Modifier.size(200.dp, 240.dp))
            }
            Text(product.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, lineHeight = 29.sp, modifier = Modifier.padding(top = 18.dp))
            Text("${product.category} · ${product.colour} · ${product.style}", fontSize = 13.sp, color = Muted, modifier = Modifier.padding(top = 4.dp))
            Text(gbp(product.price), fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp))
            Text("Size", fontSize = 12.sp, color = Muted, modifier = Modifier.padding(top = 18.dp))
            Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                sizes.forEach { s ->
                    Box(
                        Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (s == size) Ink else Paper)
                            .border(1.dp, if (s == size) Ink else Placeholder, CircleShape)
                            .clickable { size = s },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(s, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (s == size) Paper else Ink)
                    }
                }
            }
            Text(
                "A ${product.style} $one in ${product.colour.lowercase()}. Choose your size, then add it to your bag.",
                fontSize = 13.sp,
                color = Soft,
                lineHeight = 21.sp,
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        // .dfoot
        Row(Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 26.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(
                Modifier
                    .width(96.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Pill)
                    .clickable(onClick = onTryOn),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(VIcons.User, null, tint = Ink, modifier = Modifier.size(20.dp))
                Text("Try on", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Row(
                Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (inBag) Pill else Ink)
                    .clickable { onToggleBag(size) }
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                if (inBag) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(VIcons.Check, null, tint = Ink, modifier = Modifier.size(18.dp))
                        Text("In your bag", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink)
                    }
                    Text("Remove", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(VIcons.Bag, null, tint = Paper, modifier = Modifier.size(18.dp))
                        Text("Add to bag", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Paper)
                    }
                    Text(gbp(product.price), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Paper)
                }
            }
        }
    }
}
