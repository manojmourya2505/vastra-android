package com.inouiw.vastra.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.graphics.BitmapFactory
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inouiw.vastra.data.Garments
import com.inouiw.vastra.data.Product
import com.inouiw.vastra.ui.theme.Hairline
import com.inouiw.vastra.ui.theme.Ink
import com.inouiw.vastra.ui.theme.Line
import com.inouiw.vastra.ui.theme.Muted
import com.inouiw.vastra.ui.theme.Paper
import com.inouiw.vastra.ui.theme.Pill
import com.inouiw.vastra.ui.theme.Placeholder
import com.inouiw.vastra.ui.theme.Soft
import com.inouiw.vastra.ui.theme.Stage
import com.inouiw.vastra.ui.theme.Stone
import com.inouiw.vastra.ui.theme.ThumbBg
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

private val easeOut = CubicBezierEasing(0.2f, 0.7f, 0.2f, 1f)

/** .rise: fade and lift 12dp, 0.6s, with an optional delay. */
@Composable
fun Rise(delayMs: Int = 0, content: @Composable () -> Unit) {
    val p = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        p.animateTo(1f, tween(600, easing = easeOut))
    }
    Box(Modifier.graphicsLayer {
        alpha = p.value
        translationY = (1f - p.value) * 12.dp.toPx()
    }) { content() }
}

/** The app frame: white, status-bar padded, 20dp gutters. */
@Composable
fun Screen(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier
            .fillMaxSize()
            .background(Paper)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        content = content,
    )
}

/** .round: 44dp grey circle. */
@Composable
fun RoundButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Pill)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, description, tint = Ink, modifier = Modifier.size(20.dp))
    }
}

/** .btn: 56dp pill, black, 15sp semibold. Disabled uses #d6d6d6. */
@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    outlined: Boolean = false,
) {
    val bg = when {
        !enabled -> Stone
        outlined -> Paper
        else -> Ink
    }
    val fg = if (outlined) Ink else Paper
    Box(
        modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(bg)
            .then(if (outlined) Modifier.border(1.dp, Ink, RoundedCornerShape(percent = 50)) else Modifier)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = fg, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** .chip: 40dp pill, 1dp #e5e5e5 border, 13sp semibold; .chip.on is black. */
@Composable
fun Chip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .height(40.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(if (selected) Ink else Paper)
            .border(1.dp, if (selected) Ink else Line, RoundedCornerShape(percent = 50))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = if (selected) Paper else Ink, fontSize = 13.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold, maxLines = 1)
    }
}

/** .eyebrow: 12sp muted. */
@Composable
fun Eyebrow(text: String, modifier: Modifier = Modifier) {
    Text(text, fontSize = 12.sp, color = Muted, modifier = modifier)
}

/** Draws a product silhouette exactly as the prototype's SVG does (70x84 viewBox, centred). */
@Composable
fun GarmentArt(product: Product, modifier: Modifier = Modifier) {
    if (product.photo.isNotEmpty()) { ProductPhoto(product.photo, modifier); return }
    val fill = Color(product.fill)
    val collarColor = if (fill.luminance() < 0.4f) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.15f)
    Canvas(modifier) {
        val s = minOf(size.width / 70f, size.height / 84f)
        val dx = (size.width - 70f * s) / 2f
        val dy = (size.height - 84f * s) / 2f
        withTransform({
            translate(dx, dy)
            scale(s, s, pivot = Offset.Zero)
        }) {
            drawPath(Garments.body(product.shape), fill)
            drawPath(Garments.collar(product.shape), collarColor, style = Stroke(width = 1.6f, cap = StrokeCap.Round))
        }
    }
}

