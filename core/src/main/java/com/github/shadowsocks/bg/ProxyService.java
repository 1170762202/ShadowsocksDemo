package com.github.shadowsocks.bg;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * @author 陈志鹏
 * @date 2019/2/21
 */
public class ProxyService extends Service implements BaseService.Interface {

    public ProxyService(){
        BaseService.register(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Intrinsics.checkParameterIsNotNull(intent, "intent");
        return DefaultImpls.onBind(this, intent);
    }


    @Override
    public int onStartCommand(@org.jetbrains.annotations.Nullable Intent intent, int flags, int startId) {
        return DefaultImpls.onStartCommand(this, intent, flags, startId);
    }

    @NotNull
    @Override
    public String getTag() {
        return "ShadowsocksProxyService";
    }

    @Override
    public void forceLoad() {
        DefaultImpls.forceLoad(this);
    }

    @NotNull
    @Override
    public ArrayList buildAdditionalArguments(@NotNull ArrayList cmd) {
        Intrinsics.checkParameterIsNotNull(cmd, "cmd");
        return DefaultImpls.buildAdditionalArguments(this, cmd);
    }

    @Override
    public void startNativeProcesses() {
        DefaultImpls.startNativeProcesses(this);
    }

    @NotNull
    @Override
    public ServiceNotification createNotification(@NotNull String profileName) {
        return new ServiceNotification((BaseService.Interface)this, profileName, "service-proxy", true);
    }

    @Override
    public void startRunner() {
        DefaultImpls.startRunner(this);
    }

    @Override
    public void killProcesses() {
        DefaultImpls.killProcesses(this);
    }

    @Override
    public void stopRunner(boolean stopService, @org.jetbrains.annotations.Nullable String msg) {
        DefaultImpls.stopRunner(this, stopService, msg);
    }

    @NotNull
    @Override
    public BaseService.Data getData() {
        return DefaultImpls.getData(this);
    }
}
