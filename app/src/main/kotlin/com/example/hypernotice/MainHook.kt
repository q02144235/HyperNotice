package com.example.hypernotice

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class MainHook : IXposedHookLoadPackage {

    private val PREFS_NAME = "com.example.hypernotice_preferences"
    private var prefs: XSharedPreferences? = null

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != "com.android.systemui") return

        XposedBridge.log("[HyperNotice] === Loaded into SystemUI ===")

        prefs = XSharedPreferences(PREFS_NAME)
        prefs?.makeWorldReadable()

        // ========== 参照 HyperIsland 的 Hook 方式 ==========

        // 1. Hook DynamicIslandBaseContentView.setCutoutY(float) — 位置控制
        hookDynamicIslandContentView(lpparam.classLoader)

        // 2. Hook FocusNotificationController
        hookFocusNotificationController(lpparam.classLoader)

        XposedBridge.log("[HyperNotice] === All hooks registered ===")
    }

    // ============================================================
    // Hook 核心：DynamicIslandBaseContentView.setCutoutY(float)
    // HyperIsland 就是用这个来控制焦点通知 Y 轴位置的！
    // ============================================================
    private fun hookDynamicIslandContentView(cl: ClassLoader) {
        try {
            val clz = XposedHelpers.findClass(
                "miui.systemui.dynamicisland.window.content.DynamicIslandBaseContentView", cl)
            XposedBridge.log("[HyperNotice] ✓ Found DynamicIslandBaseContentView")

            // Hook setCutoutY(float) — 修改 Y 轴位置
            XposedHelpers.findAndHookMethod(clz, "setCutoutY", Float::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        try {
                            val yOffset = getPrefInt("y_offset", 45)
                            val moveUp = getPrefBoolean("move_up_punchhole", false)

                            val view = param.thisObject as? View ?: return
                            val density = view.context.resources.displayMetrics.density
                            val offset = if (moveUp) -yOffset * density else -(yOffset * density).toFloat()
                            param.args[0] = offset

                            XposedBridge.log("[HyperNotice] setCutoutY -> $offset (${yOffset}dp)")
                        } catch (e: Exception) {
                            XposedBridge.log("[HyperNotice] setCutoutY hook err: ${e.message}")
                        }
                    }

                    override fun afterHookedMethod(param: MethodHookParam) {
                        try {
                            applyViewModifications(param.thisObject as? View)
                        } catch (_: Exception) {}
                    }
                })

            XposedBridge.log("[HyperNotice] ✓ Hooked DynamicIslandBaseContentView.setCutoutY")

            // 也 hook onLayout 来做额外修改
            try {
                XposedHelpers.findAndHookMethod(clz, "onLayout", Boolean::class.java,
                    Int::class.java, Int::class.java, Int::class.java, Int::class.java,
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            applyViewModifications(param.thisObject as? View)
                        }
                    })
                XposedBridge.log("[HyperNotice] ✓ Hooked onLayout")
            } catch (_: Exception) {}

        } catch (e: Exception) {
            XposedBridge.log("[HyperNotice] ✗ DynamicIslandBaseContentView: ${e.message}")
        }
    }

    // ============================================================
    // Hook FocusNotificationController
    // ============================================================
    private fun hookFocusNotificationController(cl: ClassLoader) {
        try {
            val clz = XposedHelpers.findClass(
                "miui.systemui.notification.focus.FocusNotificationController", cl)
            XposedBridge.log("[HyperNotice] ✓ Found FocusNotificationController")

            // Hook updatePosition — 控制位置
            try {
                XposedHelpers.findAndHookMethod(clz, "updatePosition",
                    object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            val yOffset = getPrefInt("y_offset", 45)
                            try {
                                XposedHelpers.setObjectField(param.thisObject, "mOffsetY", yOffset)
                                XposedBridge.log("[HyperNotice] FocusController: set mOffsetY=$yOffset")
                            } catch (_: Exception) {}
                        }
                    })
                XposedBridge.log("[HyperNotice] ✓ Hooked FocusNotificationController.updatePosition")
            } catch (_: Exception) {}

            XposedBridge.log("[HyperNotice] ✓ Hooked FocusNotificationController")
        } catch (e: Exception) {
            XposedBridge.log("[HyperNotice] ✗ FocusNotificationController: ${e.message}")
        }
    }

    // ============================================================
    // 应用外观修改到 View（背景、透明度、圆角、文字替换）
    // ============================================================
    private fun applyViewModifications(view: View?) {
        if (view == null) return

        try {
            prefs?.reload()
        } catch (_: Exception) {}

        val darkBg = getPrefBoolean("dark_bg", false)
        val opacity = getPrefInt("opacity", 95)
        val cornerRadius = getPrefInt("corner_radius", 16)
        val replaceText = getPrefBoolean("replace_text", false)
        val hideStatus = getPrefBoolean("hide_status_bar", false)

        // 纯黑背景
        if (darkBg) {
            try {
                view.setBackgroundColor(Color.BLACK)
                // 也尝试找子 View 设黑背景
                if (view is ViewGroup) {
                    for (i in 0 until view.childCount) {
                        view.getChildAt(i).setBackgroundColor(Color.BLACK)
                    }
                }
            } catch (_: Exception) {}
        }

        // 透明度
        if (opacity < 100) {
            try {
                view.alpha = opacity / 100f
            } catch (_: Exception) {}
        }

        // 圆角
        if (cornerRadius > 0) {
            try {
                view.setClipToOutline(true)
                val density = view.context.resources.displayMetrics.density
                val bg = view.background
                if (bg is GradientDrawable) {
                    bg.cornerRadius = cornerRadius * density
                }
            } catch (_: Exception) {}
        }

        // 替换文字
        if (replaceText) {
            try {
                replaceTextRecursive(view)
            } catch (_: Exception) {}
        }
    }

    private fun replaceTextRecursive(view: View) {
        if (view is TextView) {
            val text = view.text.toString()
            if (text.contains("通知")) {
                view.text = text.replace("通知", "通知通知")
            }
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                replaceTextRecursive(view.getChildAt(i))
            }
        }
    }

    // ============================================================
    // Preference helpers
    // ============================================================
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
