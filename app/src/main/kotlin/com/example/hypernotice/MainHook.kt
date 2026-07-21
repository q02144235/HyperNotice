package com.example.hypernotice

import android.content.Context
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

        hookFocusedNotifPromptView(lpparam.classLoader)
        hookFocusedNotifPromptController(lpparam.classLoader)

        XposedBridge.log("[HyperNotice] === All hooks registered ===")
    }

    // ============================================================
    // Hook 1: FocusedNotifPromptView — 视图布局时修改
    // ============================================================
    private fun hookFocusedNotifPromptView(cl: ClassLoader) {
        try {
            val viewClz = XposedHelpers.findClass(
                "com.android.systemui.statusbar.phone.FocusedNotifPromptView", cl)
            XposedBridge.log("[HyperNotice] ✓ Found FocusedNotifPromptView")

            // Hook onLayout — 每次布局后调整
            XposedHelpers.findAndHookMethod(
                viewClz, "onLayout", Boolean::class.java,
                Int::class.java, Int::class.java, Int::class.java, Int::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        adjustView(param.thisObject as View)
                    }
                })

            // Hook onAttachedToWindow
            XposedHelpers.findAndHookMethod(
                viewClz, "onAttachedToWindow",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        adjustView(param.thisObject as View)
                    }
                })

            XposedBridge.log("[HyperNotice] ✓ Hooked FocusedNotifPromptView.onLayout")
        } catch (e: Exception) {
            XposedBridge.log("[HyperNotice] ✗ FocusedNotifPromptView: ${e.message}")
        }
    }

    // ============================================================
    // Hook 2: FocusedNotifPromptController — 更新时调整
    // ============================================================
    private fun hookFocusedNotifPromptController(cl: ClassLoader) {
        try {
            val ctrlClz = XposedHelpers.findClass(
                "com.android.systemui.statusbar.phone.FocusedNotifPromptController", cl)
            XposedBridge.log("[HyperNotice] ✓ Found FocusedNotifPromptController")

            // Hook update(int)
            XposedHelpers.findAndHookMethod(
                ctrlClz, "update", Int::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        try {
                            val view = XposedHelpers.getObjectField(
                                param.thisObject, "mView") as? View
                            view?.let { adjustView(it) }
                        } catch (_: Exception) {}
                    }
                })

            // Hook setView
            XposedHelpers.findAndHookMethod(
                ctrlClz, "setView",
                XposedHelpers.findClass(
                    "com.android.systemui.statusbar.phone.FocusedNotifPromptView", cl),
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        try {
                            val view = param.args[0] as? View
                            view?.let { adjustView(it) }
                        } catch (_: Exception) {}
                    }
                })

            XposedBridge.log("[HyperNotice] ✓ Hooked FocusedNotifPromptController")
        } catch (e: Exception) {
            XposedBridge.log("[HyperNotice] ✗ FocusedNotifPromptController: ${e.message}")
        }
    }

    // ============================================================
    // 核心：调整视图
    // ============================================================
    private fun adjustView(view: View) {
        try {
            prefs?.reload()
        } catch (_: Exception) {}

        val yOffset = getPrefInt("y_offset", 45)
        val moveUp = getPrefBoolean("move_up_punchhole", false)
        val darkBg = getPrefBoolean("dark_bg", false)
        val opacity = getPrefInt("opacity", 95)
        val cornerRadius = getPrefInt("corner_radius", 16)
        val replaceText = getPrefBoolean("replace_text", false)
        val hideStatus = getPrefBoolean("hide_status_bar", false)

        val ctx = view.context
        val density = ctx.resources.displayMetrics.density

        // 1. Y轴偏移 — 用 translationY，不影响布局流程
        if (moveUp || yOffset != 45) {
            val offsetPx = -(yOffset * density).toInt()
            view.translationY = offsetPx.toFloat()
            XposedBridge.log("[HyperNotice] translationY=$offsetPx (${yOffset}dp)")
        }

        // 2. 纯黑背景
        if (darkBg) {
            view.setBackgroundColor(Color.BLACK)
        }

        // 3. 透明度
        if (opacity < 100) {
            view.alpha = opacity / 100f
        }

        // 4. 圆角
        if (cornerRadius > 0) {
            try {
                view.setClipToOutline(true)
                val bg = view.background
                if (bg is GradientDrawable) {
                    bg.cornerRadius = cornerRadius * density
                }
            } catch (_: Exception) {}
        }

        // 5. 替换文字
        if (replaceText) {
            replaceTextRecursive(view)
        }

        // 6. 隐藏状态栏 — 找父容器中的状态栏
        if (hideStatus) {
            try {
                var parent = view.parent
                while (parent is ViewGroup) {
                    for (i in 0 until parent.childCount) {
                        val child = parent.getChildAt(i)
                        if (child.tag?.toString()?.contains("status", true) == true) {
                            child.visibility = View.GONE
                        }
                    }
                    parent = parent.parent
                }
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
