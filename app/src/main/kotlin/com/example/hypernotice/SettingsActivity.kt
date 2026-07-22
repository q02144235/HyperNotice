package com.example.hypernotice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HyperNoticeApp(
                onDiagnose = {
                    val info = StringBuilder()
                    info.appendLine("=== 自检信息 ===")

                    // 1. 检查 assets 文件
                    try {
                        val assetsList = assets.list("")
                        info.appendLine("assets 文件：${assetsList?.joinToString(", ") ?: "null"}")
                    } catch (e: Exception) {
                        info.appendLine("assets 读取失败：${e.message}")
                    }

                    // 2. 检查 xposed_init 内容
                    try {
                        val xpi = assets.open("xposed_init").bufferedReader().readText()
                        info.appendLine("xposed_init：$xpi")
                    } catch (e: Exception) {
                        info.appendLine("xposed_init 不存在！${e.message}")
                    }

                    // 3. 检查 scope.list
                    try {
                        val scope = assets.open("scope.list").bufferedReader().readText()
                        info.appendLine("scope.list：$scope")
                    } catch (e: Exception) {
                        info.appendLine("scope.list 不存在！${e.message}")
                    }

                    // 4. 检查 module.prop
                    try {
                        val prop = assets.open("module.prop").bufferedReader().readText()
                        info.appendLine("module.prop：$prop")
                    } catch (e: Exception) {
                        info.appendLine("module.prop 不存在！${e.message}")
                    }

                    // 5. 检查 Xposed API 是否可用
                    try {
                        val clz = Class.forName("de.robv.android.xposed.XposedBridge")
                        info.appendLine("XposedBridge：可用 ✓")
                    } catch (e: Exception) {
                        info.appendLine("XposedBridge：不可用 ✗（${e.message}）")
                    }

                    // 6. 检查 IXposedHookLoadPackage
                    try {
                        val clz = Class.forName("de.robv.android.xposed.IXposedHookLoadPackage")
                        info.appendLine("IXposedHookLoadPackage：可用 ✓")
                    } catch (e: Exception) {
                        info.appendLine("IXposedHookLoadPackage：不可用 ✗")
                    }

                    info.toString()
                }
            )
        }
    }
}
