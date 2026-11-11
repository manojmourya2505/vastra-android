package com.inouiw.vastra.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inouiw.vastra.data.AppStore
import com.inouiw.vastra.data.Order
import com.inouiw.vastra.data.Route
import com.inouiw.vastra.ui.TopBar
import com.inouiw.vastra.ui.gbp
import com.inouiw.vastra.ui.theme.Ink
import com.inouiw.vastra.ui.theme.Muted
import com.inouiw.vastra.ui.theme.Paper
import com.inouiw.vastra.ui.theme.Soft

@Composable
fun OrdersScreen(store: AppStore) {
    val orders = store.orders
    Column(Modifier.fillMaxSize().background(Paper).padding(horizontal = 20.dp)) {
        TopBar(title = "Your orders", onBack = { store.back() })

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
            Text(
                if (orders.size == 1) "1 order" else "${orders.size} orders",
                fontSize = 14.sp,
                color = Muted,
                modifier = Modifier.padding(top = 8.dp),
            )

            if (orders.isEmpty()) {
                Column(Modifier.fillMaxWidth().padding(top = 60.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No orders yet.", fontSize = 14.sp, color = Muted)
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

            orders.forEach { order -> OrderCard(order) }
        }
    }
}

@Composable
private fun OrderCard(order: Order) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFF5F5F5))
            .padding(18.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("#${order.code}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink)
            Box(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .border(1.dp, Color(0xFFE5E5E5), RoundedCornerShape(50))
                    .background(Color.White)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            ) {
                Text("Pay in store", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Ink)
            }
        }
        Text(
            "${order.date} · Pickup code ${order.pickup}",
            fontSize = 12.sp,
            color = Muted,
            modifier = Modifier.padding(top = 4.dp),
        )

        Column(Modifier.fillMaxWidth().padding(top = 10.dp)) {
            order.items.forEach { item ->
                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${item.name} · ${item.size} × ${item.qty}", fontSize = 14.sp, color = Soft)
                    Text(gbp(item.price * item.qty), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink)
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("To pay in store", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink)
            Text(gbp(order.total), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Ink)
        }
    }
}
