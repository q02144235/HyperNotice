package com.example.hypernotice

import android.app.AndroidAppHelper
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.view.View
import android.widget.LinearLayout
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class MainHook : IXposedHookLoadPackage {

    private val PREFS_NAME = "com.example.hypernotice_preferences"
    private var prefs: XSharedPreferences? = null

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != "com.android.systemui") return

        prefs = XSharedPreferences(PREFS_NAME)
        prefs?.makeWorldReadable()

        hookFocusNotificationPosition()
        hookFocusNotificationBackground()
        hookRestartButton()
    }

    // ========== A. Y轴位置调节 ==========
    private fun hookFocusNotificationPosition() {
        try {
            // HyperOS 焦点通知控制器
            val clz = XposedHelpers.findClass(
                "miui.systemui.notification.focus.FocusNotificationController",
                null
            )
            XposedHelpers.findAndHookMethod(
                clz, "updatePosition", object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val yOffset = getPrefInt("y_offset", 45)
                        // 设置位置偏移
                        XposedHelpers.setObjectField(param.thisObject, "yOffset", yOffset)
                    }
                })
        } catch (_: Throwable) {
            tryFallbackPositionHook()
        }
    }

    private fun tryFallbackPositionHook() {
        try {
            // 备选: Hook dynamic island 的 Y 轴设置
            val clz = XposedHelpers.findClass(
                "miui.systemui.dynamicisland.window.content.DynamicIslandBaseContentView",
                null
            )
            XposedHelpers.findAndHookMethod(
                clz, "setCutoutY", Int::class.java, object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val yOffset = getPrefInt("y_offset", 45)
                        param.args[0] = yOffset
                    }
                })
        } catch (_: Throwable) {
            // 都不行，不报错
        }
    }

    // ========== B. 纯黑背景 ==========
    private fun hookFocusNotificationBackground() {
        try {
            val clz = XposedHelpers.findClass(
                "miui.systemui.notification.focus.FocusNotificationController",
                null
            )
            XposedHelpers.findAndHookMethod(
                clz, "updateBackground", object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val darkBg = getPrefBoolean("dark_bg", false)
                        if (!darkBg) return

                        try {
                            val view = XposedHelpers.getObjectField(param.thisObject, "view") as? View
                            view?.setBackgroundColor(Color.BLACK)
                        } catch (_: Exception) { }
                    }
                })
        } catch (_: Throwable) {
            tryFallbackBackgroundHook()
        }
    }

    private fun tryFallbackBackgroundHook() {
        try {
            // 备选: Hook 通知的 RootView 背景
            val clz = XposedHelpers.findClass(
                "miui.systemui.dynamicisland.window.DynamicIslandWindowViewController",
                null
            )
            XposedHelpers.findAndHookMethod(
                clz, "onViewAdded", View::class.java, object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val darkBg = getPrefBoolean("dark_bg", false)
                        if (!darkBg) return
                        val v = param.args[0] as? View ?: return
                        v.setBackgroundColor(Color.BLACK)
                    }
                })
        } catch (_: Throwable) { }
    }

    // ========== C. 重启系统界面按钮（通过 AIDL 或直接杀进程） ==========
    private fun hookRestartButton() {
        // 这部分在 UI 里已经做了（am force-stop com.android.systemui）
        // Hook 层不需要额外实现，但保留一个监听方法
        try {
            val clz = XposedHelpers.findClass(
                "miui.systemui.notification.focus.FocusNotificationController",
                null
            )
            XposedHelpers.findAndHookMethod(
                clz, "onRestartSystemUI", object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        try {
                            val process = Runtime.getRuntime().exec(arrayOf("am", "force-stop", "com.android.systemui"))
                            process.waitFor()
                        } catch (_: Exception) { }
                    }
                })
        } catch (_: Throwable) { }
    }

    // ========== 工具方法 ==========
    private fun getPrefInt(key: String, default: Int): Int {
        return try {
            prefs?.reload()
            prefs?.getInt(key, default) ?: default
        } catch (_: Exception) { default }
    }

    private fun getPrefBoolean(key: String, default: Boolean): Boolean {
        return try {
            prefs?.reload()
            prefs?.getBoolean(key, default) ?: default
        } catch (_: Exception) { default }
    }
}
