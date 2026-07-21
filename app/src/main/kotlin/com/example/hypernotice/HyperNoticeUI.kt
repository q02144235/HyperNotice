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
import kotlin.math.roundToInt

val C_Bg = Color(0xFFFFFFFF)
val C_Accent = Color(0xFF3482FF)
val C_Text = Color(0xCC000000)
val C_Sub = Color(0x99000000)
val C_White = Color(0xFFFFFFFF)
val C_Track = Color(0xFFE5E5EA)

@Composable
fun MiuiSwitch(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val thumbScale by animateFloatAsState(
        targetValue = if (isPressed) 1.127f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 987f)
    )
    Box(
        modifier = Modifier.size(49.dp, 28.dp).clip(CircleShape)
            .drawBehind { drawRect(if (checked) C_Accent else C_Track) }
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onCheckedChange?.invoke(!checked) },
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier.padding(4.dp).size(20.dp)
                .graphicsLayer { scaleX = thumbScale; scaleY = thumbScale }
                .drawBehind { drawCircle(color = C_White) }
        )
    }
}

@Composable
fun MiuiSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f
) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var isDragging by remember { mutableStateOf(false) }
    var layoutWidth by remember { mutableIntStateOf(1) }
    var layoutHeight by remember { mutableIntStateOf(28) }

    val thumbScale by animateFloatAsState(
        targetValue = if (isPressed || isDragging) 1.127f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 987f)
    )
    val animatedValue by animateFloatAsState(
        targetValue = value.coerceIn(valueRange),
        animationSpec = if (isDragging) spring(0.9f, 1755f) else spring(0.96f, 322f)
    )

    Box(
        modifier = Modifier.fillMaxWidth().height(40.dp).wrapContentHeight(Alignment.CenterVertically)
            .padding(vertical = 4.dp)
            .onSizeChanged { layoutWidth = it.width; layoutHeight = it.height }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        isDragging = true
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    },
                    onDragEnd = { isDragging = false },
                    onDragCancel = { isDragging = false },
                    onDrag = { change, _ ->
                        change.consume()
                        val tr = layoutHeight.toFloat() / 2f
                        val avail = (layoutWidth - 2f * tr).coerceAtLeast(1f)
                        val fraction = ((change.position.x - tr) / avail).coerceIn(0f, 1f)
                        val newVal = valueRange.start + fraction * (valueRange.endInclusive - valueRange.start)
                        onValueChange(newVal.coerceIn(valueRange))
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val h = size.height; val w = size.width
            val r = h / 2f; val midY = h / 2f
            val frac = ((animatedValue - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)
            val midX = r + frac * (w - 2f * r)
            drawLine(C_Track, Offset(0f, midY), Offset(w, midY), h, cap = StrokeCap.Round)
            drawLine(C_Accent, Offset(0f, midY), Offset(midX, midY), h, cap = StrokeCap.Round)
            drawCircle(C_White, r * 0.72f * thumbScale, Offset(midX, midY))
        }
    }
}

@Composable
fun MiuiCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
    ) { Column(content = content) }
}

@Composable
fun HapticSwitchItem(title: String, summary: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val view = LocalView.current
    var lc by remember { mutableStateOf(checked) }
    Row(
        Modifier.fillMaxWidth()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                lc = !lc; onCheckedChange(lc)
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = C_Text, fontSize = 16.sp)
            if (summary.isNotEmpty()) { Spacer(Modifier.height(2.dp)); Text(summary, color = C_Sub, fontSize = 12.sp) }
        }
        MiuiSwitch(lc) { view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS); lc = it; onCheckedChange(it) }
    }
}

@Composable
fun MiuiArrowItem(title: String, summary: String, onClick: () -> Unit) {
    val view = LocalView.current
    Row(
        Modifier.fillMaxWidth()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                onClick()
            }
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
    val view = LocalView.current
    Column(Modifier.fillMaxSize().background(C_Bg).verticalScroll(rememberScrollState())) {
        Row(
            Modifier.fillMaxWidth()
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS); onBack()
                }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) { Text("‹  返回", color = C_Accent, fontSize = 17.sp) }
        Spacer(Modifier.height(8.dp))
        Text("位置调整", color = C_Sub, fontSize = 13.sp, modifier = Modifier.padding(start = 16.dp, bottom = 4.dp))
        MiuiCard {
            var y by remember { mutableFloatStateOf(45f) }
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("Y 轴偏移", fontSize = 15.sp, color = C_Text)
                    Text("${y.roundToInt()}dp", fontSize = 13.sp, color = C_Sub)
                }
                Spacer(Modifier.height(12.dp))
                MiuiSlider(y, { y = it }, 0f..200f)
            }
            var d by remember { mutableFloatStateOf(300f) }
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("动画时长", fontSize = 15.sp, color = C_Text)
                    Text("${d.roundToInt()}ms", fontSize = 13.sp, color = C_Sub)
                }
                Spacer(Modifier.height(12.dp))
                MiuiSlider(d, { d = it }, 0f..2000f)
            }
            var r by remember { mutableFloatStateOf(16f) }
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("圆角大小", fontSize = 15.sp, color = C_Text)
                    Text("${r.roundToInt()}dp", fontSize = 13.sp, color = C_Sub)
                }
                Spacer(Modifier.height(12.dp))
                MiuiSlider(r, { r = it }, 0f..50f)
            }
            var a by remember { mutableFloatStateOf(95f) }
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("透明度", fontSize = 15.sp, color = C_Text)
                    Text("${a.roundToInt()}%", fontSize = 13.sp, color = C_Sub)
                }
                Spacer(Modifier.height(12.dp))
                MiuiSlider(a, { a = it }, 0f..100f)
            }
        }
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
fun HyperNoticeApp() {
    var show by remember { mutableStateOf(false) }
    val ctx = LocalContext.current
    if (show) { SliderPage { show = false }; return }
    Column(Modifier.fillMaxSize().background(C_Bg).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.height(16.dp))
        Text("功能开关", fontSize = 13.sp, color = C_Sub, modifier = Modifier.padding(start = 16.dp, bottom = 4.dp))
        MiuiCard {
            HapticSwitchItem("开启焦点通知上移", "将通知位置向上移动", true) { }
            HapticSwitchItem("开启纯黑背景", "使用纯黑作为通知背景", false) { }
            HapticSwitchItem("显示应用名称", "显示来源应用名称", true) { }
        }
        Spacer(Modifier.height(24.dp))
        Text("位置调整", fontSize = 13.sp, color = C_Sub, modifier = Modifier.padding(start = 16.dp, bottom = 4.dp))
        MiuiCard { MiuiArrowItem("位置与外观", "Y轴偏移 | 动画时长 | 圆角 | 透明度") { show = true } }
        Spacer(Modifier.height(24.dp))
        Text("关于", fontSize = 13.sp, color = C_Sub, modifier = Modifier.padding(start = 16.dp, bottom = 4.dp))
        MiuiCard {
            MiuiArrowItem("HyperNotice", "v1.0.0") { Toast.makeText(ctx, "HyperNotice v1.0.0", Toast.LENGTH_SHORT).show() }
            MiuiArrowItem("GitHub", "q02144235/HyperNotice") { Toast.makeText(ctx, "GitHub: q02144235/HyperNotice", Toast.LENGTH_SHORT).show() }
        }
        Spacer(Modifier.height(80.dp))
    }
}
