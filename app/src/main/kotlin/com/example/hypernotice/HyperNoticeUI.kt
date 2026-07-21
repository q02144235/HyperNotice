package com.example.hypernotice

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.SharedPreferences
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import kotlin.math.roundToInt

val C_Bg = Color(0xFFF7F7F7)
val C_Accent = Color(0xFF3482FF)
val C_Text = Color(0xCC000000)
val C_Sub = Color(0xFF8A8FA3)
val C_White = Color(0xFFFFFFFF)
val C_Track = Color(0xFFE5E5EA)

const val PREFS_NAME = "com.example.hypernotice_preferences"

private fun haptic(ctx: android.content.Context) {
    try {
        (ctx as? Activity)?.window?.decorView?.performHapticFeedback(
            android.view.HapticFeedbackConstants.GESTURE_THRESHOLD_ACTIVATE
        )
    } catch (_: Exception) { }
}

private fun getPrefs(ctx: android.content.Context): SharedPreferences {
    return ctx.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
}

@Composable
fun MiuiSwitch(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?) {
    val s = remember { MutableInteractionSource() }
    val p by s.collectIsPressedAsState()
    val ts by animateFloatAsState(if (p) 1.127f else 1f, tween(800), label = "")
    val sx by animateFloatAsState(if (checked) 21f else 0f, tween(300), label = "")
    Box(
        Modifier
            .size(49.dp, 28.dp)
            .clip(CircleShape)
            .drawBehind { drawRect(if (checked) C_Accent else C_Track) }
            .clickable(remember { MutableInteractionSource() }, null) { onCheckedChange?.invoke(!checked) }
    ) {
        Box(
            Modifier
                .padding(4.dp)
                .size(20.dp)
                .offset(x = sx.dp)
                .graphicsLayer { scaleX = ts; scaleY = ts }
                .drawBehind { drawCircle(C_White) }
        )
    }
}

@Composable
fun MiuiSlider(v: Float, onV: (Float) -> Unit, vr: ClosedFloatingPointRange<Float> = 0f..1f) {
    val s = remember { MutableInteractionSource() }
    val p by s.collectIsPressedAsState()
    var d by remember { mutableStateOf(false) }
    var lw by remember { mutableIntStateOf(1) }
    var lh by remember { mutableIntStateOf(28) }
    val ts by animateFloatAsState(if (p || d) 1.127f else 1f, spring(0.6f, 987f), label = "")
    val av by animateFloatAsState(v.coerceIn(vr), if (d) spring(0.9f, 1755f) else spring(0.96f, 322f), label = "")
    val ctx = LocalContext.current
    var hapticed by remember { mutableStateOf(false) }
    Box(
        Modifier
            .fillMaxWidth()
            .height(40.dp)
            .wrapContentHeight(Alignment.CenterVertically)
            .padding(vertical = 4.dp)
            .onSizeChanged { lw = it.width; lh = it.height }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { d = true; hapticed = false },
                    onDragEnd = { d = false },
                    onDragCancel = { d = false; hapticed = false },
                    onDrag = { change, _ ->
                        change.consume()
                        val tr = lh.toFloat() / 2f
                        val avl = (lw - 2f * tr).coerceAtLeast(1f)
                        val fr = ((change.position.x - tr) / avl).coerceIn(0f, 1f)
                        if (fr <= 0.01f || fr >= 0.99f) {
                            if (!hapticed) { haptic(ctx); hapticed = true }
                        } else {
                            hapticed = false
                        }
                        onV((vr.start + fr * (vr.endInclusive - vr.start)).coerceIn(vr))
                    }
                )
            }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val h = size.height
            val w = size.width
            val r = h / 2f
            val my = h / 2f
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
    ) {
        Column(content = c)
    }
}

@Composable
fun SwitchItem(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    var lc by remember { mutableStateOf(checked) }
    val ctx = LocalContext.current
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(remember { MutableInteractionSource() }, null) { lc = !lc; onCheckedChange(lc) }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = C_Text, fontSize = 17.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        MiuiSwitch(lc) {
            lc = it
            onCheckedChange(it)
            haptic(ctx)
        }
    }
}

