package com.inouiw.vastra.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inouiw.vastra.data.AppStore
import com.inouiw.vastra.data.Route
import com.inouiw.vastra.ui.Photo
import com.inouiw.vastra.ui.VIcons
import com.inouiw.vastra.ui.theme.Ink
import com.inouiw.vastra.ui.theme.Muted
import com.inouiw.vastra.ui.theme.Paper
import com.inouiw.vastra.ui.theme.Soft
import com.inouiw.vastra.ui.theme.Stone

/** Profile setup is a single sheet over a dimmed home screen: one clear picture of you. */
@Composable
fun ProfileSetupScreen(store: AppStore) {
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) store.importPhoto(context.contentResolver, uri)
    }
    val hasPhoto = store.photoPath != null
    val pick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }

    Box(Modifier.fillMaxSize().background(Paper)) {
        // Dimmed backdrop
        Box(Modifier.fillMaxSize().background(Color(0x73000000)))

        Column(
            Modifier
                .align(Alignment.Center)
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .height(560.dp)
                .background(Color.White, RoundedCornerShape(28.dp))
                .padding(start = 18.dp, top = 20.dp, end = 18.dp, bottom = 18.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Box(
                    Modifier
                        .size(32.dp)
                        .background(Color(0xFFF2F2F2), CircleShape)
                        .clickable { store.replace(Route.Login) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(VIcons.Close, "Close", tint = Ink, modifier = Modifier.size(14.dp))
                }
            }
            Text(
                "Your photo",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.01).sp,
                textAlign = TextAlign.Center,
                color = Ink,
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            )

            // Dashed picture box: tap to choose
            Box(
                Modifier
                    .padding(top = 16.dp, bottom = 14.dp)
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFF5F5F5))
                    .clickable { pick() },
                contentAlignment = Alignment.Center,
            ) {
                if (hasPhoto) {
                    Photo(store.photoPath, Modifier.fillMaxSize())
                } else {
                    Canvas(Modifier.fillMaxSize()) {
                        drawRoundRect(
                            color = Color(0xFFD6D6D6),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx()),
                            style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))),
                        )
                    }
                    Text("Tap to add a photo", fontSize = 13.sp, color = Muted)
                }
            }

            Text(
                "Add one clear picture of you. Head to toe. It's how we show clothes on you.",
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = Soft,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 0.dp),
            )

            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Upload image (outlined)
                Row(
                    Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(50))
                        .border(1.5.dp, Ink, RoundedCornerShape(50))
                        .clickable { pick() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(VIcons.Camera, null, tint = Ink, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Upload image", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Ink)
                }
                // Continue (solid, locked until a picture is chosen)
                Row(
                    Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (hasPhoto) Ink else Color(0xFFEFEFEF), RoundedCornerShape(50))
                        .border(1.5.dp, if (hasPhoto) Ink else Color(0xFFEFEFEF), RoundedCornerShape(50))
                        .clickable(enabled = hasPhoto) { store.replace(Route.Home) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text("Continue", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (hasPhoto) Paper else Color(0xFF9A9A9A))
                    Spacer(Modifier.width(8.dp))
                    Icon(VIcons.Arrow, null, tint = if (hasPhoto) Paper else Color(0xFF9A9A9A), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
