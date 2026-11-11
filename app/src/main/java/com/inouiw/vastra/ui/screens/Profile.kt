package com.inouiw.vastra.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inouiw.vastra.data.AppStore
import com.inouiw.vastra.data.Route
import com.inouiw.vastra.ui.BottomNav
import com.inouiw.vastra.ui.Photo
import com.inouiw.vastra.ui.Tab
import com.inouiw.vastra.ui.VIcons
import com.inouiw.vastra.ui.theme.Ink
import com.inouiw.vastra.ui.theme.Muted
import com.inouiw.vastra.ui.theme.Paper
import com.inouiw.vastra.ui.theme.Stone

@Composable
fun ProfileScreen(store: AppStore) {
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) store.importPhoto(context.contentResolver, uri)
    }
    val pick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }

    Box(Modifier.fillMaxSize().background(Paper)) {
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            Text(
                "Profile",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 22.dp),
            )

            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(bottom = 120.dp)) {
                Row(Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Avatar: saved photo, or initials on black
                    Box(
                        Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Ink)
                            .clickable(onClick = pick),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (store.photoPath != null) {
                            Photo(store.photoPath, Modifier.fillMaxSize())
                        } else {
                            Text(store.profile.name.split(" ").filter { it.isNotEmpty() }.take(2).joinToString("") { it.first().uppercase() }, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    Column(Modifier.padding(start = 14.dp)) {
                        Text("Your profile", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink)
                        Text("Mumbai", fontSize = 13.sp, color = Muted)
                    }
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .height(48.dp)
                        .clip(RoundedCornerShape(50))
                        .border(1.dp, Ink, RoundedCornerShape(50))
                        .clickable(onClick = pick),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(VIcons.Camera, null, tint = Ink, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Upload more images", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                }

                Text("Account", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Ink, modifier = Modifier.padding(top = 24.dp))

                Column(Modifier.fillMaxWidth().padding(top = 6.dp)) {
                    AccountRow(VIcons.Bag, "Your orders", "${store.orders.size}") { store.push(Route.Orders) }
                    AccountRow(VIcons.Pin, "Addresses", store.profile.address.ifEmpty { "Not added" }) { store.push(Route.Store) }
                }
            }
        }

        BottomNav(
            current = Tab.Profile,
            bagCount = store.bagCount(),
            onTab = { store.tab(it.route()) },
            modifier = Modifier.align(Alignment.BottomCenter).padding(start = 20.dp, end = 20.dp, bottom = 22.dp),
        )
    }
}

@Composable
private fun AccountRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = Ink, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Ink)
        Spacer(Modifier.weight(1f))
        if (value.isNotEmpty()) Text(value, fontSize = 13.sp, color = Muted)
        Spacer(Modifier.width(8.dp))
        Icon(VIcons.ChevronRight, null, tint = Ink, modifier = Modifier.size(18.dp))
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEDEDED)))
}
