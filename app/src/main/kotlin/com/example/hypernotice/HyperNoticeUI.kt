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
        Switch(
            checked = localChecked,
            onCheckedChange = { localChecked = it; onCheckedChange(it) },
            colors = SwitchDefaults.colors(
                checkedThumbColor = C_White,
                checkedTrackColor = C_Accent,
                uncheckedThumbColor = C_White,
                uncheckedTrackColor = C_SwitchTrack,
                uncheckedBorderColor = Color.Transparent,
                checkedBorderColor = Color.Transparent
            )
        )
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

    val noRipple = remember { MutableInteractionSource() }

    if (showSlider) {
        Column(modifier = Modifier.fillMaxSize().background(C_Bg).verticalScroll(scroll)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { showSlider = false }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) { Text("‹  返回", color = C_Accent, fontSize = 17.sp) }
            Spacer(Modifier.height(8.dp))

            Text("功能开关", color = C_Sub, fontSize = 13.sp, modifier = Modifier.padding(start = 16.dp, bottom = 4.dp))
            MiuiCard {
                MiuiSwitchItem("开启焦点通知上移", "将通知位置向上移动", true) { }
                MiuiSwitchItem("开启纯黑背景", "使用纯黑作为通知背景", false) { }
                MiuiSwitchItem("显示应用名称", "显示来源应用名称", true) { }
            }
            Spacer(Modifier.height(24.dp))
            Text("位置调整", color = C_Sub, fontSize = 13.sp, modifier = Modifier.padding(start = 16.dp, bottom = 4.dp))
            MiuiCard {
                // Y轴
                var yVal by remember { mutableFloatStateOf(45f) }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Y 轴偏移", color = C_Text, fontSize = 14.sp, modifier = Modifier.width(72.dp))
                    Slider(
                        value = yVal,
                        onValueChange = { yVal = it },
                        valueRange = 0f..100f,
                        modifier = Modifier.weight(1f).height(32.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = C_Accent,
                            activeTrackColor = C_Accent,
                            inactiveTrackColor = C_SwitchTrack,
                            inactiveTickColor = Color.Transparent,
                            activeTickColor = Color.Transparent
                        )
                    )
                    Text("${yVal.roundToInt()}dp", color = C_Sub, fontSize = 13.sp, modifier = Modifier.width(40.dp), textAlign = TextAlign.End)
                }
                // 动画时长
                var dVal by remember { mutableFloatStateOf(300f) }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("动画时长", color = C_Text, fontSize = 14.sp, modifier = Modifier.width(72.dp))
                    Slider(
                        value = dVal,
                        onValueChange = { dVal = it },
                        valueRange = 0f..1000f,
                        modifier = Modifier.weight(1f).height(32.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = C_Accent,
                            activeTrackColor = C_Accent,
                            inactiveTrackColor = C_SwitchTrack,
                            inactiveTickColor = Color.Transparent,
                            activeTickColor = Color.Transparent
                        )
                    )
                    Text("${dVal.roundToInt()}ms", color = C_Sub, fontSize = 13.sp, modifier = Modifier.width(48.dp), textAlign = TextAlign.End)
                }
                // 圆角大小
                var rVal by remember { mutableFloatStateOf(16f) }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("圆角大小", color = C_Text, fontSize = 14.sp, modifier = Modifier.width(72.dp))
                    Slider(
                        value = rVal,
                        onValueChange = { rVal = it },
                        valueRange = 0f..50f,
                        modifier = Modifier.weight(1f).height(32.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = C_Accent,
                            activeTrackColor = C_Accent,
                            inactiveTrackColor = C_SwitchTrack,
                            inactiveTickColor = Color.Transparent,
                            activeTickColor = Color.Transparent
                        )
                    )
                    Text("${rVal.roundToInt()}dp", color = C_Sub, fontSize = 13.sp, modifier = Modifier.width(40.dp), textAlign = TextAlign.End)
                }
                // 透明度
                var aVal by remember { mutableFloatStateOf(95f) }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("透明度", color = C_Text, fontSize = 14.sp, modifier = Modifier.width(72.dp))
                    Slider(
                        value = aVal,
                        onValueChange = { aVal = it },
                        valueRange = 0f..100f,
                        modifier = Modifier.weight(1f).height(32.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = C_Accent,
                            activeTrackColor = C_Accent,
                            inactiveTrackColor = C_SwitchTrack,
                            inactiveTickColor = Color.Transparent,
                            activeTickColor = Color.Transparent
                        )
                    )
                    Text("${aVal.roundToInt()}%", color = C_Sub, fontSize = 13.sp, modifier = Modifier.width(40.dp), textAlign = TextAlign.End)
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("关于", color = C_Sub, fontSize = 13.sp, modifier = Modifier.padding(start = 16.dp, bottom = 4.dp))
            MiuiCard {
                MiuiArrowItem("HyperNotice", "v1.0.0") { Toast.makeText(context, "HyperNotice v1.0.0", Toast.LENGTH_SHORT).show() }
                MiuiArrowItem("GitHub", "q02144235/HyperNotice") { Toast.makeText(context, "GitHub: q02144235/HyperNotice", Toast.LENGTH_SHORT).show() }
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
