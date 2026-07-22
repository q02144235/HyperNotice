package com.example.hypernotice

import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.PackageLoadedParam
import android.util.Log

class HyperNoticeModule : XposedModule() {

    override fun onPackageLoaded(param: PackageLoadedParam) {
        if (param.packageName != "com.android.systemui") return
        log(Log.INFO, "HyperNotice", "=== SystemUI loaded ===")
    }
}
