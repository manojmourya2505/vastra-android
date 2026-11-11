package com.inouiw.vastra.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inouiw.vastra.data.AppStore
import com.inouiw.vastra.ui.TopBar
import com.inouiw.vastra.ui.VIcons
import com.inouiw.vastra.ui.theme.Ink
import com.inouiw.vastra.ui.theme.Muted
import com.inouiw.vastra.ui.theme.Paper
import com.inouiw.vastra.ui.theme.Soft

@Composable
fun StoreScreen(store: AppStore) {
    val context = LocalContext.current
    Column(Modifier.fillMaxSize().background(Color.White).padding(horizontal = 20.dp)) {
        TopBar(title = "Store location", onBack = { store.back() })

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
            // Map, drawn as a flat street plan with the store pin
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .height(280.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFEEF0EC)),
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val k = w / 390f
                    // Water/green patch along the bottom
                    val water = Path().apply {
                        moveTo(0f, 210f * k); quadraticTo(120f * k, 180f * k, 200f * k, 205f * k)
                        quadraticTo(295f * k, 230f * k, 390f * k, 190f * k)
                        lineTo(390f * k, 230f * k); quadraticTo(260f * k, 250f * k, 180f * k, 236f * k)
                        quadraticTo(90f * k, 222f * k, 0f, 250f * k); close()
                    }
                    drawPath(water, Color(0xFFDFE6E2))
                    // Blocks
                    drawRoundRect(Color(0xFFE4E8E3), Offset(250f * k, 30f * k), androidx.compose.ui.geometry.Size(110f * k, 70f * k), androidx.compose.ui.geometry.CornerRadius(10f * k))
                    drawRoundRect(Color(0xFFE4E8E3), Offset(30f * k, 50f * k), androidx.compose.ui.geometry.Size(90f * k, 60f * k), androidx.compose.ui.geometry.CornerRadius(10f * k))
                    // Main roads and side streets
                    drawLine(Color.White, Offset(-10f * k, 120f * k), Offset(400f * k, 90f * k), strokeWidth = 12f * k)
                    drawLine(Color.White, Offset(180f * k, -10f * k), Offset(210f * k, 300f * k), strokeWidth = 12f * k)
                    drawLine(Color.White, Offset(-10f * k, 240f * k), Offset(400f * k, 200f * k), strokeWidth = 12f * k)
                    drawLine(Color.White, Offset(60f * k, -10f * k), Offset(110f * k, 300f * k), strokeWidth = 5f * k)
                    drawLine(Color.White, Offset(320f * k, -10f * k), Offset(300f * k, 300f * k), strokeWidth = 5f * k)
                    drawLine(Color.White, Offset(-10f * k, 60f * k), Offset(400f * k, 30f * k), strokeWidth = 5f * k)
                    // Store pin
                    val cx = 200f * k
                    val cy = 100f * k
                    drawCircle(Color.Black.copy(alpha = 0.2f), radius = 22f * k, center = Offset(cx, cy), style = Stroke(width = 1f * k))
                    val pin = Path().apply {
                        moveTo(cx, cy - 4f * k)
                        cubicTo(cx - 4f * k, cy - 16f * k, cx - 18f * k, cy - 16f * k, cx - 18f * k, cy - 4f * k)
                        cubicTo(cx - 18f * k, cy + 10f * k, cx, cy + 24f * k, cx, cy + 24f * k)
                        cubicTo(cx, cy + 24f * k, cx + 18f * k, cy + 10f * k, cx + 18f * k, cy - 4f * k)
                        cubicTo(cx + 18f * k, cy - 16f * k, cx + 4f * k, cy - 16f * k, cx, cy - 4f * k)
                        close()
                    }
                    drawPath(pin, Color.Black)
                    drawCircle(Color.White, radius = 3.5f * k, center = Offset(cx, cy - 4f * k))
                }
            }

            // Store card
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFF5F5F5))
                    .padding(18.dp),
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(Modifier.size(44.dp).clip(CircleShape).background(Ink), contentAlignment = Alignment.Center) {
                        Icon(VIcons.Pin, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Column(Modifier.weight(1f).padding(start = 14.dp)) {
                        Text("Vastra Store, Lower Parel", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ink)
                        Text(
                            "Ground floor, High Street Phoenix, Senapati Bapat Marg, Lower Parel, Mumbai 400013",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = Soft,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }
                Row(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Directions opens the phone's map app at the store
                    ActionPill("Directions", filled = true, modifier = Modifier.weight(1f)) {
                        val uri = Uri.parse("geo:0,0?q=" + Uri.encode("Vastra Store, High Street Phoenix, Lower Parel, Mumbai"))
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    }
                    ActionPill("Call store", filled = false, icon = VIcons.Phone, modifier = Modifier.weight(1f)) {
                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:+912212345678")))
                    }
                }
            }

            Text("Opening hours", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Ink, modifier = Modifier.padding(top = 24.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFF5F5F5))
                    .padding(horizontal = 18.dp, vertical = 8.dp),
            ) {
                HoursRow("Monday to Friday", "11am to 10pm", first = true)
                HoursRow("Saturday", "11am to 11pm")
                HoursRow("Sunday", "12pm to 9pm")
            }
        }
    }
}

@Composable
private fun HoursRow(day: String, hours: String, first: Boolean = false) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(day, fontSize = 13.sp, color = Ink)
        Text(hours, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Ink)
    }
}

@Composable
private fun ActionPill(
    label: String,
    filled: Boolean,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier
            .height(46.dp)
            .clip(RoundedCornerShape(50))
            .background(if (filled) Ink else Color.White)
            .border(1.dp, Ink, RoundedCornerShape(50))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Icon(icon, null, tint = if (filled) Paper else Ink, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (filled) Paper else Ink)
    }
}
