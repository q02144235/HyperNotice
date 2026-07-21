package com.example.hypernotice

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

val C_Bg = Color(0xFFF7F7F7)
val C_Card = Color(0xFFFFFFFF)
val C_Accent = Color(0xFF3482FF)
val C_Text = Color(0xCC000000)
val C_Sub = Color(0x99000000)
val C_SearchBg = Color(0xFFF2F2F2)
val C_NavOff = Color(0xFF8E8E93)
val C_White = Color(0xFFFFFFFF)
val C_SwitchTrack = Color(0xFFE5E5EA)

@Composable
fun MiuiSearchBar() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(36.dp)
            .background(C_SearchBg, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.CenterStart
    ) {
        Text("🔍  搜索设置", color = C_Sub, fontSize = 14.sp, modifier = Modifier.padding(start = 12.dp))
    }
}

@Composable
fun MiuiSectionTitle(title: String) {
    Text(title, color = C_Sub, fontSize = 13.sp, modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 6.dp))
}

@Composable
fun MiuiCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = C_Card),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp), content = content)
    }
}

@Composable
fun MiuiSwitchItem(title: String, summary: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp).clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = C_Text, fontSize = 16.sp)
            Text(summary, color = C_Sub, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = C_White, checkedTrackColor = C_Accent,
                uncheckedThumbColor = C_White, uncheckedTrackColor = C_SwitchTrack
            )
        )
    }
}

@Composable
fun MiuiArrowItem(title: String, summary: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp).clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = C_Text, fontSize = 16.sp)
            Text(summary, color = C_Sub, fontSize = 12.sp)
        }
        Text("›", color = C_NavOff, fontSize = 24.sp, fontWeight = FontWeight.Light)
    }
}

@Composable
fun MiuiSliderPage(title: String, value: Int, suffix: String, onBack: () -> Unit) {
    var sliderValue by remember { mutableFloatStateOf(value.toFloat()) }
    Column(modifier = Modifier.fillMaxSize().background(C_Bg)) {
        Box(
            modifier = Modifier.fillMaxWidth().height(52.dp).background(C_Card).clickable(onClick = onBack),
            contentAlignment = Alignment.CenterStart
        ) { Text("‹  返回", color = C_Accent, fontSize = 17.sp, modifier = Modifier.padding(start = 16.dp)) }
        Spacer(Modifier.height(24.dp))
        Text(title, color = C_Text, fontSize = 18.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(32.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Slider(
                value = sliderValue, onValueChange = { sliderValue = it },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(thumbColor = C_Accent, activeTrackColor = C_Accent, inactiveTrackColor = C_SwitchTrack),
                modifier = Modifier.weight(1f).height(40.dp)
            )
            Text("${sliderValue.toInt()}$suffix", color = C_Sub, fontSize = 14.sp, modifier = Modifier.width(44.dp), textAlign = TextAlign.End)
        }
        Spacer(Modifier.weight(1f))
        Button(
            onClick = onBack,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = C_Accent, contentColor = C_White),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(44.dp)
        ) { Text("完成", fontSize = 16.sp) }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun HyperNoticeApp() {
    var currentPage by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scroll = rememberScrollState()
    if (currentPage.isNotEmpty()) {
        val info = when (currentPage) {
            "y" -> Triple("Y 轴偏移", 45, "dp")
            "duration" -> Triple("动画时长", 300, "ms")
            "round" -> Triple("圆角大小", 16, "dp")
            "alpha" -> Triple("透明度", 95, "%")
            "width" -> Triple("宽度", 100, "%")
            "padding" -> Triple("内边距", 12, "dp")
            else -> Triple("", 0, "")
        }
        MiuiSliderPage(info.first, info.second, info.third) { currentPage = "" }
        return
    }
    Column(modifier = Modifier.fillMaxSize().background(C_Bg).verticalScroll(scroll)) {
        Spacer(Modifier.height(12.dp))
        MiuiSearchBar()
        Spacer(Modifier.height(12.dp))
        MiuiSectionTitle("功能开关")
        MiuiCard {
            MiuiSwitchItem("开启焦点通知上移", "将通知位置向上移动", true) { }
            MiuiSwitchItem("开启纯黑背景", "使用纯黑作为通知背景", false) { }
            MiuiSwitchItem("显示应用名称", "显示来源应用名称", true) { }
        }
        MiuiSectionTitle("位置调整")
        MiuiCard {
            MiuiArrowItem("Y 轴偏移", "当前: 45dp") { currentPage = "y" }
            MiuiArrowItem("动画时长", "当前: 300ms") { currentPage = "duration" }
            MiuiArrowItem("圆角大小", "当前: 16dp") { currentPage = "round" }
            MiuiArrowItem("透明度", "当前: 95%") { currentPage = "alpha" }
        }
        MiuiSectionTitle("大小调整")
        MiuiCard {
            MiuiArrowItem("宽度调整", "当前: 100%") { currentPage = "width" }
            MiuiArrowItem("内边距", "当前: 12dp") { currentPage = "padding" }
        }
        MiuiSectionTitle("关于")
        MiuiCard {
            MiuiArrowItem("HyperNotice", "v1.0.0") { Toast.makeText(context, "HyperNotice v1.0.0", Toast.LENGTH_SHORT).show() }
            MiuiArrowItem("GitHub", "q02144235/HyperNotice") { Toast.makeText(context, "GitHub: q02144235/HyperNotice", Toast.LENGTH_SHORT).show() }
        }
        Spacer(Modifier.height(80.dp))
    }
}
