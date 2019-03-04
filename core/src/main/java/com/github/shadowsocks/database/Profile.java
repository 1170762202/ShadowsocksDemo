package com.github.shadowsocks.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.github.shadowsocks.plugin.PluginConfiguration;
import com.github.shadowsocks.plugin.PluginOptions;
import com.github.shadowsocks.preference.DataStore;
import com.github.shadowsocks.utils.IteratorUtils;
import com.github.shadowsocks.utils.Key;
import com.github.shadowsocks.utils.UtilsKt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kotlin.TypeCastException;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.sequences.Sequence;
import kotlin.sequences.SequencesKt;
import kotlin.text.Charsets;
import kotlin.text.MatchResult;
import kotlin.text.Regex;
import kotlin.text.StringsKt;

/**
 * @author 陈志鹏
 * @date 2019/2/28
 */
@Entity
public class Profile implements Serializable {
    private static final String TAG = "ShadowParser";
    private static final long serialVersionUID = 0L;
    private static final Regex pattern;
    private static final Regex userInfoPattern;
    private static final Regex legacyPattern;

    static {
        String var0 = "(?i)ss://[-a-zA-Z0-9+&@#/%?=.~*'()|!:,;\\[\\]]*[-a-zA-Z0-9+&@#/%=.~*'()|\\[\\]]";
        pattern = new Regex(var0);
        var0 = "^(.+?):(.*)$";
        userInfoPattern = new Regex(var0);
        var0 = "^(.+?):(.*)@(.+?):(\\d+?)$";
        legacyPattern = new Regex(var0);
    }

    @NotNull
    public static Sequence findAllUrls(@Nullable CharSequence data, @Nullable final Profile feature) {
        if (data == null) {
            data = "";
        }
        return SequencesKt.filterNotNull(SequencesKt.map(Profile.pattern.findAll(data, 0), new Function1() {
            // $FF: synthetic method
            // $FF: bridge method
            @Override
            public Object invoke(Object var1) {
                return this.invoke((MatchResult) var1);
            }

            @Nullable
            public Profile invoke(@NotNull MatchResult it) {
                Uri uri = Uri.parse(it.getValue());
                try {
                    if (uri.getUserInfo() == null) {
                        MatchResult match = Profile.legacyPattern.matchEntire(new String(Base64.decode(uri.getHost(), Base64.NO_PADDING)));
                        if (match != null) {
                            Profile profile = new Profile();
                            if (feature != null) {
                                feature.copyFeatureSettingsTo(profile);
                            }
                            profile.setMethod(match.getGroupValues().get(1).toLowerCase());
                            profile.setPassword(match.getGroupValues().get(2));
                            profile.setHost(match.getGroupValues().get(3));
                            profile.setRemotePort(Integer.parseInt(match.getGroupValues().get(4)));
                            profile.setPlugin(uri.getQueryParameter(Key.plugin));
                            profile.setName(uri.getFragment());
                            return profile;
                        } else {
                            Log.e(TAG, "Unrecognized URI: " + it.getValue());
                            return null;
                        }
                    } else {
                        String str = new String(Base64.decode(uri.getUserInfo(), 11), Charsets.UTF_8);
                        MatchResult match = Profile.userInfoPattern.matchEntire(str);
                        if (match != null) {
                            Profile profile = new Profile();
                            if (feature != null) {
                                feature.copyFeatureSettingsTo(profile);
                            }

                            profile.setMethod(match.getGroupValues().get(1));
                            profile.setPassword(match.getGroupValues().get(2));

                            try {
                                URI javaURI = new URI(it.getValue());
                                String var22 = javaURI.getHost();
                                if (var22 == null) {
                                    var22 = "";
                                }

                                profile.setHost(var22);
                                Character var23 = StringsKt.firstOrNull((CharSequence) profile.getHost());
                                if (var23 != null) {
                                    if (var23 == '[') {
                                        var23 = StringsKt.lastOrNull((CharSequence) profile.getHost());
                                        if (var23 != null) {
                                            if (var23 == ']') {
                                                String var6 = profile.getHost();
                                                if (var6 == null) {
                                                    throw new TypeCastException("null cannot be cast to non-null type java.lang.String");
                                                }

                                                String host = var6.substring(1, profile.getHost().length() - 1);
                                                profile.setHost(host);
                                            }
                                        }
                                    }
                                }
                                profile.setRemotePort(javaURI.getPort());
                                profile.setPlugin(uri.getQueryParameter(Key.plugin));
                                var22 = uri.getFragment();
                                if (var22 == null) {
                                    var22 = "";
                                }

                                profile.setName(var22);
                                return profile;
                            } catch (URISyntaxException var11) {
                                Log.e("ShadowParser", "Invalid URI: " + it.getValue());
                                return null;
                            }
                        } else {
                            Log.e("ShadowParser", "Unknown user info: " + it.getValue());
                            return null;
                        }
                    }
                } catch (IllegalArgumentException var12) {
                    Log.e("ShadowParser", "Invalid base64 detected: " + it.getValue());
                    return null;
                }
            }
        }));
    }

