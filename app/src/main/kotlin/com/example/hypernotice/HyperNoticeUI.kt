package com.example.hypernotice

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

val C_Bg = Color(0xFFFFFFFF)
val C_Card = Color(0xFFFFFFFF)
val C_Accent = Color(0xFF3482FF)
val C_Text = Color(0xCC000000)
val C_Sub = Color(0x99000000)
val C_White = Color(0xFFFFFFFF)
val C_SwitchTrack = Color(0xFFE5E5EA)

// 自定义 Switch——白色滑块一样大、无边框
@Composable
fun MiuiSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val thumbSize = 20.dp
    val trackWidth = 48.dp
    val trackHeight = 28.dp
    Box(
        modifier = Modifier
            .width(trackWidth).height(trackHeight)
            .background(
                if (checked) C_Accent else C_SwitchTrack,
                RoundedCornerShape(trackHeight / 2)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) }
            .padding(if (checked) PaddingValues(start = trackWidth - thumbSize - 2.dp, top = 2.dp, bottom = 2.dp, end = 2.dp)
                else PaddingValues(start = 2.dp, top = 2.dp, bottom = 2.dp, end = trackWidth - thumbSize - 2.dp))
    ) {
        Box(
            modifier = Modifier.size(thumbSize).background(C_White, RoundedCornerShape(thumbSize / 2))
        )
    }
}

// 自定义 Slider（胶囊样式）
@Composable
fun MiuiSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..100f
) {
    val fraction = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)
    Box(
        modifier = Modifier.fillMaxWidth().height(32.dp).clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { },
        contentAlignment = Alignment.CenterStart
    ) {
        // 灰色轨道背景
        Box(
            modifier = Modifier.fillMaxWidth().height(28.dp)
                .background(C_SwitchTrack, RoundedCornerShape(14.dp))
        )
        // 蓝色激活轨道
        Box(
            modifier = Modifier.fillMaxWidth(fraction).height(28.dp)
                .background(C_Accent, RoundedCornerShape(14.dp))
        )
        // 白色滑块
        Box(
            modifier = Modifier.offset(x = with(androidx.compose.ui.platform.LocalDensity.current) {
                ((fraction * 1000) - 14).toDp()
            }).size(28.dp).background(C_White, RoundedCornerShape(14.dp))
        )
    }
}

@Composable
fun MiuiCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = C_Card),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
    ) {
        Column(content = content)
    }
}

@Composable
fun MiuiSwitchItem(title: String, summary: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    var localChecked by remember { mutableStateOf(checked) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { localChecked = !localChecked; onCheckedChange(localChecked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = C_Text, fontSize = 16.sp)
            if (summary.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(summary, color = C_Sub, fontSize = 12.sp)
            }
        }
        MiuiSwitch(checked = localChecked, onCheckedChange = { localChecked = it; onCheckedChange(it) })
    }
}

@Composable
fun MiuiArrowItem(title: String, summary: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = C_Text, fontSize = 16.sp)
            if (summary.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(summary, color = C_Sub, fontSize = 12.sp)
            }
        }
        Text("›", color = Color(0xFF8E8E93), fontSize = 24.sp, fontWeight = FontWeight.Light)
    }
}

@Composable
fun HyperNoticeApp() {
    var showSlider by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scroll = rememberScrollState()

    if (showSlider) {
        Column(modifier = Modifier.fillMaxSize().background(C_Bg).verticalScroll(scroll)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showSlider = false }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) { Text("‹  返回", color = C_Accent, fontSize = 17.sp) }
            Spacer(Modifier.height(8.dp))
            Text("位置调整", color = C_Sub, fontSize = 13.sp, modifier = Modifier.padding(start = 16.dp, bottom = 4.dp))
            MiuiCard {
                // Y轴
                var yVal by remember { mutableFloatStateOf(45f) }
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Y 轴偏移", color = C_Text, fontSize = 15.sp)
                        Text("${yVal.roundToInt()}dp", color = C_Sub, fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    MiuiSlider(value = yVal, onValueChange = { yVal = it }, valueRange = 0f..200f)
                }
                // 动画时长
                var dVal by remember { mutableFloatStateOf(300f) }
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("动画时长", color = C_Text, fontSize = 15.sp)
                        Text("${dVal.roundToInt()}ms", color = C_Sub, fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    MiuiSlider(value = dVal, onValueChange = { dVal = it }, valueRange = 0f..2000f)
                }
                // 圆角大小
                var rVal by remember { mutableFloatStateOf(16f) }
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("圆角大小", color = C_Text, fontSize = 15.sp)
                        Text("${rVal.roundToInt()}dp", color = C_Sub, fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    MiuiSlider(value = rVal, onValueChange = { rVal = it }, valueRange = 0f..50f)
                }
                // 透明度
                var aVal by remember { mutableFloatStateOf(95f) }
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("透明度", color = C_Text, fontSize = 15.sp)
                        Text("${aVal.roundToInt()}%", color = C_Sub, fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    MiuiSlider(value = aVal, onValueChange = { aVal = it }, valueRange = 0f..100f)
                }
            }
            Spacer(Modifier.height(80.dp))
        }
        return
    }

    // 主页
    Column(modifier = Modifier.fillMaxSize().background(C_Bg).verticalScroll(scroll)) {
        Spacer(Modifier.height(16.dp))
        Text("功能开关", color = C_Sub, fontSize = 13.sp, modifier = Modifier.padding(start = 16.dp, bottom = 4.dp))
        MiuiCard {
            MiuiSwitchItem("开启焦点通知上移", "将通知位置向上移动", true) { }
            MiuiSwitchItem("开启纯黑背景", "使用纯黑作为通知背景", false) { }
            MiuiSwitchItem("显示应用名称", "显示来源应用名称", true) { }
        }
        Spacer(Modifier.height(24.dp))
        Text("位置调整", color = C_Sub, fontSize = 13.sp, modifier = Modifier.padding(start = 16.dp, bottom = 4.dp))
        MiuiCard {
            MiuiArrowItem("位置与外观", "Y轴偏移 | 动画时长 | 圆角 | 透明度") { showSlider = true }
        }
        Spacer(Modifier.height(24.dp))
        Text("关于", color = C_Sub, fontSize = 13.sp, modifier = Modifier.padding(start = 16.dp, bottom = 4.dp))
        MiuiCard {
            MiuiArrowItem("HyperNotice", "v1.0.0") { Toast.makeText(context, "HyperNotice v1.0.0", Toast.LENGTH_SHORT).show() }
            MiuiArrowItem("GitHub", "q02144235/HyperNotice") { Toast.makeText(context, "GitHub: q02144235/HyperNotice", Toast.LENGTH_SHORT).show() }
        }
        Spacer(Modifier.height(80.dp))
    }
}
