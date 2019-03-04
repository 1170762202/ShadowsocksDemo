package com.github.shadowsocks.bg;

import android.content.Context;
import com.github.shadowsocks.acl.Acl;
import com.github.shadowsocks.acl.AclSyncer;
import com.github.shadowsocks.database.Profile;
import com.github.shadowsocks.database.ProfileManager;
import com.github.shadowsocks.plugin.PluginConfiguration;
import com.github.shadowsocks.plugin.PluginManager;
import com.github.shadowsocks.plugin.PluginOptions;
import com.github.shadowsocks.preference.DataStore;
import com.github.shadowsocks.utils.Commandline;
import com.github.shadowsocks.utils.DirectBoot;
import com.github.shadowsocks.utils.UtilsKt;
import kotlin.Pair;
import kotlin.TuplesKt;
import kotlin.Unit;
import kotlin.collections.ArraysKt;
import kotlin.collections.CollectionsKt;
import kotlin.io.FilesKt;
import kotlin.jvm.functions.Function0;
import kotlin.text.Charsets;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author 陈志鹏
 * @date 2019/2/25
 */
public class ProxyInstance implements AutoCloseable {

    private Profile profile;
    private String route;
    @Nullable
    private File configFile;
    @Nullable
    private TrafficMonitor trafficMonitor;
    private PluginOptions plugin;
    @Nullable
    private String pluginPath;


    public ProxyInstance(Profile profile) {
        this(profile, profile.getRoute());
    }

    public ProxyInstance(Profile profile, String route) {
        this.profile = profile;
        this.route = route;

        try {
            plugin = new PluginConfiguration(profile.getPlugin() != null ? profile.getPlugin() : "").getSelectedOptions();
            pluginPath = PluginManager.init(plugin);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public void init() {
        if (route == Acl.CUSTOM_RULES) {
            Acl.save(Acl.CUSTOM_RULES, Acl.getCustomRules().flatten(10));
        }
        if (!UtilsKt.isNumericAddress(profile.getHost())) {
            UtilsKt.thread("ProxyInstance-resolve", false, false, (ClassLoader) null, 0, (Function0) (new Function0() {
                @Override
                public Object invoke() {
                    try {
                        InetAddress var10001 = InetAddress.getByName(profile.getHost());
                        String var3 = var10001.getHostAddress();
                        if (var3 == null) {
                            var3 = "";
                        }

                        profile.setHost(var3);
                    } catch (UnknownHostException var2) {
                        ;
                    }
                    return Unit.INSTANCE;
                }
            }));
            if (!UtilsKt.isNumericAddress(this.profile.getHost())) {
                try {
                    throw new UnknownHostException();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public final void start(@NotNull BaseService.Interface service, @NotNull File stat, @NotNull File configFile, @Nullable String extraFlag) {
        this.trafficMonitor = new TrafficMonitor(stat);
        this.configFile = configFile;
        JSONObject config = null;
        try {
            config = profile.toJson(null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (pluginPath != null) {
            ArrayList<String> pluginCmd = (ArrayList<String>) Arrays.asList(pluginPath);
            if (DataStore.getTcpFastOpen()) {
                pluginCmd.add("--fast-open");
            }
            try {
                config
                        .put("plugin", Commandline.toString(service.buildAdditionalArguments(pluginCmd)))
                        .put("plugin_opts", plugin.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        FilesKt.writeText(configFile, config.toString(), Charsets.UTF_8);

        ArrayList<String> list = new ArrayList<>();
        list.add(new File(((Context) service).getApplicationInfo().nativeLibraryDir, Executable.SS_LOCAL).getAbsolutePath());
        list.add("-b");
        list.add(DataStore.getListenAddress());
        list.add("-l");
        list.add(DataStore.getPortProxy() + "");
        list.add("-t");
        list.add("600");
        list.add("-S");
        list.add(stat.getAbsolutePath());
        list.add("-c");
        list.add(configFile.getAbsolutePath());
        ArrayList<String> cmd = service.buildAdditionalArguments(list);
        if (extraFlag != null) {
            cmd.add(extraFlag);
        }
        if (route != Acl.ALL) {
            cmd.add("--acl");
            cmd.add(Acl.getFile(route).getAbsolutePath());
        }

        // for UDP profile, it's only going to operate in UDP relay mode-only so this flag has no effect
        if (profile.isUdpdns()) {
            cmd.add("-D");
        }

        if (DataStore.getTcpFastOpen()) {
            cmd.add("--fast-open");
        }

        service.getData().getProcesses().start(cmd, null);
    }

    public final void scheduleUpdate() {
        if (!ArraysKt.contains(new String[]{Acl.ALL, Acl.CUSTOM_RULES}, this.route)) {
            AclSyncer.schedule(this.route);
        }
    }

    @Nullable
    public final File getConfigFile() {
        return this.configFile;
    }

    public final void setConfigFile(@Nullable File var1) {
        this.configFile = var1;
    }

    @Nullable
    public final TrafficMonitor getTrafficMonitor() {
        return this.trafficMonitor;
    }

    public final void setTrafficMonitor(@Nullable TrafficMonitor var1) {
        this.trafficMonitor = var1;
    }


    @Override
    public void close() throws Exception {
        if (this.trafficMonitor != null) {
            trafficMonitor.close();
            try {
                Profile profile = ProfileManager.getProfile(this.profile.getId());
                if (profile == null) {
                    return;
                }
                profile.setTx(profile.getTx() + trafficMonitor.getCurrent().getTxTotal());
                profile.setRx(profile.getRx() + trafficMonitor.getCurrent().getRxTotal());
                ProfileManager.updateProfile(profile);
            } catch (IOException e) {
                if (!DataStore.getDirectBootAware()) {
                    throw e;
                }
                Pair pair = DirectBoot.getDeviceProfile();
                Iterable $receiver$iv = (Iterable) CollectionsKt.filterNotNull((Iterable) TuplesKt.toList(pair));
                Object single$iv = null;
                boolean found$iv = false;
                Iterator var8 = $receiver$iv.iterator();

                while (var8.hasNext()) {
                    Object element$iv = var8.next();
                    Profile it = (Profile) element$iv;
                    if (it.getId() == this.profile.getId()) {
                        if (found$iv) {
                            throw new IllegalArgumentException("Collection contains more than one matching element.");
                        }

                        single$iv = element$iv;
                        found$iv = true;
                    }
                }

                if (!found$iv) {
                    throw new NoSuchElementException("Collection contains no element matching the predicate.");
                }
                Profile profile = (Profile) single$iv;
                profile.setTx(profile.getTx() + trafficMonitor.getCurrent().getTxTotal());
                profile.setRx(profile.getRx() + trafficMonitor.getCurrent().getRxTotal());
                profile.setDirty(true);
                DirectBoot.update(profile);
                DirectBoot.listenForUnlock();
            }
        }
        trafficMonitor = null;
        if (configFile != null) {
            configFile.delete();
        }
        configFile = null;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    @Nullable
    public String getPluginPath() {
        return pluginPath;
    }

    public void setPluginPath(@Nullable String pluginPath) {
        this.pluginPath = pluginPath;
    }
}
