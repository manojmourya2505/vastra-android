package com.inouiw.vastra.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.inouiw.vastra.data.AppStore
import com.inouiw.vastra.data.Route
import com.inouiw.vastra.ui.TopBar
import com.inouiw.vastra.ui.VIcons
import com.inouiw.vastra.ui.gbp
import com.inouiw.vastra.ui.theme.Ink
import com.inouiw.vastra.ui.theme.Muted
import com.inouiw.vastra.ui.theme.Paper
import com.inouiw.vastra.ui.theme.Soft

@Composable
fun CheckoutScreen(store: AppStore) {
    val count = store.bagCount()
    val total = store.bagTotal()
    val canPlace = count > 0

    Column(Modifier.fillMaxSize().background(Color.White).padding(horizontal = 20.dp)) {
        TopBar(title = "Checkout", onBack = { store.back() })

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Text(
                "COLLECT IN STORE",
                fontSize = 12.sp,
                letterSpacing = 0.14.em,
                color = Muted,
                modifier = Modifier.padding(top = 14.dp),
            )
            Text(
                "Confirm pickup",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.01).em,
                color = Ink,
                modifier = Modifier.padding(top = 4.dp),
            )

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
                    Box(
                        Modifier.size(44.dp).clip(CircleShape).background(Ink),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(VIcons.Pin, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Column(Modifier.weight(1f).padding(start = 14.dp)) {
                        Text("Vastra Store, Lower Parel", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink)
                        Text("Ground floor, High Street Phoenix, Mumbai 400013", fontSize = 12.sp, lineHeight = 17.sp, color = Soft, modifier = Modifier.padding(top = 2.dp))
                        Text("Ready in about 2 hours", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Ink, modifier = Modifier.padding(top = 6.dp))
                    }
                }
                Text(
                    "View store location",
                    fontSize = 12.sp,
                    color = Ink,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.padding(top = 12.dp).clickable { store.push(Route.Store) },
                )
            }

            Text("Payment", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Ink, modifier = Modifier.padding(top = 24.dp))

            // Selected option: pay in store
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(22.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(44.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFF2F2F2)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(VIcons.Pin, null, tint = Ink, modifier = Modifier.size(20.dp))
                }
                Column(Modifier.weight(1f).padding(start = 14.dp)) {
                    Text("Pay in store", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Text("Pay at the counter when you collect", fontSize = 12.sp, color = Muted)
                }
                Box(
                    Modifier.size(22.dp).clip(CircleShape).border(1.5.dp, Ink, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(Modifier.size(12.dp).clip(CircleShape).background(Ink))
                }
            }

            // Totals
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFF5F5F5))
                    .padding(18.dp),
            ) {
                TotalRow("Items", count.toString())
                TotalRow("Pickup", "Free")
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("To pay in store", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Text(gbp(total), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Ink)
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // Place order
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 28.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(50))
                .background(if (canPlace) Ink else Color(0xFFD6D6D6))
                .clickable(enabled = canPlace) {
                    val order = store.placeOrder()
                    store.reset(Route.Home)
                    store.push(Route.Placed(order.code, order.pickup, order.total))
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text("Place order", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Paper)
            Spacer(Modifier.width(10.dp))
            Icon(VIcons.ChevronRight, null, tint = Paper, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun TotalRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, fontSize = 14.sp, color = Soft)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink)
    }
}

