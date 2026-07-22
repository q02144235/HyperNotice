package com.example.hypernotice

import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.PackageLoadedParam

class HyperNoticeModule : XposedModule() {

    override fun onPackageLoaded(param: PackageLoadedParam) {
        if (param.packageName != "com.android.systemui") return
        log("[HyperNotice] === SystemUI loaded ===")
    }
}