@Composable
fun MiuiArrowItem(title: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(remember { MutableInteractionSource() }, null) { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = C_Text, fontSize = 17.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Text("\u203A", color = Color(0xFF8E8E93), fontSize = 26.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MiuiArrowItemWithSummary(title: String, summary: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(remember { MutableInteractionSource() }, null) { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = C_Text, fontSize = 17.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(3.dp))
            Text(summary, color = C_Sub, fontSize = 12.sp)
        }
        Text("\u203A", color = Color(0xFF8E8E93), fontSize = 26.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SliderPage(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val prefs = getPrefs(ctx)
    var showRestartDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    if (showRestartDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showRestartDialog = false },
            title = { Text("重启系统界面") },
            text = { Text("更改位置和外观参数后需要重启系统界面才会生效，确定要重启吗？") },
            confirmButton = {
                TextButton(onClick = {
                    showRestartDialog = false
                    Toast.makeText(ctx, "正在重启系统界面…", Toast.LENGTH_SHORT).show()
                    try {
                        Runtime.getRuntime().exec(arrayOf("su", "-c", "am force-stop com.android.systemui"))
                    } catch (_: Exception) {
                        try {
                            Runtime.getRuntime().exec(arrayOf("am", "force-stop", "com.android.systemui"))
                        } catch (_: Exception) {
                            Toast.makeText(ctx, "重启失败，请手动重启系统界面", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) { Text("重启") }
            },
            dismissButton = {
                TextButton(onClick = { showRestartDialog = false }) { Text("取消") }
            }
        )
    }

    var swipeOffset by remember { mutableFloatStateOf(0f) }
    var swiping by remember { mutableStateOf(false) }
    val swipeAnim by animateFloatAsState(
        targetValue = if (swiping) swipeOffset else 0f,
        animationSpec = if (swiping) snap() else tween(250),
        label = "swipe"
    )

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .offset(x = swipeAnim.dp)
                .background(C_Bg)
                .systemBarsPadding()
                .verticalScroll(scrollState)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { swiping = true },
                        onDragEnd = {
                            if (swipeOffset > 150f) {
                                onBack()
                            }
                            swiping = false
                            swipeOffset = 0f
                        },
                        onDragCancel = {
                            swiping = false
                            swipeOffset = 0f
                        },
                        onHorizontalDrag = { _, drag ->
                            swipeOffset = (swipeOffset + drag).coerceAtLeast(0f)
                        }
                    )
                }
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("\u2190", color = Color.Black, fontSize = 24.sp, fontWeight = FontWeight.Normal,
                    modifier = Modifier.clickable(remember { MutableInteractionSource() }, null) { onBack() })
                Spacer(Modifier.weight(1f))
                Text("\u21BB", color = Color(0xFF8E8E93), fontSize = 24.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(remember { MutableInteractionSource() }, null) { showRestartDialog = true })
            }
            Text("位置与外观", color = Color.Black, fontSize = 32.sp, fontWeight = FontWeight.Medium,
                modifier = Modifier.offset(x = 30.dp).padding(bottom = 4.dp))
            Text("位置调整", color = C_Sub, fontSize = 13.sp,
                modifier = Modifier.offset(x = 30.dp).padding(bottom = 2.dp))
            MiuiCard {
                var y by remember { mutableFloatStateOf(prefs.getFloat("y_offset", 45f)) }
                Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 12.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Y 轴偏移", color = C_Text, fontSize = 17.sp, fontWeight = FontWeight.Medium)
                        Text("${y.roundToInt()}", color = C_Sub, fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                    MiuiSlider(y, { y = it; prefs.edit().putFloat("y_offset", it).apply() }, 0f..200f)
                }
                var d by remember { mutableFloatStateOf(prefs.getFloat("anim_duration", 300f)) }
                Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("动画时长", color = C_Text, fontSize = 17.sp, fontWeight = FontWeight.Medium)
                        Text("${d.roundToInt()}", color = C_Sub, fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                    MiuiSlider(d, { d = it; prefs.edit().putFloat("anim_duration", it).apply() }, 0f..2000f)
                }
                var t by remember { mutableFloatStateOf(prefs.getFloat("corner_radius", 16f)) }
                Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("圆角大小", color = C_Text, fontSize = 17.sp, fontWeight = FontWeight.Medium)
                        Text("${t.roundToInt()}", color = C_Sub, fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                    MiuiSlider(t, { t = it; prefs.edit().putFloat("corner_radius", it).apply() }, 0f..50f)
                }
                var o by remember { mutableFloatStateOf(prefs.getFloat("opacity", 95f)) }
                Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 20.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("透明度", color = C_Text, fontSize = 17.sp, fontWeight = FontWeight.Medium)
                        Text("${o.roundToInt()}", color = C_Sub, fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                    MiuiSlider(o, { o = it; prefs.edit().putFloat("opacity", it).apply() }, 0f..100f)
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
    val prefs = getPrefs(ctx)
    SideEffect {
        val act = ctx as? Activity
        act?.window?.statusBarColor = C_Bg.hashCode()
        act?.window?.decorView?.let { v ->
            v.systemUiVisibility = v.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = !sh,
            enter = fadeIn(tween(300)) + slideInHorizontally(tween(300)) { -it },
            exit = fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { -it }
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .background(C_Bg)
                    .systemBarsPadding()
                    .verticalScroll(rememberScrollState())
            ) {
                Box(Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 8.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val bmp = remember { mutableStateOf<android.graphics.Bitmap?>(null) }
                        LaunchedEffect(Unit) {
                            bmp.value = try {
                                android.graphics.BitmapFactory.decodeResource(ctx.resources, R.drawable.qwq)
                            } catch (_: Exception) { null }
                        }
                        bmp.value?.let { b ->
                            Image(b.asImageBitmap(), null,
                                Modifier.size(72.dp).clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop)
                        }
                        Spacer(Modifier.height(10.dp))
                        Text("HyperNotice", color = C_Text, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("功能开关", color = C_Sub, fontSize = 13.sp,
                    modifier = Modifier.offset(x = 30.dp).padding(bottom = 4.dp))
                MiuiCard {
                    // 按用户要求重新排序
                    var replaceText by remember { mutableStateOf(prefs.getBoolean("replace_text", false)) }
                    SwitchItem("将通知替换为通知通知", replaceText) {
                        replaceText = it; prefs.edit().putBoolean("replace_text", it).apply()
                    }
                    var hideStatus by remember { mutableStateOf(prefs.getBoolean("hide_status_bar", false)) }
                    SwitchItem("焦点通知时隐藏状态栏", hideStatus) {
                        hideStatus = it; prefs.edit().putBoolean("hide_status_bar", it).apply()
                    }
                    var moveUp by remember { mutableStateOf(prefs.getBoolean("move_up_punchhole", false)) }
                    SwitchItem("焦点通知上移融入前摄", moveUp) {
                        moveUp = it; prefs.edit().putBoolean("move_up_punchhole", it).apply()
                    }
                    var darkBg by remember { mutableStateOf(prefs.getBoolean("dark_bg", false)) }
                    SwitchItem("焦点通知背景改为纯黑", darkBg) {
                        darkBg = it; prefs.edit().putBoolean("dark_bg", it).apply()
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text("位置调整", color = C_Sub, fontSize = 13.sp,
                    modifier = Modifier.offset(x = 30.dp).padding(bottom = 4.dp))
                MiuiCard {
                    MiuiArrowItem("位置与外观") { sh = true }
                }
                Spacer(Modifier.height(24.dp))
                Text("关于", color = C_Sub, fontSize = 13.sp,
                    modifier = Modifier.offset(x = 30.dp).padding(bottom = 4.dp))
                MiuiCard {
                    MiuiArrowItemWithSummary("HyperNotice", "v1.0.0") {
                        Toast.makeText(ctx, "HyperNotice v1.0.0", Toast.LENGTH_SHORT).show()
                    }
                    MiuiArrowItemWithSummary("GitHub", "q02144235/HyperNotice") {
                        try { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/q02144235/HyperNotice"))) } catch (_: Exception) { Toast.makeText(ctx, "无法打开浏览器", Toast.LENGTH_SHORT).show() }
                    }
                }
                Spacer(Modifier.height(80.dp))
            }
        }

        AnimatedVisibility(
            visible = sh,
            enter = slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)),
            exit = fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { it / 4 }
        ) {
            SliderPage { sh = false }
        }
    }
}
