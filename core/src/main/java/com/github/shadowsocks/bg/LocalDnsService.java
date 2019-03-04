package com.github.shadowsocks.bg;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.github.shadowsocks.Core;
import com.github.shadowsocks.acl.Acl;
import com.github.shadowsocks.database.Profile;
import com.github.shadowsocks.preference.DataStore;
import com.github.shadowsocks.utils.UtilsKt;

import kotlin.io.FilesKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.Charsets;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author chenzhipeng
 */
public class LocalDnsService {
    public interface Interface extends BaseService.Interface {
        @Override
        public void startNativeProcesses();

        public static final class DefaultImpls {

            public static void startNativeProcesses(LocalDnsService.Interface $this) {
                com.github.shadowsocks.bg.BaseService.Interface.DefaultImpls.startNativeProcesses((com.github.shadowsocks.bg.BaseService.Interface) $this);
                BaseService.Data data = $this.getData();
                Profile profile = $this.getData().getProxy().getProfile();

                if (!profile.isUdpdns()) {
                    try {
                        ArrayList<String> list = new ArrayList<>();
                        list.add(new File(Core.app.getApplicationInfo().nativeLibraryDir,
                                Executable.OVERTURE).getAbsolutePath());
                        list.add("-c");
                        list.add(buildOvertureConfig($this, "overture.conf"));
                        Log.e("TAG", "startNativeProcesses:" + buildOvertureConfig($this, "overture.conf"));
                        data.getProcesses().start(buildAdditionalArguments($this, list), null);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            public static JSONObject makeDns(LocalDnsService.Interface $this, String name, String address, Integer timeout, Boolean edns) {
                JSONObject var5 = new JSONObject();
                try {
                    var5.put("Name", name);
                    InetAddress var8 = UtilsKt.parseNumericAddress(address);
                    var5.put("Address", var8 instanceof Inet6Address ? '[' + address + ']' : address);
                    var5.put("Timeout", timeout);
                    var5.put("EDNSClientSubnet", (new JSONObject()).put("Policy", "disable"));
                    String var10002;
                    if (edns) {
                        var5.put("Socks5Address", "127.0.0.1:" + DataStore.getPortProxy());
                        var10002 = "tcp";
                    } else {
                        var10002 = "udp";
                    }

                    var5.put("Protocol", var10002);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return var5;
            }


            public static String buildOvertureConfig(LocalDnsService.Interface $this, String file) throws JSONException {
                Profile profile = $this.getData().getProxy().getProfile();
                JSONObject object = new JSONObject();
                object.put("BindAddress", DataStore.getListenAddress() + ':' + DataStore.getPortLocalDns());
                object.put("RedirectIPv6Record", true);
                object.put("DomainBase64Decode", false);
                object.put("HostsFile", "hosts");
                object.put("MinimumTTL", 120);
                object.put("CacheSize", 4096);
                JSONArray remoteDns = new JSONArray();
                JSONArray jsonArray = new JSONArray(profile.getRemoteDns().split(","));
                for (int i = 0; i < jsonArray.length(); i++) {
                    String dns = (String) jsonArray.get(i);
                    JSONObject jsonObject = makeDns($this, "UserDef-" + i, dns.trim() + ":53", 12, true);
                    remoteDns.put(jsonObject);
                }
                JSONArray localDns = new JSONArray(Arrays.asList(
                        makeDns($this, "Primary-1", "208.67.222.222:443", 9, false),
                        makeDns($this, "Primary-2", "119.29.29.29:53", 9, false),
                        makeDns($this, "Primary-3", "114.114.114.114:53", 9, false)));
                switch (profile.getRoute()) {
                    case Acl.BYPASS_CHN:
                    case Acl.BYPASS_LAN_CHN:
                    case Acl.GFWLIST:
                    case Acl.CUSTOM_RULES:
                        object.put("PrimaryDNS", localDns);
                        object.put("AlternativeDNS", remoteDns);
                        object.put("IPNetworkFile", "china_ip_list.txt");
                        object.put("DomainFile", "domain_exceptions.acl");
                        break;
                    case Acl.CHINALIST:
                        object.put("PrimaryDNS", localDns);
                        object.put("AlternativeDNS", remoteDns);
                        break;
                    default:
                        object.put("PrimaryDNS", remoteDns);
                        // no need to setup AlternativeDNS in Acl.ALL/BYPASS_LAN mode
                        object.put("OnlyPrimaryDNS", true);
                        break;
                }

                String string = object.toString();
                File newFile = new File(Core.getDeviceStorage().getNoBackupFilesDir(), file);
                FilesKt.writeText(newFile, string, Charsets.UTF_8);
                return file;
            }

            @Nullable
            public static IBinder onBind(LocalDnsService.Interface $this, @NotNull Intent intent) {
                Intrinsics.checkParameterIsNotNull(intent, "intent");
                return com.github.shadowsocks.bg.BaseService.Interface.DefaultImpls.onBind((com.github.shadowsocks.bg.BaseService.Interface) $this, intent);
            }

            public static void forceLoad(LocalDnsService.Interface $this) {
                com.github.shadowsocks.bg.BaseService.Interface.DefaultImpls.forceLoad((com.github.shadowsocks.bg.BaseService.Interface) $this);
            }

            @NotNull
            public static ArrayList buildAdditionalArguments(LocalDnsService.Interface $this, @NotNull ArrayList cmd) {
                Intrinsics.checkParameterIsNotNull(cmd, "cmd");
                return com.github.shadowsocks.bg.BaseService.Interface.DefaultImpls.buildAdditionalArguments((com.github.shadowsocks.bg.BaseService.Interface) $this, cmd);
            }

            public static void startRunner(LocalDnsService.Interface $this) {
                com.github.shadowsocks.bg.BaseService.Interface.DefaultImpls.startRunner((com.github.shadowsocks.bg.BaseService.Interface) $this);
            }

            public static void killProcesses(LocalDnsService.Interface $this) {
                com.github.shadowsocks.bg.BaseService.Interface.DefaultImpls.killProcesses((com.github.shadowsocks.bg.BaseService.Interface) $this);
            }

            public static void stopRunner(LocalDnsService.Interface $this, boolean stopService, @Nullable String msg) {
                com.github.shadowsocks.bg.BaseService.Interface.DefaultImpls.stopRunner((com.github.shadowsocks.bg.BaseService.Interface) $this, stopService, msg);
            }

            @NotNull
            public static BaseService.Data getData(LocalDnsService.Interface $this) {
                return com.github.shadowsocks.bg.BaseService.Interface.DefaultImpls.getData((com.github.shadowsocks.bg.BaseService.Interface) $this);
            }

            public static int onStartCommand(LocalDnsService.Interface $this, @Nullable Intent intent, int flags, int startId) {
                return com.github.shadowsocks.bg.BaseService.Interface.DefaultImpls.onStartCommand((com.github.shadowsocks.bg.BaseService.Interface) $this, intent, flags, startId);
            }
        }
    }
}
