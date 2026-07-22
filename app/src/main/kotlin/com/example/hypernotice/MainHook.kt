package com.example.hypernotice

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

        XposedBridge.log("[HyperNotice] === SystemUI loaded ===")

        prefs = XSharedPreferences(PREFS_NAME)
        prefs?.makeWorldReadable()

        // 探查所有候选类
        val candidates = listOf(
            "miui.systemui.notification.focus.FocusNotificationController",
            "miui.systemui.dynamicisland.window.content.DynamicIslandBaseContentView",
            "miui.systemui.dynamicisland.window.content.DynamicIslandContentView",
            "miui.systemui.dynamicisland.window.DynamicIslandWindowViewController",
            "miui.systemui.dynamicisland.DynamicIslandController",
            "miui.systemui.dynamicisland.DynamicFeatureConfig",
            "miui.systemui.notification.NotificationSettingsManager",
            "com.android.systemui.statusbar.phone.FocusedNotifPromptView",
            "com.android.systemui.statusbar.phone.FocusedNotifPromptController"
        )

        var foundCount = 0
        for (cn in candidates) {
            try {
                val clz = XposedHelpers.findClass(cn, lpparam.classLoader)
                XposedBridge.log("[HyperNotice] ✓ $cn")
                for (m in clz.declaredMethods) {
                    XposedBridge.log("[HyperNotice]   → ${m.name}(${m.parameterTypes.size} params)")
                }
                foundCount++
            } catch (_: Throwable) {
                XposedBridge.log("[HyperNotice] ✗ $cn")
            }
        }
        XposedBridge.log("[HyperNotice] === Found $foundCount/${candidates.size} ===")

        // Hook FocusNotificationController 所有方法
        try {
            val fnc = XposedHelpers.findClass(
                "miui.systemui.notification.focus.FocusNotificationController", lpparam.classLoader)
            XposedBridge.log("[HyperNotice] ✓ Hook FocusNotificationController")
            for (m in fnc.declaredMethods) {
                try {
                    XposedHelpers.findAndHookMethod(fnc, m.name, *m.parameterTypes, object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            XposedBridge.log("[HyperNotice] CALL FocusController.${m.name}")
                            applyModifications(param.thisObject, "FocusNotificationController")
                        }
                    })
                } catch (_: Throwable) {}
            }
        } catch (e: Throwable) {
            XposedBridge.log("[HyperNotice] ✗ FocusNotificationController: ${e.message}")
        }

        // Hook DynamicIslandBaseContentView
        try {
            val dibcv = XposedHelpers.findClass(
                "miui.systemui.dynamicisland.window.content.DynamicIslandBaseContentView", lpparam.classLoader)
            XposedBridge.log("[HyperNotice] ✓ Hook DynamicIslandBaseContentView")
            for (m in dibcv.declaredMethods) {
                try {
                    XposedHelpers.findAndHookMethod(dibcv, m.name, *m.parameterTypes, object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            XposedBridge.log("[HyperNotice] CALL DynamicIslandBaseContentView.${m.name}")
                        }
                    })
                } catch (_: Throwable) {}
            }
        } catch (e: Throwable) {
            XposedBridge.log("[HyperNotice] ✗ DynamicIslandBaseContentView: ${e.message}")
        }
    }

    private fun applyModifications(obj: Any, tag: String) {
        try {
            prefs?.reload()
        } catch (_: Exception) {}

        val yOffset = getPrefInt("y_offset", 45)
        try {
            XposedHelpers.setObjectField(obj, "mOffsetY", yOffset)
            XposedBridge.log("[HyperNotice] $tag: set mOffsetY=$yOffset")
        } catch (_: Exception) {}
    }

    private fun getPrefInt(key: String, default: Int): Int {
        return try { prefs?.reload(); prefs?.getInt(key, default) ?: default }
        catch (_: Exception) { default }
    }

    private fun getPrefBoolean(key: String, default: Boolean): Boolean {
        return try { prefs?.reload(); prefs?.getBoolean(key, default) ?: default }
        catch (_: Exception) { default }
    }
}
