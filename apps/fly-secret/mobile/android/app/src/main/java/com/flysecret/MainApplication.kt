package com.flysecret

import android.app.Application
import com.facebook.react.PackageList
import com.facebook.react.ReactApplication
import com.facebook.react.ReactHost
import com.facebook.react.ReactNativeApplicationEntryPoint.loadReactNative
import com.facebook.react.defaults.DefaultReactHost.getDefaultReactHost

class MainApplication : Application(), ReactApplication {
    override val reactHost: ReactHost
        get() = getDefaultReactHost(
            context = applicationContext,
            packageList = PackageList(this).packages
        )

    override fun onCreate() {
        super.onCreate()
        loadReactNative(this)
    }
}