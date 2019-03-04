package com.github.shadowsocks;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.UserManager;
import android.support.v4.content.ContextCompat;

import com.github.shadowsocks.acl.Acl;
import com.github.shadowsocks.bg.BaseService;
import com.github.shadowsocks.core.R;
import com.github.shadowsocks.database.Profile;
import com.github.shadowsocks.database.ProfileManager;
import com.github.shadowsocks.preference.DataStore;
import com.github.shadowsocks.utils.Action;
import com.github.shadowsocks.utils.DeviceStorageApp;
import com.github.shadowsocks.utils.DirectBoot;
import com.github.shadowsocks.utils.Key;
import com.github.shadowsocks.utils.TcpFastOpen;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.work.Configuration;
import androidx.work.WorkManager;
import kotlin.Pair;
import kotlin.collections.CollectionsKt;
import kotlin.io.ByteStreamsKt;
import kotlin.io.FilesKt;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.Charsets;


/**
 * @author 陈志鹏
 * @date 2019/2/27
 */
public class Core {
    @NotNull
    public static Application app;
    @NotNull
    public static Function1<Context, PendingIntent> configureIntent;
    public static Handler handler = new Handler(Looper.getMainLooper());
    public static int currentState = BaseService.IDLE;

    public static PackageInfo getPackageInfo() {
        return getPackageInfo(app.getPackageName());
    }

    @NotNull
    public static PackageInfo getPackageInfo(@NotNull String packageName) {
        Application var10000 = app;
        if (app == null) {
            Intrinsics.throwUninitializedPropertyAccessException("app");
        }

        PackageInfo var2 = null;
        try {
            var2 = var10000.getPackageManager().getPackageInfo(packageName, Build.VERSION.SDK_INT >= 28 ? PackageManager.GET_SIGNING_CERTIFICATES : PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (var2 == null) {
            Intrinsics.throwNpe();
        }

        return var2;
    }

    @NotNull
    public static Application getDeviceStorage() {
        if (Build.VERSION.SDK_INT < 24) {
            return app;
        } else {
            return new DeviceStorageApp(app);
        }
    }

    public static boolean getDirectBootSupported() {
        if (Build.VERSION.SDK_INT >= 24) {
            Context context = Core.app;
            DevicePolicyManager manager = ContextCompat.getSystemService(context, DevicePolicyManager.class);
            if (manager != null) {
                if (manager.getStorageEncryptionStatus() == DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_PER_USER) {
                    return true;
                }
            }
        }
        return false;
    }

    @NotNull
    public static List<Long> getActiveProfileIds() {
        Profile var1 = null;
        try {
            var1 = ProfileManager.getProfile(DataStore.getProfileId());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return var1 == null ? new ArrayList<Long>() : CollectionsKt.listOfNotNull(new Long[]{var1.getId(), var1.getUdpFallback()});
    }

    @Nullable
    public static Pair<Profile, Profile> getCurrentProfile() {
        if (DataStore.getDirectBootAware()) {
            Pair var10000 = DirectBoot.getDeviceProfile();
            if (var10000 != null) {
                return var10000;
            }
        }
        try {
            Profile var4 = ProfileManager.getProfile(DataStore.getProfileId());
            return var4 != null ? ProfileManager.expand(var4) : null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @NotNull
    public static Profile switchProfile(long id) {
        Profile result = null;
        try {
            result = ProfileManager.getProfile(id);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (result == null) {
            result = ProfileManager.createProfile();
        }
        DataStore.setProfileId(result.getId());
        return result;
    }

    public static void init(@NotNull Application app, @NotNull final Class configureClass) {
        Core.app = app;
        configureIntent = new Function1() {
            @Override
            public Object invoke(Object var1) {
                Context context = (Context) var1;
                return PendingIntent.getActivity(context, 0,
                        new Intent(context, configureClass).setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT), 0);
            }
        };
        if (Build.VERSION.SDK_INT >= 24) {  // migrate old files
            getDeviceStorage().moveDatabaseFrom(app, Key.DB_PUBLIC);
            File old = Acl.getFile(Acl.CUSTOM_RULES, app);
            if (old.canRead()) {
                FilesKt.writeText(Acl.getFile(Acl.CUSTOM_RULES), FilesKt.readText(old, Charsets.UTF_8), Charsets.UTF_8);
                old.delete();
            }
        }
        WorkManager.initialize(getDeviceStorage(), (new Configuration.Builder()).build());
        if (Build.VERSION.SDK_INT >= 24 && DataStore.getDirectBootAware()) {
            UserManager var39 = ContextCompat.getSystemService(app, UserManager.class);
            if (var39 != null) {
                if (var39.isUserUnlocked()) {
                    DirectBoot.flushTrafficStats();
                }
            }
        }
        if (DataStore.getTcpFastOpen() && !TcpFastOpen.getSendEnabled()) {
            try {
                TcpFastOpen.enableAsync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (DataStore.publicStore.getLong(Key.assetUpdateTime, -1) != getPackageInfo().lastUpdateTime) {
            AssetManager assetManager = app.getAssets();
            for (int i = 0; i < 2; i++) {
                String dir = i == 0 ? "acl" : "overture";
                try {
                    String[] var40 = assetManager.list(dir);
                    if (var40 == null) {
                        Intrinsics.throwNpe();
                    }

                    for (int var9 = 0; var9 < var40.length; ++var9) {
                        String file = var40[var9];
                        InputStream input = assetManager.open(dir + '/' + file);
                        try {
                            File var17 = new File(getDeviceStorage().getNoBackupFilesDir(), file);
                            FileOutputStream output = new FileOutputStream(var17);

                            ByteStreamsKt.copyTo(input, output, 8 * 1024);
                        } catch (Throwable var34) {
                            throw var34;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            DataStore.publicStore.putLong(Key.assetUpdateTime, getPackageInfo().lastUpdateTime);
        }
        updateNotificationChannels();
    }

    public static void updateNotificationChannels() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager nm = app.getSystemService(NotificationManager.class);
            List<NotificationChannel> channels = new ArrayList<>();
            channels.add(new NotificationChannel(
                    "service-vpn", app.getText(R.string.service_vpn),
                    NotificationManager.IMPORTANCE_LOW));
            channels.add(new NotificationChannel(
                    "service-proxy", app.getText(R.string.service_proxy),
                    NotificationManager.IMPORTANCE_LOW));
            channels.add(new NotificationChannel(
                    "service-transproxy", app.getText(R.string.service_transproxy),
                    NotificationManager.IMPORTANCE_LOW));

            nm.createNotificationChannels(channels);
            nm.deleteNotificationChannel("service-nat"); // NAT mode is gone for good
        }
    }

    public static void startService() {
        ContextCompat.startForegroundService(app, new Intent(app, BaseService.getServiceClass()));
    }

    public static void reloadService() {
        app.sendBroadcast(new Intent(Action.RELOAD));
    }

    public static void stopService() {
        app.sendBroadcast(new Intent(Action.CLOSE));
    }

    @NotNull
    public static BroadcastReceiver listenForPackageChanges(final boolean onetime, @NotNull final Function0 callback) {
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        BroadcastReceiver result = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
                    return;
                }
                callback.invoke();
                if (onetime) {
                    app.unregisterReceiver(this);
                }
            }
        };
        app.registerReceiver(result, filter);
        return result;
    }
}
