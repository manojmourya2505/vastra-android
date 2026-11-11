package com.inouiw.vastra.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.inouiw.vastra.data.AppStore
import com.inouiw.vastra.data.Route
import com.inouiw.vastra.ui.VIcons
import com.inouiw.vastra.ui.theme.Ink
import com.inouiw.vastra.ui.theme.Line
import com.inouiw.vastra.ui.theme.Muted
import com.inouiw.vastra.ui.theme.Paper
import com.inouiw.vastra.ui.theme.Placeholder
import com.inouiw.vastra.ui.theme.Soft
import com.inouiw.vastra.ui.theme.Stone
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val serif = FontFamily.Serif
private val fieldShape = RoundedCornerShape(14.dp)

/** The one OTP for the test user. */
private const val TEST_OTP = "123456"

@Composable
fun LoginScreen(store: AppStore) {
    var inCodeStep by remember { mutableStateOf(false) }
    var phone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val focus = remember { FocusRequester() }

    val progress = if (inCodeStep) code.length / 6f else phone.length / 10f
    val ringProgress by animateFloatAsState(progress, tween(400, easing = LinearEasing), label = "ring")
    val canContinue = if (inCodeStep) code.length == 6 else phone.length == 10

    fun submit() {
        if (!canContinue || loading) return
        if (!inCodeStep) {
            inCodeStep = true
            code = ""
        } else if (code != TEST_OTP) {
            code = ""
        } else {
            loading = true
            store.login(phone) { ok ->
                loading = false
                if (ok) store.replace(Route.ProfileSetup)
            }
        }
    }

    LaunchedEffect(inCodeStep) { if (!inCodeStep) focus.requestFocus() }

    Box(Modifier.fillMaxSize().background(Paper)) {
        Column(Modifier.fillMaxSize().padding(start = 26.dp, top = 26.dp, end = 26.dp, bottom = 34.dp)) {
            // .top: brand + SKIP / BACK
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Canvas(Modifier.size(24.dp)) {
                        val s = size.width / 1024f
                        fun tri(a: Offset, b: Offset, c: Offset, col: Color) {
                            val p = androidx.compose.ui.graphics.Path().apply {
                                moveTo(a.x, a.y); lineTo(b.x, b.y); lineTo(c.x, c.y); close()
                            }
                            drawPath(p, col)
                        }
                        tri(Offset(512f * s, 190f * s), Offset(232f * s, 430f * s), Offset(792f * s, 430f * s), Ink)
                        tri(Offset(232f * s, 430f * s), Offset(512f * s, 430f * s), Offset(512f * s, 840f * s), Color(0xFF3A3A3A))
                        tri(Offset(792f * s, 430f * s), Offset(512f * s, 430f * s), Offset(512f * s, 840f * s), Color(0xFF9A9A9A))
                    }
                    Text("VASTRA", fontFamily = serif, fontSize = 19.sp, letterSpacing = 0.18.em, color = Ink)
                }
                Text(
                    if (inCodeStep) "BACK" else "",
                    fontSize = 12.sp,
                    letterSpacing = 0.14.em,
                    color = Muted,
                    modifier = Modifier
                        .clickable(enabled = inCodeStep) {
                            if (code.isNotEmpty()) code = code.dropLast(1) else inCodeStep = false
                        }
                        .padding(vertical = 6.dp),
                )
            }

            // .head
            Column(Modifier.padding(top = 78.dp)) {
                Text(
                    if (inCodeStep) "VERIFY" else "WELCOME BACK",
                    fontSize = 11.sp,
                    letterSpacing = 0.24.em,
                    color = Ink,
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    if (inCodeStep) "Enter the code" else "Sign in with your phone",
                    fontFamily = serif,
                    fontSize = 44.sp,
                    lineHeight = 46.sp,
                    color = Ink,
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    if (inCodeStep) "We sent a 6-digit code to +44 ${phone.take(4)} ${phone.drop(4)}"
                    else "We'll send you a 6-digit code. No password needed.",
                    fontSize = 15.sp,
                    lineHeight = 23.sp,
                    color = Soft,
                )
            }

            if (!inCodeStep) {
                // Step A: country + number
                Row(Modifier.padding(top = 40.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        Modifier
                            .height(46.dp)
                            .width(96.dp)
                            .clip(fieldShape)
                            .background(Color(0xFFF7F7F7))
                            .border(1.dp, Stone, fieldShape),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text("🇬🇧", fontSize = 14.sp)
                        Spacer(Modifier.width(6.dp))
                        Text("+44", fontSize = 14.sp, color = Ink)
                        Spacer(Modifier.width(6.dp))
                        Box(Modifier.size(8.dp, 5.dp).background(Muted, RoundedCornerShape(1.dp)))
                    }
                    BasicTextField(
                        value = phone,
                        onValueChange = { phone = it.filter(Char::isDigit).take(10) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        textStyle = TextStyle(fontSize = 16.sp, color = Ink, letterSpacing = 0.05.em),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(Ink),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .focusRequester(focus)
                            .clip(fieldShape)
                            .background(Color(0xFFF7F7F7))
                            .border(1.dp, if (phone.isNotEmpty()) Ink else Stone, fieldShape)
                            .padding(horizontal = 14.dp),
                        decorationBox = { inner ->
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                                if (phone.isEmpty()) Text("7700 900123", fontSize = 16.sp, color = Placeholder)
                                inner()
                            }
                        },
                    )
                }
            } else {
                // Step B: six boxes, with the hidden field collecting digits
                Box(Modifier.padding(top = 40.dp).fillMaxWidth()) {
                    Row(
                        Modifier.fillMaxWidth().clickable { /* focus handled by hidden field below */ },
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        repeat(6) { k ->
                            val ch = code.getOrNull(k)
                            val active = k == code.length
                            Box(
                                Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .clip(fieldShape)
                                    .background(Color(0xFFF7F7F7))
                                    .border(1.dp, if (active) Ink else Stone, fieldShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(ch?.toString() ?: "", fontSize = 20.sp, color = Ink)
                            }
                        }
                    }
                    BasicTextField(
                        value = code,
                        onValueChange = { code = it.filter(Char::isDigit).take(6) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = TextStyle(fontSize = 1.sp, color = Color.Transparent),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.Transparent),
                        modifier = Modifier.size(1.dp).focusRequester(focus),
                    )
                }
                Text(
                    "By continuing, you agree to Vastra's Privacy and Terms.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = Placeholder,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 18.dp),
                )
                LaunchedEffect(Unit) { focus.requestFocus() }
            }

            Spacer(Modifier.weight(1f))

            // .bottom: ring button left, alt text right
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Box(
                    Modifier
                        .size(76.dp)
                        .clickable(enabled = canContinue && !loading) { submit() },
                    contentAlignment = Alignment.Center,
                ) {
                    Canvas(Modifier.size(76.dp)) {
                        val r = 34.5f * size.width / 76f
                        val c = size.width / 2f
                        drawCircle(Line, radius = r, center = Offset(c, c), style = Stroke(width = 2f))
                        drawArc(
                            color = Ink,
                            startAngle = -90f,
                            sweepAngle = 360f * ringProgress,
                            useCenter = false,
                            topLeft = Offset(c - r, c - r),
                            size = Size(r * 2, r * 2),
                            style = Stroke(width = 2f, cap = StrokeCap.Round),
                        )
                    }
                    val spin = if (loading) {
                        rememberInfiniteTransition(label = "spin").animateFloat(
                            0f, 360f,
                            infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Restart),
                            label = "spinAngle",
                        ).value
                    } else 0f
                    Box(
                        Modifier
                            .size(58.dp)
                            .rotate(spin)
                            .graphicsLayer { alpha = if (canContinue || loading) 1f else 0.35f }
                            .clip(CircleShape)
                            .background(Ink),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(VIcons.Arrow, "Continue", tint = Paper, modifier = Modifier.size(22.dp))
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("JUST BROWSING?", fontSize = 11.sp, letterSpacing = 0.2.em, lineHeight = 20.sp, color = Placeholder)
                    Text(
                        if (inCodeStep) "RESEND CODE" else "CONTINUE AS GUEST",
                        fontSize = 11.sp,
                        letterSpacing = 0.2.em,
                        lineHeight = 20.sp,
                        color = Ink,
                        modifier = Modifier.clickable {
                            if (inCodeStep) code = "" else store.replace(Route.ProfileSetup)
                        },
                    )
                }
            }
        }
    }
}
