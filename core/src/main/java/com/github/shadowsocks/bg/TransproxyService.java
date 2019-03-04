package com.github.shadowsocks.bg;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;
import com.github.shadowsocks.Core;
import com.github.shadowsocks.preference.DataStore;
import kotlin.TypeCastException;
import kotlin.collections.CollectionsKt;
import kotlin.io.FilesKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.Charsets;
import kotlin.text.StringsKt;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author 陈志鹏
 * @date 2019/2/21
 */
public class TransproxyService extends Service implements LocalDnsService.Interface {

    public TransproxyService(){
        BaseService.register(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return DefaultImpls.onBind(this, intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return DefaultImpls.onStartCommand(this, intent, flags, startId);
    }

    private void startDNSTunnel() {
        ProxyInstance proxy = this.getData().getProxy();
        if (proxy == null) {
            Intrinsics.throwNpe();
        }

        String[] var11 = new String[]{(new File(this.getApplicationInfo().nativeLibraryDir, Executable.SS_TUNNEL)).getAbsolutePath(), "-t", "10",
                "-b", DataStore.getListenAddress(),
                "-u",
                "-l", String.valueOf(DataStore.getPortLocalDns()),
                "-L", null, null, null};

        StringBuilder var10003 = new StringBuilder();
        String[] split = proxy.getProfile().getRemoteDns().split(",");
        String var3 = (String) CollectionsKt.first(Arrays.asList(split));
        StringBuilder var8 = var10003;
        byte var7 = 9;
        String[] var6 = var11;
        String[] var5 = var11;
        if (var3 == null) {
            throw new TypeCastException("null cannot be cast to non-null type kotlin.CharSequence");
        } else {
            String var9 = StringsKt.trim((CharSequence)var3).toString();
            var6[var7] = var8.append(var9).append(":53").toString();
            var5[10] = "-c";
            File var12 = proxy.getConfigFile();
            if (var12 == null) {
                Intrinsics.throwNpe();
            }

            var5[11] = var12.getAbsolutePath();
            ArrayList cmd = CollectionsKt.arrayListOf(var5);
            if (DataStore.getTcpFastOpen()) {
                Collection var10 = (Collection)cmd;
                String var4 = "--fast-open";
                var10.add(var4);
            }
            this.getData().getProcesses().start(cmd,null);
        }
    }

    private final void startRedsocksDaemon() {
        FilesKt.writeText(new File(Core.getDeviceStorage().getNoBackupFilesDir(), "redsocks.conf"), "base {\n log_debug = off;\n log_info = off;\n log = stderr;\n daemon = off;\n redirector = iptables;\n}\nredsocks {\n local_ip = " + DataStore.getListenAddress() + ";\n local_port = " + DataStore.getPortTransproxy() + ";\n ip = 127.0.0.1;\n port = " + DataStore.getPortProxy() + ";\n type = socks5;\n}\n", Charsets.UTF_8);
        getData().getProcesses().start(CollectionsKt.listOf(new String[]{(new File(this.getApplicationInfo().nativeLibraryDir, "libredsocks.so")).getAbsolutePath(), "-c", "redsocks.conf"}),null);
    }

    @Override
    public void startNativeProcesses() {
        this.startRedsocksDaemon();
        DefaultImpls.startNativeProcesses(this);
        ProxyInstance var10000 = this.getData().getProxy();
        if (var10000 == null) {
            Intrinsics.throwNpe();
        }

        if (var10000.getProfile().isUdpdns()) {
            this.startDNSTunnel();
        }
    }

    @NotNull
    @Override
    public String getTag() {
        return "ShadowsocksTransproxyService";
    }

    @Override
    public void forceLoad() {
        DefaultImpls.forceLoad(this);
    }

    @NotNull
    @Override
    public ArrayList<String> buildAdditionalArguments(@NotNull ArrayList<String> cmd) {
        Intrinsics.checkParameterIsNotNull(cmd, "cmd");
        return DefaultImpls.buildAdditionalArguments(this, cmd);
    }

    @NotNull
    @Override
    public ServiceNotification createNotification(@NotNull String profileName) {
        Intrinsics.checkParameterIsNotNull(profileName, "profileName");
        return new ServiceNotification(this, profileName, "service-transproxy", true);
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