    private static class JsonParser extends ArrayList<Profile> {
        private final LinkedHashMap fallbackMap;
        private final Profile feature;

        public JsonParser(Profile feature) {
            this.feature = feature;
            fallbackMap = new LinkedHashMap();
        }

        private Profile tryParse(JSONObject json, boolean fallback) {
            String host = json.optString("server");
            if (TextUtils.isEmpty(host)) {
                return null;
            }
            int remotePort = json.optInt("server_port");
            if (remotePort <= 0) {
                return null;
            }
            String password = json.optString("password");
            if (TextUtils.isEmpty(password)) {
                return null;
            }
            String method = json.optString("method");
            if (TextUtils.isEmpty(method)) {
                return null;
            }
            Profile profile = new Profile();
            profile.setHost(host);
            profile.setRemotePort(remotePort);
            profile.setPassword(password);
            profile.setMethod(method);
            if (this.feature != null) {
                feature.copyFeatureSettingsTo(profile);
            }
            String id = json.optString("plugin");
            if (!TextUtils.isEmpty(id)) {
                profile.setPlugin((new PluginOptions(id, json.optString("plugin_opts"))).toString(false));
            }
            profile.setName(json.optString("remarks"));
            profile.setRoute(json.optString("route", profile.getRoute()));
            if (fallback) {
                return profile;
            }
            profile.setRemoteDns(json.optString("remote_dns", profile.getRemoteDns()));
            profile.setIpv6(json.optBoolean("ipv6", profile.isIpv6()));
            JSONObject var24 = json.optJSONObject("proxy_apps");
            if (var24 != null) {
                profile.setProxyApps(var24.optBoolean("enabled", profile.isProxyApps()));
                profile.setBypass(var24.optBoolean("bypass", profile.isBypass()));
                String oldIndividual = null;
                JSONArray android_list = var24.optJSONArray("android_list");
                for (int i = 0; i < android_list.length(); i++) {
                    try {
                        oldIndividual = Intrinsics.stringPlus(oldIndividual, android_list.get(i));
                        if (i != android_list.length() - 1) {
                            oldIndividual = Intrinsics.stringPlus(oldIndividual, "\n");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                profile.setIndividual(oldIndividual == null ? profile.getIndividual() : oldIndividual);
            }
            profile.setUdpdns(json.optBoolean("udpdns", profile.isUdpdns()));
            var24 = json.optJSONObject("udp_fallback");
            if (var24 != null) {
                Profile newProfile = tryParse(var24, true);
                if (newProfile != null) {
                    fallbackMap.put(profile, newProfile);
                }
            }
            return profile;
        }

        public void process(@NotNull Object json) {
            if (json instanceof JSONObject) {
                Profile profile = tryParse((JSONObject) json, false);
                if (profile != null) {
                    add(profile);
                } else {
                    Iterator var5 = ((JSONObject) json).keys();
                    while (var5.hasNext()) {
                        String key = (String) var5.next();
                        Object var10001 = null;
                        try {
                            var10001 = ((JSONObject) json).get(key);
                            this.process(var10001);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else if (json instanceof JSONArray) {
                Iterator iterator = IteratorUtils.asIterable((JSONArray) json);
                while (iterator.hasNext()) {
                    Object object = iterator.next();
                    process(object);
                }
            }
        }

        public final void finalize(@NotNull Function1 create) {
            List<Profile> profiles = null;
            try {
                List<Profile> allProfiles = ProfileManager.getAllProfiles();
                if (allProfiles == null) {
                    profiles = new ArrayList<>();
                } else {
                    profiles = allProfiles;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            Iterator var4 = fallbackMap.entrySet().iterator();
            while (var4.hasNext()) {
                Map.Entry var3 = (Map.Entry) var4.next();
                Profile profile = (Profile) var3.getKey();
                Profile fallback = (Profile) var3.getValue();

                Iterator var9 = profiles.iterator();
                Object var17;
                while (true) {
                    if (!var9.hasNext()) {
                        var17 = null;
                        break;
                    }

                    Object element$iv;
                    boolean var16;
                    label50:
                    {
                        element$iv = var9.next();
                        Profile it = (Profile) element$iv;
                        if (Intrinsics.areEqual(fallback.getHost(), it.getHost()) && fallback.getRemotePort() == it.getRemotePort() && Intrinsics.areEqual(fallback.getPassword(), it.getPassword()) && Intrinsics.areEqual(fallback.getMethod(), it.getMethod())) {
                            CharSequence var13 = (CharSequence) it.getPlugin();
                            if (var13 == null || var13.length() == 0) {
                                var16 = true;
                                break label50;
                            }
                        }

                        var16 = false;
                    }

                    if (var16) {
                        var17 = element$iv;
                        break;
                    }
                }

                Profile match = (Profile) var17;
                Long var10001;
                if (match == null) {
                    create.invoke(fallback);
                    var10001 = fallback.getId();
                } else {
                    var10001 = match.getId();
                }

                profile.setUdpFallback(var10001);
                try {
                    ProfileManager.updateProfile(profile);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void parseJson(@NotNull String json, @Nullable Profile feature, @NotNull Function1 create) {
        JsonParser var4 = new JsonParser(feature);
        Object var10001 = null;
        try {
            var10001 = (new JSONTokener(json)).nextValue();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        var4.process(var10001);
        Iterator var7 = var4.iterator();

        while (var7.hasNext()) {
            Profile profile = (Profile) var7.next();
            create.invoke(profile);
        }

        var4.finalize(create);
    }

    @android.arch.persistence.room.Dao
    public interface Dao {
        @Query("SELECT * FROM `Profile` WHERE `id` = :id")
        @Nullable
        Profile get(long id);

        @Query("SELECT * FROM `Profile` ORDER BY `userOrder`")
        @NotNull
        List<Profile> list();

        @Query("SELECT MAX(`userOrder`) + 1 FROM `Profile`")
        @Nullable
        Long nextOrder();

        @Query("SELECT 1 FROM `Profile` LIMIT 1")
        boolean isNotEmpty();

        @Insert
        long create(@NotNull Profile value);

        @Update
        int update(@NotNull Profile value);

        @Query("DELETE FROM `Profile` WHERE `id` = :id")
        int delete(long id);

        @Query("DELETE FROM `Profile`")
        int deleteAll();
    }

    @PrimaryKey(
            autoGenerate = true
    )
    private long id = 0;
    @Nullable
    private String name = "";
    @NotNull
    private String host = "198.199.101.152";
    private int remotePort = 8388;
    @NotNull
    private String password = "u1rRWTssNv0p";
    @NotNull
    private String method = "aes-256-cfb";
    @NotNull
    private String route = "all";
    @NotNull
    private String remoteDns = "8.8.8.8";
    private boolean proxyApps = false;
    private boolean bypass = false;
    private boolean udpdns = false;
    private boolean ipv6 = true;
    @NotNull
    private String individual = "";
    private long tx = 0;
    private long rx = 0;
    private long userOrder = 0;
    @Nullable
    private String plugin = null;
    @Nullable
    private Long udpFallback = null;
    @Ignore
    private boolean dirty = false;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    @NotNull
    public String getHost() {
        return host;
    }

    public void setHost(@NotNull String host) {
        this.host = host;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    @NotNull
    public String getPassword() {
        return password;
    }

    public void setPassword(@NotNull String password) {
        this.password = password;
    }

    @NotNull
    public String getMethod() {
        return method;
    }

    public void setMethod(@NotNull String method) {
        this.method = method;
    }

    @NotNull
    public String getRoute() {
        return route;
    }

    public void setRoute(@NotNull String route) {
        this.route = route;
    }

    @NotNull
    public String getRemoteDns() {
        return remoteDns;
    }

    public void setRemoteDns(@NotNull String remoteDns) {
        this.remoteDns = remoteDns;
    }

    public boolean isProxyApps() {
        return proxyApps;
    }

    public void setProxyApps(boolean proxyApps) {
        this.proxyApps = proxyApps;
    }

    public boolean isBypass() {
        return bypass;
    }

    public void setBypass(boolean bypass) {
        this.bypass = bypass;
    }

    public boolean isUdpdns() {
        return udpdns;
    }

    public void setUdpdns(boolean udpdns) {
        this.udpdns = udpdns;
    }

    public boolean isIpv6() {
        return ipv6;
    }

    public void setIpv6(boolean ipv6) {
        this.ipv6 = ipv6;
    }

    @NotNull
    public String getIndividual() {
        return individual;
    }

    public void setIndividual(@NotNull String individual) {
        this.individual = individual;
    }

    public long getTx() {
        return tx;
    }

    public void setTx(long tx) {
        this.tx = tx;
    }

    public long getRx() {
        return rx;
    }

    public void setRx(long rx) {
        this.rx = rx;
    }

    public long getUserOrder() {
        return userOrder;
    }

    public void setUserOrder(long userOrder) {
        this.userOrder = userOrder;
    }

    @Nullable
    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(@Nullable String plugin) {
        this.plugin = plugin;
    }

    @Nullable
    public Long getUdpFallback() {
        return udpFallback;
    }

    public void setUdpFallback(@Nullable Long udpFallback) {
        this.udpFallback = udpFallback;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    @NotNull
    public final String getFormattedAddress() {
        String var1 = StringsKt.contains((CharSequence) this.host, (CharSequence) ":", false) ? "[%s]:%d" : "%s:%d";
        Object[] var2 = new Object[]{this.host, this.remotePort};
        String var10000 = String.format(var1, var2);
        return var10000;
    }

    @NotNull
    public final String getFormattedName() {
        String var10000;
        if (TextUtils.isEmpty(name)) {
            var10000 = this.getFormattedAddress();
        } else {
            var10000 = this.name;
            if (this.name == null) {
                Intrinsics.throwNpe();
            }
        }
        return var10000;
    }

    public void copyFeatureSettingsTo(@NotNull Profile profile) {
        profile.route = this.route;
        profile.ipv6 = this.ipv6;
        profile.proxyApps = this.proxyApps;
        profile.bypass = this.bypass;
        profile.individual = this.individual;
        profile.udpdns = this.udpdns;
    }

    @NotNull
    public Uri toUri() {
        Uri.Builder builder = (new Uri.Builder()).scheme("ss");
        Object[] var19 = new Object[3];
        String var17 = String.format(Locale.ENGLISH, "%s:%s", new Object[]{this.method, this.password});
        byte[] var18 = var17.getBytes(Charsets.UTF_8);
        var19[0] = Base64.encodeToString(var18, 11);
        var19[1] = StringsKt.contains(host, ':', false) ? '[' + this.host + ']' : this.host;
        var19[2] = this.remotePort;
        String var20 = String.format(Locale.ENGLISH, "%s@%s:%d", var19);
        builder.encodedAuthority(var20);
        PluginConfiguration configuration = new PluginConfiguration(plugin == null ? "" : plugin);
        var20 = configuration.getSelected();
        if (var20.length() > 0) {
            builder.appendQueryParameter(Key.plugin, configuration.getSelectedOptions().toString(false));
        }

        if (name != null && name.length() != 0) {
            builder.fragment(name);
        }

        return builder.build();
    }

    @Override
    @NotNull
    public String toString() {
        return toUri().toString();
    }

    @NotNull
    public JSONObject toJson(@Nullable Map<Long, Profile> profiles) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("server", this.host);
        jsonObject.put("server_port", this.remotePort);
        jsonObject.put("password", this.password);
        jsonObject.put("method", this.method);
        if (profiles == null) {
            return jsonObject;
        }
        PluginConfiguration var10000 = new PluginConfiguration(plugin == null ? "" : plugin);
        PluginOptions var5 = var10000.getSelectedOptions();
        String var17 = var5.getId();
        if (var17.length() > 0) {
            jsonObject.put("plugin", var5.getId());
            jsonObject.put("plugin_opts", var5.toString());
        }

        jsonObject.put("remarks", this.name);
        jsonObject.put("route", this.route);
        jsonObject.put("remote_dns", this.remoteDns);
        jsonObject.put("ipv6", this.ipv6);
        JSONObject var12 = new JSONObject();
        String var9 = "proxy_apps";
        var12.put("enabled", this.proxyApps);
        if (this.proxyApps) {
            var12.put("bypass", this.bypass);
            var12.put("android_list", new JSONArray((Collection) StringsKt.split((CharSequence) this.individual, new String[]{"\n"}, false, 0)));
        }

        jsonObject.put(var9, var12);
        jsonObject.put("udpdns", this.udpdns);
        Profile fallback = (Profile) profiles.get(udpFallback);
        if (fallback != null) {
            if (TextUtils.isEmpty(fallback.plugin)) {
                JSONObject var14 = fallback.toJson(null);
                jsonObject.put("udp_fallback", var14);
            }
        }
        return jsonObject;
    }

    public final void serialize() {
        DataStore.setEditingId(this.id);
        DataStore.privateStore.putString(Key.name, this.name);
        DataStore.privateStore.putString(Key.host, this.host);
        DataStore.privateStore.putString(Key.remotePort, String.valueOf(this.remotePort));
        DataStore.privateStore.putString(Key.password, this.password);
        DataStore.privateStore.putString(Key.route, this.route);
        DataStore.privateStore.putString(Key.remoteDns, this.remoteDns);
        DataStore.privateStore.putString(Key.method, this.method);
        DataStore.setProxyApps(this.proxyApps);
        DataStore.setBypass(this.bypass);
        DataStore.privateStore.putBoolean(Key.udpdns, this.udpdns);
        DataStore.privateStore.putBoolean(Key.ipv6, this.ipv6);
        DataStore.setIndividual(this.individual);
        String var10000 = this.plugin;
        if (this.plugin == null) {
            var10000 = "";
        }

        DataStore.setPlugin(var10000);
        DataStore.setUdpFallback(this.udpFallback);
        DataStore.privateStore.remove(Key.dirty);
    }

    public void deserialize() {
        boolean var4;
        label58:
        {
            if (this.id != 0L) {
                label56:
                {
                    Long var10000 = DataStore.getEditingId();
                    if (var10000 != null) {
                        if (var10000 == id) {
                            break label56;
                        }
                    }

                    var4 = false;
                    break label58;
                }
            }

            var4 = true;
        }
        if (!var4) {
            String var2 = "Check failed.";
            throw new IllegalStateException(var2);
        }
        DataStore.setEditingId(null);
        // It's assumed that default values are never used, so 0/false/null is always used even if that isn't the case
        String var10001 = DataStore.privateStore.getString(Key.name);
        if (var10001 == null) {
            var10001 = "";
        }
        this.name = var10001;
        var10001 = DataStore.privateStore.getString(Key.host);
        if (var10001 == null) {
            var10001 = "";
        }
        this.host = var10001;
        this.remotePort = UtilsKt.parsePort(DataStore.privateStore.getString(Key.remotePort), 8388, 1);
        var10001 = DataStore.privateStore.getString(Key.password);
        if (var10001 == null) {
            var10001 = "";
        }
        this.password = var10001;
        var10001 = DataStore.privateStore.getString(Key.method);
        if (var10001 == null) {
            var10001 = "";
        }
        this.method = var10001;
        var10001 = DataStore.privateStore.getString(Key.route);
        if (var10001 == null) {
            var10001 = "";
        }
        this.route = var10001;
        var10001 = DataStore.privateStore.getString(Key.remoteDns);
        if (var10001 == null) {
            var10001 = "";
        }
        this.remoteDns = var10001;
        this.proxyApps = DataStore.getProxyApps();
        this.bypass = DataStore.getBypass();
        this.udpdns = DataStore.privateStore.getBoolean(Key.udpdns, false);
        this.ipv6 = DataStore.privateStore.getBoolean(Key.ipv6, false);
        this.individual = DataStore.getIndividual();
        this.plugin = DataStore.getPlugin();
        this.udpFallback = DataStore.getUdpFallback();
    }
}
