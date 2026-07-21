package com.example.hypernotice

import android.app.Activity
import android.app.AlertDialog
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.roundToInt

val C_Bg = Color(0xFFF7F7F7)
val C_Accent = Color(0xFF3482FF)
val C_Text = Color(0xCC000000)
val C_Sub = Color(0xFF8A8FA3)
val C_White = Color(0xFFFFFFFF)
val C_Track = Color(0xFFE5E5EA)

private fun haptic(ctx: android.content.Context) {
    try {
        val vib = if (Build.VERSION.SDK_INT >= 31)
            (ctx.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        else
            ctx.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
        vib?.vibrate(VibrationEffect.createOneShot(10, 50))
    } catch (_: Exception) { }
}

@Composable
fun MiuiSwitch(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?) {
    val s = remember { MutableInteractionSource() }
    val p by s.collectIsPressedAsState()
    val ts by animateFloatAsState(if (p) 1.127f else 1f, spring(0.6f, 987f))
    val ctx = LocalContext.current
    Box(
        Modifier.size(49.dp, 28.dp).clip(CircleShape)
            .drawBehind { drawRect(if (checked) C_Accent else C_Track) }
            .clickable(remember { MutableInteractionSource() }, null) {
                onCheckedChange?.invoke(!checked)
                haptic(ctx)
            },
        if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(Modifier.padding(4.dp).size(20.dp).graphicsLayer { scaleX = ts; scaleY = ts }
            .drawBehind { drawCircle(C_White) })
    }
}

@Composable
fun MiuiSlider(v: Float, onV: (Float) -> Unit, vr: ClosedFloatingPointRange<Float> = 0f..1f) {
    val s = remember { MutableInteractionSource() }
    val p by s.collectIsPressedAsState()
    var d by remember { mutableStateOf(false) }
    var lw by remember { mutableIntStateOf(1) }
    var lh by remember { mutableIntStateOf(28) }
    val ts by animateFloatAsState(if (p || d) 1.127f else 1f, spring(0.6f, 987f))
    val av by animateFloatAsState(v.coerceIn(vr), if (d) spring(0.9f, 1755f) else spring(0.96f, 322f))
    val ctx = LocalContext.current
    var lastFr by remember { mutableFloatStateOf(-1f) }
    Box(
        Modifier.fillMaxWidth().height(40.dp).wrapContentHeight(Alignment.CenterVertically).padding(vertical = 4.dp)
            .onSizeChanged { lw = it.width; lh = it.height }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { d = true },
                    onDragEnd = { d = false },
                    onDragCancel = { d = false },
                    onDrag = { ch, _ ->
                        ch.consume()
                        val tr = lh.toFloat() / 2f
                        val avl = (lw - 2f * tr).coerceAtLeast(1f)
                        val fr = ((ch.position.x - tr) / avl).coerceIn(0f, 1f)
                        val nv = (vr.start + fr * (vr.endInclusive - vr.start)).coerceIn(vr)
                        if (fr <= 0.01f || fr >= 0.99f)
                            if (abs(fr - lastFr) > 0.005f) haptic(ctx)
                        lastFr = fr
                        onV(nv)
                    }
                )
            }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val h = size.height; val w = size.width; val r = h / 2f; val my = h / 2f
            val fr = ((av - vr.start) / (vr.endInclusive - vr.start)).coerceIn(0f, 1f)
            val mx = r + fr * (w - 2f * r)
            drawLine(C_Track, Offset(r, my), Offset(w - r, my), h, cap = StrokeCap.Round)
            drawLine(C_Accent, Offset(r, my), Offset(mx, my), h, cap = StrokeCap.Round)
            drawCircle(C_White, r * 0.72f * ts, Offset(mx, my))
        }
    }
}

@Composable
fun MiuiCard(c: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
    ) { Column(content = c) }
}

@Composable
fun SwitchItem(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    var lc by remember { mutableStateOf(checked) }
    val ctx = LocalContext.current
    Row(
        Modifier.fillMaxWidth().clickable(remember { MutableInteractionSource() }, null) { lc = !lc; onCheckedChange(lc); haptic(ctx) }
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = C_Text, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        MiuiSwitch(lc) { lc = it; onCheckedChange(it) }
    }
}

