package com.example.hypernotice

import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.roundToInt

val C_Bg = Color(0xFFF2F2F5)
val C_Accent = Color(0xFF3482FF)
val C_Text = Color(0xCC000000)
val C_Sub = Color(0x99000000)
val C_White = Color(0xFFFFFFFF)
val C_Track = Color(0xFFE5E5EA)

@Composable
fun MiuiSwitch(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?) {
    val s = remember { MutableInteractionSource() }
    val p by s.collectIsPressedAsState()
    val ts by animateFloatAsState(if (p) 1.127f else 1f, spring(0.6f, 987f))
    Box(
        Modifier.size(49.dp, 28.dp).clip(CircleShape)
            .drawBehind { drawRect(if (checked) C_Accent else C_Track) }
            .clickable(remember { MutableInteractionSource() }, null) { onCheckedChange?.invoke(!checked) },
        if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(Modifier.padding(4.dp).size(20.dp).graphicsLayer { scaleX = ts; scaleY = ts }
            .drawBehind { drawCircle(C_White) })
    }
}

@Composable
fun MiuiSlider(v: Float, onV: (Float) -> Unit, vr: ClosedFloatingPointRange<Float> = 0f..1f) {
    val view = LocalView.current
    val s = remember { MutableInteractionSource() }
    val p by s.collectIsPressedAsState()
    var d by remember { mutableStateOf(false) }
    var lw by remember { mutableIntStateOf(1) }; var lh by remember { mutableIntStateOf(28) }
    var lastEdge by remember { mutableIntStateOf(-1) }

    val ts by animateFloatAsState(if (p || d) 1.127f else 1f, spring(0.6f, 987f))
    val av by animateFloatAsState(v.coerceIn(vr), if (d) spring(0.9f, 1755f) else spring(0.96f, 322f))

    Box(
        Modifier.fillMaxWidth().height(40.dp).wrapContentHeight(Alignment.CenterVertically).padding(vertical = 4.dp)
            .onSizeChanged { lw = it.width; lh = it.height }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { d = true; lastEdge = -1 },
                    onDragEnd = { d = false }, onDragCancel = { d = false },
                    onDrag = { ch, _ ->
                        ch.consume()
                        val tr = lh.toFloat() / 2f
                        val avl = (lw - 2f * tr).coerceAtLeast(1f)
                        val fr = ((ch.position.x - tr) / avl).coerceIn(0f, 1f)
                        val nv = (vr.start + fr * (vr.endInclusive - vr.start)).coerceIn(vr)
                        onV(nv)

                        // 滑到最左或最右时振动
                        val edge = when {
                            fr <= 0.001f -> 0
                            fr >= 0.999f -> 1
                            else -> -1
                        }
                        if (edge >= 0 && edge != lastEdge) {
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            lastEdge = edge
                        }
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
fun SwitchItem(title: String, summary: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    var lc by remember { mutableStateOf(checked) }
    Row(
        Modifier.fillMaxWidth()
            .clickable(remember { MutableInteractionSource() }, null) { lc = !lc; onCheckedChange(lc) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = C_Text, fontSize = 16.sp)
            if (summary.isNotEmpty()) { Spacer(Modifier.height(2.dp)); Text(summary, color = C_Sub, fontSize = 12.sp) }
        }
        MiuiSwitch(lc) { lc = it; onCheckedChange(it) }
    }
}

@Composable
fun MiuiArrowItem(title: String, summary: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .clickable(remember { MutableInteractionSource() }, null) { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = C_Text, fontSize = 16.sp)
            if (summary.isNotEmpty()) { Spacer(Modifier.height(2.dp)); Text(summary, color = C_Sub, fontSize = 12.sp) }
        }
        Text("›", color = Color(0xFF8E8E93), fontSize = 24.sp, fontWeight = FontWeight.Light)
    }
}

@Composable
fun SliderPage(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(C_Bg).systemBarsPadding().verticalScroll(rememberScrollState())) {
        Row(
            Modifier.fillMaxWidth()
                .clickable(remember { MutableInteractionSource() }, null) { onBack() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) { Text("‹  返回", color = C_Accent, fontSize = 17.sp) }
        Spacer(Modifier.height(8.dp))
        Text("位置调整", color = C_Sub, fontSize = 13.sp, modifier = Modifier.padding(start = 16.dp, bottom = 4.dp))
        MiuiCard {
            var y by remember { mutableFloatStateOf(45f) }
            Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("Y 轴偏移", color = C_Text, fontSize = 15.sp)
                    Text("${y.roundToInt()}dp", color = C_Sub, fontSize = 13.sp)
                }
                Spacer(Modifier.height(8.dp))
                MiuiSlider(y, { y = it }, 0f..200f)
            }
            var d by remember { mutableFloatStateOf(300f) }
            Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("动画时长", color = C_Text, fontSize = 15.sp)
                    Text("${d.roundToInt()}ms", color = C_Sub, fontSize = 13.sp)
                }
                Spacer(Modifier.height(8.dp))
                MiuiSlider(d, { d = it }, 0f..2000f)
            }
            var r by remember { mutableFloatStateOf(16f) }
            Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("圆角大小", color = C_Text, fontSize = 15.sp)
                    Text("${r.roundToInt()}dp", color = C_Sub, fontSize = 13.sp)
                }
                Spacer(Modifier.height(8.dp))
                MiuiSlider(r, { r = it }, 0f..50f)
            }
            var a by remember { mutableFloatStateOf(95f) }
            Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("透明度", color = C_Text, fontSize = 15.sp)
                    Text("${a.roundToInt()}%", color = C_Sub, fontSize = 13.sp)
                }
                Spacer(Modifier.height(8.dp))
                MiuiSlider(a, { a = it }, 0f..100f)
            }
        }
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
fun HyperNoticeApp() {
    var sh by remember { mutableStateOf(false) }
    val ctx = LocalContext.current
    if (sh) { SliderPage { sh = false }; return }
    Column(Modifier.fillMaxSize().background(C_Bg).systemBarsPadding().verticalScroll(rememberScrollState())) {
        Spacer(Modifier.height(16.dp))
        Text("功能开关", color = C_Sub, fontSize = 13.sp, modifier = Modifier.padding(start = 16.dp, bottom = 4.dp))
        MiuiCard {
            SwitchItem("开启焦点通知上移", "将通知位置向上移动", true) { }
            SwitchItem("开启纯黑背景", "使用纯黑作为通知背景", false) { }
            SwitchItem("显示应用名称", "显示来源应用名称", true) { }
        }
        Spacer(Modifier.height(24.dp))
        Text("位置调整", color = C_Sub, fontSize = 13.sp, modifier = Modifier.padding(start = 16.dp, bottom = 4.dp))
        MiuiCard { MiuiArrowItem("位置与外观", "Y轴偏移 | 动画时长 | 圆角 | 透明度") { sh = true } }
        Spacer(Modifier.height(24.dp))
        Text("关于", color = C_Sub, fontSize = 13.sp, modifier = Modifier.padding(start = 16.dp, bottom = 4.dp))
        MiuiCard {
            MiuiArrowItem("HyperNotice", "v1.0.0") { Toast.makeText(ctx, "HyperNotice v1.0.0", Toast.LENGTH_SHORT).show() }
            MiuiArrowItem("GitHub", "q02144235/HyperNotice") { Toast.makeText(ctx, "GitHub: q02144235/HyperNotice", Toast.LENGTH_SHORT).show() }
        }
        Spacer(Modifier.height(80.dp))
    }
}
