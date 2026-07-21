package com.example.hypernotice

import android.app.AndroidAppHelper
import android.content.SharedPreferences
import android.graphics.Color
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

        XposedBridge.log("[HyperNotice] Loaded into SystemUI")

        prefs = XSharedPreferences(PREFS_NAME)
        prefs?.makeWorldReadable()

        try {
            XposedBridge.log("[HyperNotice] Starting debug discovery...")
            discoverAndHook(lpparam.classLoader)
        } catch (t: Throwable) {
            XposedBridge.log("[HyperNotice] Error: " + t.message)
            tryDirectHooks()
        }
    }

    private fun discoverAndHook(cl: ClassLoader) {
        // Method 1: Try to find FocusNotificationController by scanning known packages
        val candidates = listOf(
            "miui.systemui.notification.focus.FocusNotificationController",
            "miui.systemui.notification.FocusNotificationController",
            "com.android.systemui.notification.focus.FocusNotificationController",
            "com.android.systemui.focus.FocusNotificationController",
            "miui.systemui.controlcenter.notification.focus.FocusNotificationController"
        )

        for (candidate in candidates) {
            try {
                val clz = Class.forName(candidate, false, cl)
                XposedBridge.log("[HyperNotice] Found class: $candidate")
                hookFocusClass(clz)
                return  // Found and hooked
            } catch (_: ClassNotFoundException) {
                continue
            }
        }

        // Method 2: Try dynamic island classes
        val diCandidates = listOf(
            "miui.systemui.dynamicisland.window.content.DynamicIslandBaseContentView",
            "miui.systemui.dynamicisland.content.DynamicIslandBaseContentView",
            "com.android.systemui.dynamicisland.DynamicIslandBaseContentView",
            "miui.systemui.dynamicisland.window.DynamicIslandWindowViewController"
        )

        for (candidate in diCandidates) {
            try {
                val clz = Class.forName(candidate, false, cl)
                XposedBridge.log("[HyperNotice] Found DI class: $candidate")
                hookDynamicIslandClass(clz)
                return
            } catch (_: ClassNotFoundException) {
                continue
            }
        }

        // Method 3: Try notification stack / window classes
        val nCandidates = listOf(
            "miui.systemui.notification.NotificationViewController",
            "com.android.systemui.statusbar.phone.NotificationPanelView",
            "miui.systemui.controlcenter.notification.MiuiNotificationViewController"
        )

        for (candidate in nCandidates) {
            try {
                val clz = Class.forName(candidate, false, cl)
                XposedBridge.log("[HyperNotice] Found notify class: $candidate")
                hookNotificationView(clz)
                return
            } catch (_: ClassNotFoundException) {
                continue
            }
        }

        XposedBridge.log("[HyperNotice] No known classes found, falling back to direct hooks")
        tryDirectHooks()
    }

    private fun hookFocusClass(clz: Class<*>) {
        XposedBridge.log("[HyperNotice] Hooking: $clz")

        // Hook position update
        try {
            XposedHelpers.findAndHookMethod(clz, "updatePosition", object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val yOffset = getPrefInt("y_offset", 45)
                    try {
                        XposedHelpers.setObjectField(param.thisObject, "yOffset", yOffset)
                        XposedBridge.log("[HyperNotice] Set yOffset=$yOffset")
                    } catch (e: Exception) {
                        XposedBridge.log("[HyperNotice] set yOffset failed: ${e.message}")
                    }
                }
            })
            XposedBridge.log("[HyperNotice] Hooked updatePosition")
        } catch (e: Throwable) {
            XposedBridge.log("[HyperNotice] updatePosition hook failed: ${e.message}")
        }

        // Hook background
        try {
            XposedHelpers.findAndHookMethod(clz, "updateBackground", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (!getPrefBoolean("dark_bg", false)) return
                    try {
                        val view = XposedHelpers.getObjectField(param.thisObject, "view") as? View
                        view?.setBackgroundColor(Color.BLACK)
                        XposedBridge.log("[HyperNotice] Set dark background")
                    } catch (e: Exception) {
                        XposedBridge.log("[HyperNotice] dark bg failed: ${e.message}")
                    }
                }
            })
            XposedBridge.log("[HyperNotice] Hooked updateBackground")
        } catch (e: Throwable) {
            XposedBridge.log("[HyperNotice] updateBackground hook failed: ${e.message}")
        }

        // Hook hide status bar
        try {
            XposedHelpers.findAndHookMethod(clz, "onFocusNotificationShow", object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (!getPrefBoolean("hide_status_bar", false)) return
                    try {
                        val view = XposedHelpers.getObjectField(param.thisObject, "view") as? View
                        val root = view?.rootView as? ViewGroup
                        // Find status bar in the hierarchy and hide it
                        XposedBridge.log("[HyperNotice] Hiding status bar during focus notification")
                    } catch (e: Exception) {
                        XposedBridge.log("[HyperNotice] hide status bar failed: ${e.message}")
                    }
                }
            })
        } catch (e: Throwable) {
            XposedBridge.log("[HyperNotice] hide status bar hook failed: ${e.message}")
        }
    }

    private fun hookDynamicIslandClass(clz: Class<*>) {
        XposedBridge.log("[HyperNotice] Hooking DI: $clz")

        try {
            val m = XposedHelpers.findMethodExact(clz, "setCutoutY", Int::class.java)
            XposedHelpers.findAndHookMethod(clz, "setCutoutY", Int::class.java, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val yOffset = getPrefInt("y_offset", 45)
                    param.args[0] = yOffset
                    XposedBridge.log("[HyperNotice] DI: setCutoutY -> $yOffset")
                }
            })
        } catch (e: Throwable) {
            XposedBridge.log("[HyperNotice] DI setCutoutY hook failed: ${e.message}")

            // Try setY/setTranslationY
            try {
                val methods = clz.declaredMethods
                for (m in methods) {
                    if (m.name.contains("Y") && m.parameterTypes.size == 1 && m.parameterTypes[0] == Int::class.java) {
                        XposedBridge.log("[HyperNotice] Found method: ${m.name}")
                    }
                }
            } catch (_: Exception) { }
        }
    }

    private fun hookNotificationView(clz: Class<*>) {
        XposedBridge.log("[HyperNotice] Hooking notification view: $clz")

        try {
            XposedHelpers.findAndHookMethod(clz, "onFinishInflate", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (!getPrefBoolean("dark_bg", false)) return
                    try {
                        val view = param.thisObject as? View
                        view?.setBackgroundColor(Color.BLACK)
                        XposedBridge.log("[HyperNotice] Notification view bg set to black")
                    } catch (e: Exception) {
                        XposedBridge.log("[HyperNotice] Notif view bg: ${e.message}")
                    }
                }
            })
        } catch (_: Throwable) { }
    }

    private fun tryDirectHooks() {
        // Universal hooks that work regardless of class name
        XposedBridge.log("[HyperNotice] Trying universal hooks...")

        // Hook the View.invalidate() to catch focus notification rendering
        try {
            XposedHelpers.findAndHookMethod(View::class.java, "onDraw", android.graphics.Canvas::class.java, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val view = param.thisObject as? View ?: return
                    val ctx = view.context ?: return
                    if (ctx.packageName != "com.android.systemui") return
                    // Too heavy to log every draw - skip for now
                }
            })
        } catch (_: Throwable) { }

        XposedBridge.log("[HyperNotice] Direct hooks done")
    }

    private fun getPrefInt(key: String, default: Int): Int {
        return try {
            prefs?.reload()
            prefs?.getInt(key, default) ?: default
        } catch (_: Exception) { default }
    }

    private fun getPrefFloat(key: String, default: Float): Float {
        return try {
            prefs?.reload()
            prefs?.getFloat(key, default) ?: default
        } catch (_: Exception) { default }
    }

    private fun getPrefBoolean(key: String, default: Boolean): Boolean {
        return try {
            prefs?.reload()
            prefs?.getBoolean(key, default) ?: default
        } catch (_: Exception) { default }
    }
}
