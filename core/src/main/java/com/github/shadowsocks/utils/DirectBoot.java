package com.github.shadowsocks.utils;

import android.content.*;
import com.github.shadowsocks.Core;
import com.github.shadowsocks.database.Profile;
import com.github.shadowsocks.database.ProfileManager;
import com.github.shadowsocks.preference.DataStore;
import kotlin.Pair;
import org.jetbrains.annotations.Nullable;

import java.io.*;

/**
 * @author 陈志鹏
 * @date 2019/2/22
 */
public class DirectBoot extends BroadcastReceiver {

    private static boolean registered = false;

    public static File getFile() {
        return new File(Core.getDeviceStorage().getNoBackupFilesDir(), "directBootProfile");
    }

    @Nullable
    public static Pair<Profile, Profile> getDeviceProfile() {
        try {
            FileInputStream inputStream = new FileInputStream(getFile());
            ObjectInputStream stream = new ObjectInputStream(inputStream);
            Object object = stream.readObject();
            if (!(object instanceof Pair)) {
                object = null;
            }

            return (Pair<Profile, Profile>) object;
        } catch (Throwable var12) {
            var12.printStackTrace();
            return null;
        }
    }

    public static void clean() {
        getFile().delete();
        (new File(Core.getDeviceStorage().getNoBackupFilesDir(), "shadowsocks.conf")).delete();
        (new File(Core.getDeviceStorage().getNoBackupFilesDir(), "shadowsocks-udp.conf")).delete();
    }

    public static void update(@Nullable Profile profile) {
        if (profile == null) {
            clean();
        } else {
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(getFile());
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(ProfileManager.expand(profile));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void flushTrafficStats() {
        try {
            Pair pair = getDeviceProfile();
            if (pair != null) {
                Profile profile = (Profile) pair.component1();
                Profile fallback = (Profile) pair.component2();
                if (profile.isDirty()) {
                    ProfileManager.updateProfile(profile);
                }

                if (fallback != null) {
                    if (fallback.isDirty()) {
                        ProfileManager.updateProfile(fallback);
                    }
                }
            }
            update(ProfileManager.getProfile(DataStore.getProfileId()));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void listenForUnlock() {
        if (!registered) {
            Core.app.registerReceiver(new DirectBoot(), new IntentFilter(Intent.ACTION_BOOT_COMPLETED));
            registered = true;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        flushTrafficStats();
        Core.app.unregisterReceiver(this);
        registered = false;
    }
}
