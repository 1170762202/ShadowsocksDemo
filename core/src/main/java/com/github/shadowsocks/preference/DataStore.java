package com.github.shadowsocks.preference;

import android.os.Binder;
import android.support.v7.preference.PreferenceDataStore;

import com.github.shadowsocks.Core;
import com.github.shadowsocks.database.PrivateDatabase;
import com.github.shadowsocks.database.ProfileManager;
import com.github.shadowsocks.database.PublicDatabase;
import com.github.shadowsocks.utils.DirectBoot;
import com.github.shadowsocks.utils.Key;
import com.github.shadowsocks.utils.TcpFastOpen;
import com.github.shadowsocks.utils.UtilsKt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;

import kotlin.jvm.internal.Intrinsics;

/**
 * @author 陈志鹏
 * @date 2019/2/27
 */
public class DataStore implements OnPreferenceDataStoreChangeListener {
    @NotNull
    public static final RoomPreferenceDataStore publicStore;
    @NotNull
    public static final RoomPreferenceDataStore privateStore;
    public static int userIndex;
    public static boolean hasArc0;

    static {
        publicStore = new RoomPreferenceDataStore(PublicDatabase.getKvPairDao());
        privateStore = new RoomPreferenceDataStore(PrivateDatabase.getKvPairDao());
    }

    public DataStore(){
        publicStore.registerChangeListener(this);
        userIndex = Binder.getCallingUserHandle().hashCode();

        initHasArc0();
    }

    private void initHasArc0(){
        int retry = 0;
        while (retry < 5) {
            try {
                hasArc0 = NetworkInterface.getByName("arc0") != null;
            } catch (SocketException e) { }
            retry++;
            try {
                Thread.sleep(100L << retry);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        hasArc0 = false;
    }

    @NotNull
    public static String getListenAddress() {
        return publicStore.getBoolean(Key.shareOverLan, hasArc0) ? "0.0.0.0" : "127.0.0.1";
    }

    public static int getPortProxy() {
        return getLocalPort(Key.portProxy, 1080);
    }

    public static void setPortProxy(int value) {
        publicStore.putString(Key.portProxy, String.valueOf(value));
    }

    public static int getPortLocalDns() {
        return getLocalPort(Key.portLocalDns, 5450);
    }

    public static void setPortLocalDns(int value) {
        publicStore.putString(Key.portLocalDns, String.valueOf(value));
    }

    public static int getPortTransproxy() {
        return getLocalPort(Key.portTransproxy, 8200);
    }

    public static void setPortTransproxy(int value) {
        publicStore.putString(Key.portTransproxy, String.valueOf(value));
    }

    public static void initGlobal() {
        if (publicStore.getBoolean(Key.tfo) == null) {
            publicStore.putBoolean(Key.tfo, getTcpFastOpen());
        }
        if (publicStore.getString(Key.portProxy) == null) {
            setPortProxy(getPortProxy());
        }
        if (publicStore.getString(Key.portLocalDns) == null) {
            setPortLocalDns(getPortLocalDns());
        }
        if (publicStore.getString(Key.portTransproxy) == null) {
            setPortTransproxy(getPortTransproxy());
        }
    }

    @Override
    public void onPreferenceDataStoreChanged(@NotNull PreferenceDataStore store, @Nullable String key) {
        if (key == Key.id){
            if (DataStore.getDirectBootAware()) {
                try {
                    DirectBoot.update(ProfileManager.getProfile(DataStore.getProfileId()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static int getLocalPort(String key, int defaultint) {
        Integer value = publicStore.getInt(key);
        if (value != null) {
            publicStore.putString(key, String.valueOf(value));
            return value;
        } else {
            return UtilsKt.parsePort(publicStore.getString(key), defaultint + userIndex);
        }
    }

    public static long getProfileId() {
        Long profileId = publicStore.getLong(Key.id);
        return profileId != null ? profileId : 0L;
    }

    public static void setProfileId(long value) {
        publicStore.putLong(Key.id, value);
    }

    public static boolean getCanToggleLocked() {
        return Intrinsics.areEqual(publicStore.getBoolean(Key.directBootAware), true);
    }

    public static boolean getDirectBootAware() {
        return Core.getDirectBootSupported() && getCanToggleLocked();
    }

    public static boolean getTcpFastOpen() {
        return TcpFastOpen.getSendEnabled() && publicStore.getBoolean(Key.tfo, true);
    }

    @NotNull
    public static String getServiceMode() {
        String serviceMode = publicStore.getString(Key.serviceMode);
        if (serviceMode == null) {
            serviceMode = Key.modeVpn;
        }
        return serviceMode;
    }

    @Nullable
    public static Long getEditingId() {
        return privateStore.getLong(Key.id);
    }

    public static void setEditingId(@Nullable Long value) {
        privateStore.putLong(Key.id, value);
    }

    public static boolean getProxyApps() {
        Boolean var10000 = privateStore.getBoolean(Key.proxyApps);
        return var10000 != null ? var10000 : false;
    }

    public static void setProxyApps(boolean value) {
        privateStore.putBoolean(Key.proxyApps, value);
    }

    public static boolean getBypass() {
        Boolean var10000 = privateStore.getBoolean(Key.bypass);
        return var10000 != null ? var10000 : false;
    }

    public static void setBypass(boolean value) {
        privateStore.putBoolean(Key.bypass, value);
    }

    @NotNull
    public static String getIndividual() {
        String var10000 = privateStore.getString(Key.individual);
        if (var10000 == null) {
            var10000 = "";
        }

        return var10000;
    }

    public static void setIndividual(@NotNull String value) {
        privateStore.putString(Key.individual, value);
    }

    @NotNull
    public static String getPlugin() {
        String var10000 = privateStore.getString(Key.plugin);
        if (var10000 == null) {
            var10000 = "";
        }

        return var10000;
    }

    public static void setPlugin(@NotNull String value) {
        privateStore.putString(Key.plugin, value);
    }

    @Nullable
    public static Long getUdpFallback() {
        return privateStore.getLong(Key.udpFallback);
    }

    public static void setUdpFallback(@Nullable Long value) {
        privateStore.putLong(Key.udpFallback, value);
    }

    public static boolean getDirty() {
        Boolean var10000 = privateStore.getBoolean(Key.dirty);
        return var10000 != null ? var10000 : false;
    }

    public static void setDirty(boolean value) {
        privateStore.putBoolean(Key.dirty, value);
    }
}
