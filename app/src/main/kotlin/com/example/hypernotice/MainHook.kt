package com.example.hypernotice

import android.os.Build
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.io.BufferedReader
import java.io.InputStreamReader

class MainHook : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != "com.android.systemui") return

        XposedBridge.log("[HyperNotice] === SystemUI loaded ===")
        XposedBridge.log("[HyperNotice] classLoader = ${lpparam.classLoader}")

        // 先打印所有跟 focus / island 相关的已加载类
        try {
            XposedBridge.log("[HyperNotice] === Searching Runtime Classes ===")
            var count = 0
            val seen = HashSet<String>()

            // 方法1：通过 ClassLoader 的 loadedClasses（如果可用）
            try {
                val classesField = ClassLoader::class.java.getDeclaredField("classes")
                classesField.isAccessible = true
                @Suppress("UNCHECKED_CAST")
                val classes = classesField.get(lpparam.classLoader) as? Vector<Class<*>>
                if (classes != null) {
                    for (clz in classes) {
                        val name = clz.name
                        if ((name.contains("focus", true) || name.contains("island", true) || name.contains("notif", true))
                            && name.startsWith("miui.") || name.startsWith("com.android.systemui")) {
                            if (name !in seen) {
                                seen.add(name)
                                XposedBridge.log("[HyperNotice] LOADED: $name")
                                count++
                            }
                        }
                    }
                }
            } catch (_: Exception) {}

            // 方法2：直接用自省方法
            if (count == 0) {
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
                    "com.android.systemui.statusbar.notification.policy.FakeFocusNotifControllerImpl"
                )
                for (cn in candidates) {
                    try {
                        val clz = XposedHelpers.findClass(cn, lpparam.classLoader)
                        XposedBridge.log("[HyperNotice] FOUND: $cn")
                        // 打印方法
                        for (m in clz.declaredMethods) {
                            XposedBridge.log("[HyperNotice]   method: ${m.name}")
                        }
                        count++
                    } catch (_: Exception) {
                        // not found, skip
                    }
                }
            }

            XposedBridge.log("[HyperNotice] === Found $count classes ===")

            // 如果找到 FocusNotificationController，hook 它
            try {
                val fnc = XposedHelpers.findClass("miui.systemui.notification.focus.FocusNotificationController", lpparam.classLoader)
                XposedBridge.log("[HyperNotice] ✓ Hook FocusNotificationController...")
                for (m in fnc.declaredMethods) {
                    XposedHelpers.findAndHookMethod(fnc, m.name, object : XposedHelpers.XHook() {
                        override fun beforeHookedMethod(param: XposedHelpers.MethodHookParam) {
                            XposedBridge.log("[HyperNotice] FocusController.${m.name} called")
                        }
                    })
                }
            } catch (_: Exception) {}

            // 如果找到 DynamicIslandBaseContentView，hook 它的 setCutoutY 和所有方法
            try {
                val dibcv = XposedHelpers.findClass("miui.systemui.dynamicisland.window.content.DynamicIslandBaseContentView", lpparam.classLoader)
                XposedBridge.log("[HyperNotice] ✓ Hook DynamicIslandBaseContentView...")
                for (m in dibcv.declaredMethods) {
                    if (m.name.contains("cutout", true) || m.name.contains("y", true) || m.name.contains("layout", true) || m.name.contains("measur", true)) {
                        try {
                            XposedHelpers.findAndHookMethod(dibcv, m.name, *m.parameterTypes, object : XposedHelpers.XHook() {
                                override fun beforeHookedMethod(param: XposedHelpers.MethodHookParam) {
                                    XposedBridge.log("[HyperNotice] DynamicIsland.${m.name} called")
                                }
                            })
                        } catch (_: Exception) {}
                    }
                }
            } catch (_: Exception) {}

        } catch (e: Exception) {
            XposedBridge.log("[HyperNotice] ERROR: ${e.message}")
            e.printStackTrace()
        }
    }
}
