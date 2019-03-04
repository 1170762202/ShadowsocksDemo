package com.mrd.news.vpn;

import android.app.Application;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatDelegate;

import com.github.shadowsocks.Core;

/**
 * @author 陈志鹏
 * @date 2019/2/21
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Core.init(this, VPNActivity.class);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Core.updateNotificationChannels();
    }
}
