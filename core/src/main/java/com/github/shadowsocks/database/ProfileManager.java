package com.github.shadowsocks.database;

import android.database.sqlite.SQLiteCantOpenDatabaseException;
import com.github.shadowsocks.Core;
import com.github.shadowsocks.preference.DataStore;
import com.github.shadowsocks.utils.DirectBoot;
import kotlin.Pair;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

/**
 * @author 陈志鹏
 * @date 2019/2/27
 */
public class ProfileManager {
    @Nullable
    public static ProfileManager.Listener listener;

    @NotNull
    public static Profile createProfile(){
        return createProfile(new Profile());
    }

    @NotNull
    public static Profile createProfile(@NotNull Profile profile){
        profile.setId(0L);
        Long var10001 = PrivateDatabase.getProfileDao().nextOrder();
        profile.setUserOrder(var10001 != null ? var10001 : 0L);
        profile.setId(PrivateDatabase.getProfileDao().create(profile));
        if (listener != null) {
            listener.onAdd(profile);
        }
        return profile;
    }

    public static void updateProfile(@NotNull Profile profile) throws SQLException {
        boolean var2 = PrivateDatabase.getProfileDao().update(profile) == 1;
        if (!var2) {
            String var3 = "Check failed.";
            throw new IllegalStateException(var3);
        }
    }

    public static boolean containProfile(@NotNull String host) throws SQLException, IOException {
        List allProfiles = getAllProfiles();
        if (allProfiles == null) {
            Intrinsics.throwNpe();
        }

        Iterator iterator = allProfiles.iterator();

        Profile item;
        do {
            if (!iterator.hasNext()) {
                return false;
            }

            item = (Profile)iterator.next();
        } while(!host.equals(item.getHost()));

        return true;
    }

    @Nullable
    public static Profile getProfile(long id) throws IOException {
        Profile var3;
        try {
            var3 = PrivateDatabase.getProfileDao().get(id);
        } catch (SQLiteCantOpenDatabaseException var5) {
            throw new IOException(var5);
        }
        return var3;
    }

    @NotNull
    public static Pair<Profile, Profile> expand(@NotNull Profile profile) throws IOException {
        Long var10001 = profile.getUdpFallback();
        Profile var12;
        if (var10001 != null) {
            Long var2 = var10001;
            long it = ((Number)var2).longValue();
            Profile var9 = getProfile(it);
            var12 = var9;
        } else {
            var12 = null;
        }

        Profile var10 = var12;
        return new Pair(profile, var10);
    }

    public static void delProfile(long id) throws SQLException {
        boolean var3 = PrivateDatabase.getProfileDao().delete(id) == 1;
        if (!var3) {
            String var4 = "Check failed.";
            throw new IllegalStateException(var4);
        } else {
            ProfileManager.Listener var10000 = listener;
            if (listener != null) {
                var10000.onRemove(id);
            }

            if (Core.getActiveProfileIds().contains(id) && DataStore.getDirectBootAware()) {
                DirectBoot.clean();
            }
        }
    }

    public static int clear() throws SQLException {
        int var1 = PrivateDatabase.getProfileDao().deleteAll();
        DirectBoot.clean();
        return var1;
    }

    public static void ensureNotEmpty() throws IOException, SQLException {
        boolean nonEmpty = false;
        try {
            nonEmpty = PrivateDatabase.getProfileDao().isNotEmpty();
        } catch (SQLiteCantOpenDatabaseException var4) {
            throw new IOException(var4);
        }

        if (!nonEmpty) {
            DataStore.setProfileId(createProfile().getId());
        }
    }

    @Nullable
    public static List<Profile> getAllProfiles() throws IOException {
        List var1;
        try {
            var1 = PrivateDatabase.getProfileDao().list();
        } catch (SQLiteCantOpenDatabaseException var3) {
            throw new IOException(var3);
        }

        return var1;
    }

    public interface Listener {
        void onAdd(@NotNull Profile var1);

        void onRemove(long var1);
    }
}