/** The user's photo, or the plain grey placeholder when none is saved. */
@Composable
fun Photo(path: String?, modifier: Modifier = Modifier) {
    val bitmap: ImageBitmap? = remember(path) {
        path?.let { runCatching { BitmapFactory.decodeFile(it)?.asImageBitmap() }.getOrNull() }
    }
    if (bitmap != null) {
        Image(bitmap, contentDescription = "Your photo", contentScale = ContentScale.Crop, modifier = modifier)
    } else {
        Box(modifier.background(Stage), contentAlignment = Alignment.Center) {
            Icon(VIcons.User, null, tint = Placeholder, modifier = Modifier.size(24.dp))
        }
    }
}

/** .tile: bordered card with a 158dp picture well, name, colour and price row. */
@Composable
fun ProductTile(
    product: Product,
    added: Boolean,
    onAdd: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, Hairline, RoundedCornerShape(22.dp))
            .background(Paper)
            .clickable(onClick = onOpen)
            .padding(10.dp),
    ) {
        Box(
            Modifier.fillMaxWidth().height(158.dp).clip(RoundedCornerShape(16.dp)).background(ThumbBg),
            contentAlignment = Alignment.Center,
        ) {
            GarmentArt(product, Modifier.size(92.dp, 110.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text("${product.category} · ${product.colour}", fontSize = 11.sp, color = Muted, maxLines = 1)
        Text(product.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        Row(
            Modifier.fillMaxWidth().padding(top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(gbp(product.price), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            AddButton(added, onAdd)
        }
    }
}

/** .addb: black pill "Add", or white outlined "Added" once in the bag. */
@Composable
fun AddButton(added: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(if (added) Paper else Ink)
            .border(1.dp, Ink, RoundedCornerShape(percent = 50))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(if (added) "Added" else "Add", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (added) Ink else Paper)
    }
}

fun gbp(n: Int): String = "£" + NumberFormat.getIntegerInstance(Locale.UK).format(n)

enum class Tab(val label: String, val icon: ImageVector) {
    Home("Home", VIcons.Home),
    Planner("Planner", VIcons.Calendar),
    Bag("Bag", VIcons.Bag),
    Profile("Profile", VIcons.User),
}

/**
 * .fnav: a floating 60dp pill (#f0f0f0, soft shadow) 20dp in from the edges.
 * The selected item is a black pill with its label; the others are 56dp icon-only.
 */
@Composable
fun BottomNav(current: Tab, bagCount: Int, onTab: (Tab) -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(percent = 50)
    Row(
        modifier
            .fillMaxWidth()
            .height(60.dp)
            .shadow(14.dp, shape, ambientColor = Color.Black.copy(alpha = 0.12f), spotColor = Color.Black.copy(alpha = 0.12f))
            .clip(shape)
            .background(Color(0xFFF0F0F0))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Tab.entries.forEach { t ->
            val selected = t == current
            Box(
                Modifier
                    .height(40.dp)
                    .then(if (selected) Modifier else Modifier.width(56.dp))
                    .clip(RoundedCornerShape(percent = 50))
                    .background(if (selected) Ink else Color.Transparent)
                    .clickable { onTab(t) }
                    .padding(horizontal = if (selected) 18.dp else 0.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box {
                        Icon(t.icon, t.label, tint = if (selected) Paper else Ink, modifier = Modifier.size(20.dp))
                        if (t == Tab.Bag && bagCount > 0) {
                            Box(
                                Modifier
                                    .size(20.dp)
                                    .offset(x = 10.dp, y = (-8).dp)
                                    .clip(CircleShape)
                                    .background(Ink)
                                    .border(2.dp, Paper, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(bagCount.toString(), color = Paper, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    if (selected) Text(t.label, color = Paper, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

/** A real product photo bundled in the app's assets (see Product.photo). */
@Composable
fun ProductPhoto(path: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap by produceState<ImageBitmap?>(initialValue = null, path) {
        value = withContext(Dispatchers.IO) {
            runCatching { context.assets.open(path).use { BitmapFactory.decodeStream(it)?.asImageBitmap() } }.getOrNull()
        }
    }
    bitmap?.let { Image(bitmap = it, contentDescription = null, contentScale = ContentScale.Fit, modifier = modifier) }
}
