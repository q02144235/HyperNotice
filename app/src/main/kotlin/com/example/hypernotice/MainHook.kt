package com.example.hypernotice

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.util.Vector

class MainHook : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != "com.android.systemui") return

        XposedBridge.log("[HyperNotice] === SystemUI loaded ===")
        XposedBridge.log("[HyperNotice] classLoader = ${lpparam.classLoader}")

        var count = 0

        XposedBridge.log("[HyperNotice] Direct class loading attempt...")
        val candidates = listOf(
            "miui.systemui.notification.focus.FocusNotificationController",
            "miui.systemui.dynamicisland.window.content.DynamicIslandBaseContentView",
            "miui.systemui.dynamicisland.DynamicIslandController",
            "miui.systemui.dynamicisland.module.IslandTextViewHolder",
            "miui.systemui.dynamicisland.DynamicFeatureConfig",
            "miui.systemui.notification.NotificationSettingsManager",
            "com.android.systemui.statusbar.phone.FocusedNotifPromptView",
            "com.android.systemui.statusbar.phone.FocusedNotifPromptController",
            "com.android.systemui.statusbar.notification.DynamicIslandController",
            "com.android.systemui.statusbar.notification.policy.FakeFocusNotifControllerImpl",
            "miui.systemui.dynamicisland.window.DynamicIslandWindowViewController",
            "miui.systemui.dynamicisland.window.content.DynamicIslandContentView",
            "miui.systemui.dynamicisland.anim.DynamicIslandAnimationController",
            "miui.systemui.dynamicisland.view.DynamicGlowEffectView",
            "miui.systemui.dynamicisland.DynamicIslandBackgroundView"
        )
        for (cn in candidates) {
            try {
                val clz = XposedHelpers.findClass(cn, lpparam.classLoader)
                XposedBridge.log("[HyperNotice] ✓ FOUND: $cn")
                for (m in clz.declaredMethods) {
                    XposedBridge.log("[HyperNotice]   method: ${m.name} (${m.parameterTypes.size} params)")
                }
                count++
            } catch (_: Exception) {
                XposedBridge.log("[HyperNotice] ✗ NOT_FOUND: $cn")
            }
        }

        XposedBridge.log("[HyperNotice] === Found $count / ${candidates.size} classes ===")

        // 对找到的类做通用 Hook，记录方法调用
        try {
            val fnc = XposedHelpers.findClass("miui.systemui.notification.focus.FocusNotificationController", lpparam.classLoader)
            hookMethodsAll(fnc, "FocusController")
        } catch (_: Exception) {}

        try {
            val dibcv = XposedHelpers.findClass("miui.systemui.dynamicisland.window.content.DynamicIslandBaseContentView", lpparam.classLoader)
            hookMethodsAll(dibcv, "DynamicIslandBaseContentView")
        } catch (_: Exception) {}
    }

    private fun hookMethodsAll(clz: Class<*>, tag: String) {
        for (m in clz.declaredMethods) {
            try {
                val paramTypes = m.parameterTypes
                val args = arrayOfNulls<Any>(1 + paramTypes.size)
                args[0] = clz
                args[1] = m.name
                for (i in paramTypes.indices) args[2 + i] = paramTypes[i]
                XposedHelpers.findAndHookMethod(clz, m.name, *paramTypes, object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        XposedBridge.log("[HyperNotice] CALLED: $tag.${m.name}")
                    }
                })
            } catch (_: Exception) {
                // skip methods that can't be hooked
            }
        }
        XposedBridge.log("[HyperNotice] ✓ Hooked ${clz.declaredMethods.size} methods on $tag")
    }
}