@Composable
fun MiuiArrowItem(title: String, onClick: () -> Unit) {
    val ctx = LocalContext.current
    Row(
        Modifier.fillMaxWidth().clickable(remember { MutableInteractionSource() }, null) { onClick(); haptic(ctx) }
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = C_Text, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text("鈥?, color = Color(0xFF8E8E93), fontSize = 26.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MiuiArrowItemWithSummary(title: String, summary: String, onClick: () -> Unit) {
    val ctx = LocalContext.current
    Row(
        Modifier.fillMaxWidth().clickable(remember { MutableInteractionSource() }, null) { onClick(); haptic(ctx) }
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = C_Text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(3.dp))
            Text(summary, color = C_Sub, fontSize = 13.sp)
        }
        Text("鈥?, color = Color(0xFF8E8E93), fontSize = 26.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SliderPage(onBack: () -> Unit) {
    val ctx = LocalContext.current
    var showRestartDialog by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var dragging by remember { mutableStateOf(false) }
    var pageWidth by remember { mutableIntStateOf(1) }

    if (showRestartDialog) {
        AlertDialog.Builder(ctx)
            .setTitle("閲嶅惎绯荤粺鐣岄潰")
            .setMessage("閲嶅惎 SystemUI 浣?Hook 鐢熸晥锛?)
            .setPositiveButton("閲嶅惎") { _, _ ->
                Toast.makeText(ctx, "姝ｅ湪閲嶅惎绯荤粺鐣岄潰鈥?, Toast.LENGTH_SHORT).show()
                try { Runtime.getRuntime().exec(arrayOf("am", "force-stop", "com.android.systemui")) }
                catch (_: Exception) { }
            }
            .setNegativeButton("鍙栨秷", null)
            .show()
    }

    Box(
        Modifier.fillMaxSize()
            .onSizeChanged { pageWidth = it.width }
            .graphicsLayer { translationX = if (dragging) dragOffset else 0f }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { dragging = true },
                    onHorizontalDrag = { change, delta ->
                        change.consume()
                        dragOffset = (dragOffset + delta).coerceAtLeast(0f)
                    },
                    onDragEnd = {
                        dragging = false
                        if (abs(dragOffset) > pageWidth * 0.25f) onBack()
                        dragOffset = 0f
                    },
                    onDragCancel = { dragging = false; dragOffset = 0f }
                )
            }
    ) {
        Column(Modifier.fillMaxSize().background(C_Bg).systemBarsPadding().verticalScroll(rememberScrollState())) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("鈫?, color = Color.Black, fontSize = 24.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(remember { MutableInteractionSource() }, null) { onBack() })
                Spacer(Modifier.weight(1f))
                Text("鈫?, color = Color(0xFF8E8E93), fontSize = 22.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(remember { MutableInteractionSource() }, null) { showRestartDialog = true })
            }
            Spacer(Modifier.height(8.dp))
            Text("浣嶇疆璋冩暣", color = C_Sub, fontSize = 13.sp, modifier = Modifier.padding(start = 16.dp, bottom = 4.dp))
            MiuiCard {
                var y by remember { mutableFloatStateOf(45f) }
                Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Y 杞村亸绉?, color = C_Text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("${y.roundToInt()}", color = C_Sub, fontSize = 14.sp)
                    }
                    Spacer(Modifier.height(10.dp)); MiuiSlider(y, { y = it }, 0f..200f)
                }
                var d by remember { mutableFloatStateOf(300f) }
                Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 20.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("鍔ㄧ敾鏃堕暱", color = C_Text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("${d.roundToInt()}", color = C_Sub, fontSize = 14.sp)
                    }
                    Spacer(Modifier.height(10.dp)); MiuiSlider(d, { d = it }, 0f..2000f)
                }
                var t by remember { mutableFloatStateOf(16f) }
                Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 20.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("鍦嗚澶у皬", color = C_Text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("${t.roundToInt()}", color = C_Sub, fontSize = 14.sp)
                    }
                    Spacer(Modifier.height(10.dp)); MiuiSlider(t, { t = it }, 0f..50f)
                }
                var o by remember { mutableFloatStateOf(95f) }
                Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 20.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("閫忔槑搴?, color = C_Text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("${o.roundToInt()}", color = C_Sub, fontSize = 14.sp)
                    }
                    Spacer(Modifier.height(10.dp)); MiuiSlider(o, { o = it }, 0f..100f)
                }
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun HyperNoticeApp() {
    var sh by remember { mutableStateOf(false) }
    val ctx = LocalContext.current
    SideEffect {
        val act = ctx as? Activity
        act?.window?.statusBarColor = Color(0xFFF7F7F7).hashCode()
        act?.window?.decorView?.let { v -> v.systemUiVisibility = v.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR }
    }
    AnimatedContent(
        targetState = sh,
        transitionSpec = {
            if (targetState) (slideInHorizontally { it } + fadeIn(tween(250))) togetherWith (slideOutHorizontally { -it } + fadeOut(tween(250)))
            else (slideInHorizontally { -it } + fadeIn(tween(250))) togetherWith (slideOutHorizontally { it } + fadeOut(tween(250)))
        },
        label = "page"
    ) { target ->
        if (target) SliderPage { sh = false }
        else {
            Column(Modifier.fillMaxSize().background(C_Bg).systemBarsPadding().verticalScroll(rememberScrollState())) {
                Box(Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 8.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(painterResource(R.drawable.ic_launcher), null, Modifier.size(72.dp).clip(RoundedCornerShape(16.dp)), ContentScale.Crop)
                        Spacer(Modifier.height(10.dp))
                        Text("HyperNotice", color = C_Text, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("鍔熻兘寮€鍏?, color = C_Sub, fontSize = 13.sp, modifier = Modifier.padding(start = 16.dp, bottom = 4.dp))
                MiuiCard {
                    SwitchItem("寮€鍚劍鐐归€氱煡涓婄Щ", true) { }
                    SwitchItem("寮€鍚函榛戣儗鏅?, false) { }
                }
                Spacer(Modifier.height(24.dp))
                Text("浣嶇疆璋冩暣", color = C_Sub, fontSize = 13.sp, modifier = Modifier.padding(start = 16.dp, bottom = 4.dp))
                MiuiCard { MiuiArrowItem("浣嶇疆涓庡瑙?) { sh = true } }
                Spacer(Modifier.height(24.dp))
                Text("鍏充簬", color = C_Sub, fontSize = 13.sp, modifier = Modifier.padding(start = 16.dp, bottom = 4.dp))
                MiuiCard {
                    MiuiArrowItemWithSummary("HyperNotice", "v1.0.0") { Toast.makeText(ctx, "HyperNotice v1.0.0", Toast.LENGTH_SHORT).show() }
                    MiuiArrowItemWithSummary("GitHub", "q02144235/HyperNotice") { Toast.makeText(ctx, "GitHub: q02144235/HyperNotice", Toast.LENGTH_SHORT).show() }
                }
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

