package com.github.shadowsocks.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import org.jetbrains.annotations.NotNull;

/**
 * @author 陈志鹏
 * @date 2019/2/21
 */
@SuppressLint({"Registered"})
@TargetApi(24)
public final class DeviceStorageApp extends Application {

    @Override
    public Context getApplicationContext() {
        return this;
    }

    public DeviceStorageApp(@NotNull Context context) {
        this.attachBaseContext(context.createDeviceProtectedStorageContext());
    }
}
