package com.github.shadowsocks.bg;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserManager;
import android.support.v4.content.ContextCompat;

import com.github.shadowsocks.Core;
import com.github.shadowsocks.aidl.IShadowsocksService;
import com.github.shadowsocks.aidl.IShadowsocksServiceCallback;
import com.github.shadowsocks.aidl.TrafficStats;
import com.github.shadowsocks.core.R;
import com.github.shadowsocks.database.Profile;
import com.github.shadowsocks.plugin.PluginManager;
import com.github.shadowsocks.preference.DataStore;
import com.github.shadowsocks.utils.Action;
import com.github.shadowsocks.utils.Key;
import com.github.shadowsocks.utils.UtilsKt;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;

import androidx.annotation.Nullable;
import kotlin.Pair;
import kotlin.Triple;
import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;

/**
 * @author 陈志鹏
 * @date 2019/2/28
 */
public class BaseService {
    public static final int IDLE = 0;
    public static final int CONNECTING = 1;
    public static final int CONNECTED = 2;
    public static final int STOPPING = 3;
    public static final int STOPPED = 4;
    @NotNull
    public static final String CONFIG_FILE = "shadowsocks.conf";
    @NotNull
    public static final String CONFIG_FILE_UDP = "shadowsocks-udp.conf";
    private static final WeakHashMap instances;

    static {
        instances = new WeakHashMap<Interface, Data>();
    }

    public static void register(@NotNull BaseService.Interface instance) {
        BaseService.Data var3 = new BaseService.Data(instance);
        instances.put(instance, var3);
    }

    public static boolean getUsingVpnMode() {
        return Intrinsics.areEqual(DataStore.getServiceMode(), Key.modeVpn);
    }

    public interface Interface {
        @NotNull
        String getTag();

        @Nullable
        IBinder onBind(@NotNull Intent intent);

        void forceLoad();

        @NotNull
        ArrayList<String> buildAdditionalArguments(@NotNull ArrayList<String> cmd);

        void startNativeProcesses();

        @NotNull
        ServiceNotification createNotification(@NotNull String profileName);

        void startRunner();

        void killProcesses();

        void stopRunner(boolean stopService, @Nullable String msg);

        @NotNull
        BaseService.Data getData();

        int onStartCommand(@Nullable Intent intent, int flags, int startId);

        public static final class DefaultImpls {
            @Nullable
            public static IBinder onBind(BaseService.Interface $this, @NotNull Intent intent) {
                return Intrinsics.areEqual(intent.getAction(), Action.SERVICE) ? (IBinder) $this.getData().getBinder() : null;
            }

            public static void forceLoad(BaseService.Interface $this) {
                Pair<Profile, Profile> pair = Core.getCurrentProfile();
                if (pair == null) {
                    $this.stopRunner(true, ((Context) $this).getString(R.string.profile_empty));
                    return;
                }
                if (pair.getFirst().getHost().isEmpty() || pair.getFirst().getPassword().isEmpty()
                        || pair.getSecond() != null && (pair.getSecond().getHost().isEmpty() || pair.getSecond().getPassword().isEmpty())) {
                    $this.stopRunner(true, ((Context) $this).getString(R.string.proxy_empty));
                    return;
                }
                int s = $this.getData().state;
                switch (s) {
                    case STOPPED:
                        $this.startRunner();
                        break;
                    case CONNECTED:
                        $this.stopRunner(false, null);
                        $this.startRunner();
                        break;
                    default:
                        break;
                }
            }

            @NotNull
            public static ArrayList buildAdditionalArguments(BaseService.Interface $this, @NotNull ArrayList cmd) {
                return cmd;
            }

            public static void startNativeProcesses(BaseService.Interface $this) {
                File configRoot;
                UserManager userManager = ContextCompat.getSystemService(Core.app, UserManager.class);
                if (Build.VERSION.SDK_INT < 24 || userManager.isUserUnlocked() != false) {
                    configRoot = Core.app.getNoBackupFilesDir();
                } else {
                    configRoot = Core.getDeviceStorage().getNoBackupFilesDir();
                }
                ProxyInstance udpFallback = $this.getData().udpFallback;

                ProxyInstance var7 = $this.getData().getProxy();
                if (var7 == null) {
                    Intrinsics.throwNpe();
                }
                var7.start($this,
                        new File(Core.getDeviceStorage().getNoBackupFilesDir(), "stat_main"),
                        new File(configRoot, CONFIG_FILE),
                        udpFallback == null ? "-u" : null);
                boolean var3 = (udpFallback != null ? udpFallback.getPluginPath() : null) == null;
                if (!var3) {
                    String var4 = "Check failed.";
                    throw new IllegalStateException(var4);
                }
                if (udpFallback != null) {
                    udpFallback.start($this, new File(Core.getDeviceStorage().getNoBackupFilesDir(), "stat_udp"),
                            new File(configRoot, CONFIG_FILE_UDP), "-U");
                }
            }

            public static void startRunner(BaseService.Interface $this) {
                if ($this == null) {
                    throw new TypeCastException("null cannot be cast to non-null type android.content.Context");
                } else {
                    if (Build.VERSION.SDK_INT >= 26) {
                        ((Context) $this).startForegroundService(new Intent((Context) $this, $this.getClass()));
                    } else {
                        ((Context) $this).startService(new Intent((Context) $this, $this.getClass()));
                    }
                }
            }

            public static void killProcesses(BaseService.Interface $this) {
                $this.getData().getProcesses().killAll();
            }

            public static void stopRunner(BaseService.Interface $this, boolean stopService, @Nullable String msg) {
                final BaseService.Data data = $this.getData();
                data.changeState(STOPPING, null);
                $this.killProcesses();

                if (data.isCloseReceiverRegistered()) {
                    ((Service) $this).unregisterReceiver(data.getCloseReceiver());
                    data.setCloseReceiverRegistered(false);
                }

                ServiceNotification var17 = data.getNotification();
                if (var17 != null) {
                    var17.destroy();
                }
                data.setNotification(null);

                List<ProxyInstance> proxyInstances = CollectionsKt.listOfNotNull(new ProxyInstance[]{data.getProxy(), data.getUdpFallback()});
                final List<Long> ids = new ArrayList(proxyInstances.size());
                Iterator var8 = proxyInstances.iterator();

                while (var8.hasNext()) {
                    ProxyInstance it = (ProxyInstance) var8.next();
                    try {
                        it.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Profile var18 = it.getProfile();
                    Long var15 = var18.getId();
                    ids.add(var15);
                }
                data.setProxy(null);

                if (!ids.isEmpty()) {
                    Core.handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!data.getBandwidthListeners().isEmpty()) {
                                int n = data.getCallbacks().beginBroadcast();
                                for (int i = 0; i < n; i++) {
                                    try {
                                        IShadowsocksServiceCallback item = data.getCallbacks().getBroadcastItem(i);
                                        if (data.getBandwidthListeners().contains(item.asBinder())) {
                                            Iterator var6 = ids.iterator();
                                            while (var6.hasNext()) {
                                                long p1 = ((Number) var6.next()).longValue();
                                                item.trafficPersisted(p1);
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                data.getCallbacks().finishBroadcast();
                            }
                        }
                    });
                }
                // change the state
                data.changeState(STOPPED, msg);
                // stop the service if nothing has bound to it
                if (stopService) {
                    ((Service) $this).stopSelf();
                }
            }

            @NotNull
            public static BaseService.Data getData(BaseService.Interface $this) {
                return (BaseService.Data) BaseService.instances.get($this);
            }

            public static int onStartCommand(final BaseService.Interface $this, @Nullable Intent intent, int flags, int startId) {
                final BaseService.Data data = $this.getData();
                if (data.getState() != STOPPED) {
                    return Service.START_NOT_STICKY;
                }
                Pair<Profile, Profile> profilePair = Core.getCurrentProfile();
                if (profilePair == null) {
                    // gracefully shutdown: https://stackoverflow.com/q/47337857/2245107
                    data.setNotification($this.createNotification(""));
                    $this.stopRunner(true, ((Context) $this).getString(R.string.profile_empty));
                    return Service.START_NOT_STICKY;
                }
                Profile profile = profilePair.component1();
                Profile fallback = profilePair.component2();
                profile.setName(profile.getFormattedName());   // save name for later queries
                final ProxyInstance proxy = new ProxyInstance(profile);
                data.setProxy(proxy);
                if (fallback != null) {
                    data.setUdpFallback(new ProxyInstance(fallback, profile.getRoute()));
                }

                if (!data.isCloseReceiverRegistered()) {
                    IntentFilter filter = new IntentFilter();
                    filter.addAction(Action.RELOAD);
                    filter.addAction(Intent.ACTION_SHUTDOWN);
                    filter.addAction(Action.CLOSE);
                    ((Context) $this).registerReceiver(data.getCloseReceiver(), filter);
                    data.setCloseReceiverRegistered(true);
                }

                data.setNotification($this.createNotification(profile.getFormattedName()));
                data.changeState(CONNECTING, null);

                UtilsKt.thread($this.getTag() + "-Connecting", new Function0() {
                    // $FF: synthetic method
                    // $FF: bridge method
                    @Override
                    public Object invoke() {
                        try {
                            proxy.init();
                            ProxyInstance var10000 = data.getUdpFallback();
                            if (var10000 != null) {
                                var10000.init();
                            }

                            $this.killProcesses();
                            $this.startNativeProcesses();
                            proxy.scheduleUpdate();
                            var10000 = data.getUdpFallback();
                            if (var10000 != null) {
                                var10000.scheduleUpdate();
                            }

                            data.changeState(CONNECTED, null);
                        } catch (Exception var2) {
                            $this.stopRunner(true, ((Context) $this).getString(R.string.invalid_server));
                        } catch (Throwable var3) {
                            if (!(var3 instanceof PluginManager.PluginNotFoundException) && !(var3 instanceof VpnService.NullConnectionException)) {
                                UtilsKt.printLog(var3);
                            }

                            $this.stopRunner(true, ((Context) $this).getString(R.string.service_failed) + ": " + var3.getLocalizedMessage());
                        }
                        return Unit.INSTANCE;
                    }
                });
                return Service.START_NOT_STICKY;
            }
        }
    }

    public static final class Data {

        private volatile int state = STOPPED;
        private BaseService.Interface service;
        @NotNull
        private GuardedProcessPool processes = new GuardedProcessPool();
        @Nullable
        private volatile ProxyInstance proxy;
        @Nullable
        private volatile ProxyInstance udpFallback;
        @NotNull
        private RemoteCallbackList<IShadowsocksServiceCallback> callbacks = new RemoteCallbackList<IShadowsocksServiceCallback>();
        @NotNull
        private HashSet<IBinder> bandwidthListeners = new HashSet<IBinder>();
        @Nullable
        private ServiceNotification notification;
        @NotNull
        private BroadcastReceiver closeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() == Action.RELOAD) {
                    service.forceLoad();
                } else {
                    service.stopRunner(true, null);
                }
            }
        };
        private boolean closeReceiverRegistered = false;
        private IShadowsocksService.Stub binder = new IShadowsocksService.Stub() {
            @Override
            public int getState() throws RemoteException {
                return Data.this.state;
            }

            @Override
            public String getProfileName() throws RemoteException {
                ProxyInstance var10000 = Data.this.proxy;
                String var2;
                if (var10000 != null) {
                    Profile var1 = var10000.getProfile();
                    if (var1 != null) {
                        var2 = var1.getName();
                        if (var2 != null) {
                            return var2;
                        }
                    }
                }

                var2 = "Idle";
                return var2;
            }

            @Override
            public void registerCallback(IShadowsocksServiceCallback cb) throws RemoteException {
                Data.this.callbacks.register(cb);
            }

            private final boolean registerTimeout() {
                return Core.handler.postAtTime(new Runnable() {
                    @Override
                    public void run() {
                        onTimeout();
                    }
                }, this, SystemClock.uptimeMillis() + 1000);
            }

            private void onTimeout() {
                List<ProxyInstance> proxies = CollectionsKt.listOfNotNull(new ProxyInstance[]{Data.this.getProxy(), Data.this.getUdpFallback()});
                ArrayList<Pair<Long, Pair<TrafficStats, Boolean>>> proxyInstances = new ArrayList(proxies.size());
                Iterator var6 = proxies.iterator();
                while (var6.hasNext()) {
                    ProxyInstance itx = (ProxyInstance) var6.next();
                    Profile profile = itx.getProfile();
                    Long id = profile.getId();
                    TrafficMonitor var10003 = itx.getTrafficMonitor();
                    Pair<Long, Pair<TrafficStats, Boolean>> var18 = new Pair(id, var10003 != null ? var10003.requestUpdate() : null);
                    proxyInstances.add(var18);
                }

                ArrayList<Pair<Long, Pair<TrafficStats, Boolean>>> newList = new ArrayList(proxyInstances.size());
                var6 = proxyInstances.iterator();

                while (var6.hasNext()) {
                    Pair<Long, Pair<TrafficStats, Boolean>> itxx = (Pair<Long, Pair<TrafficStats, Boolean>>) var6.next();
                    if (itxx.getSecond() != null) {
                        newList.add(itxx);
                    }
                }

                ArrayList<Triple<Long, TrafficStats, Boolean>> stats = new ArrayList(newList.size());
                var6 = newList.iterator();

                while (var6.hasNext()) {
                    Pair<Long, Pair<TrafficStats, Boolean>> itxx = (Pair<Long, Pair<TrafficStats, Boolean>>) var6.next();
                    Long var44 = itxx.getFirst();
                    Object var45 = itxx.getSecond();
                    if (var45 == null) {
                        Intrinsics.throwNpe();
                    }
                    TrafficStats var46 = (TrafficStats) ((android.util.Pair)var45).first;
                    Boolean var10004 = (Boolean) ((android.util.Pair)var45).second;
                    Triple var43 = new Triple(var44, var46, var10004);
                    stats.add(var43);
                }

                boolean var40;
                if (stats instanceof Collection && ((Collection) stats).isEmpty()) {
                    var40 = false;
                } else {
                    Iterator var4 = stats.iterator();
                    while (true) {
                        if (!var4.hasNext()) {
                            var40 = false;
                            break;
                        }

                        Triple<Long, TrafficStats, Boolean> it = (Triple<Long, TrafficStats, Boolean>) var4.next();
                        if (it.getThird()) {
                            var40 = true;
                            break;
                        }
                    }
                }
                if (var40 && state == CONNECTED) {
                    Collection var20 = (Collection) Data.this.getBandwidthListeners();
                    if (!var20.isEmpty()) {
                        TrafficStats sum = new TrafficStats();

                        TrafficStats var41;
                        for (Iterator var32 = stats.iterator(); var32.hasNext(); sum = var41) {
                            Triple b = (Triple) var32.next();
                            var41 = sum.plus((TrafficStats) b.getSecond());
                        }

                        int n = Data.this.getCallbacks().beginBroadcast();
                        for (int i = 0; i < n; i++) {
                            try {
                                IShadowsocksServiceCallback item = (IShadowsocksServiceCallback) Data.this.getCallbacks().getBroadcastItem(i);
                                if (Data.this.getBandwidthListeners().contains(item.asBinder())) {
                                    Iterator var37 = stats.iterator();

                                    while (var37.hasNext()) {
                                        Triple $id_stats = (Triple) var37.next();
                                        long id = ((Number) $id_stats.component1()).longValue();
                                        TrafficStats statsx = (TrafficStats) $id_stats.component2();
                                        item.trafficUpdated(id, statsx);
                                    }

                                    item.trafficUpdated(0L, sum);
                                }
                            } catch (Exception var19) {
                                UtilsKt.printLog((Throwable) var19);
                            }
                        }

                        Data.this.getCallbacks().finishBroadcast();
                    }
                }

                this.registerTimeout();
            }

            @Override
            public void startListeningForBandwidth(IShadowsocksServiceCallback cb) throws RemoteException {
                boolean wasEmpty = Data.this.bandwidthListeners.isEmpty();
                if (Data.this.bandwidthListeners.add(cb.asBinder())) {
                    if (wasEmpty) {
                        this.registerTimeout();
                    }
                    if (this.getState() != CONNECTED) {
                        return;
                    }
                    TrafficStats sum = new TrafficStats();
                    ProxyInstance proxy = Data.this.getProxy();
                    if (proxy == null) {
                        return;
                    }

                    TrafficMonitor var12 = proxy.getTrafficMonitor();
                    TrafficStats stats = var12 != null ? var12.getOut() : null;
                    TrafficStats var10002;
                    if (stats == null) {
                        var10002 = sum;
                    } else {
                        sum = sum.plus(stats);
                        var10002 = stats;
                    }

                    cb.trafficUpdated(proxy.getProfile().getId(), var10002);

                    ProxyInstance var10000 = Data.this.getUdpFallback();
                    if (var10000 != null) {
                        var12 = var10000.getTrafficMonitor();
                        TrafficStats var8 = var12 != null ? var12.getOut() : null;
                        Profile var10001 = var10000.getProfile();
                        long var13 = var10001.getId();
                        if (var8 == null) {
                            var10002 = new TrafficStats();
                        } else {
                            sum = sum.plus(var8);
                            var10002 = var8;
                        }
                        cb.trafficUpdated(var13, var10002);
                    }
                    cb.trafficUpdated(0L, sum);
                }
            }

            @Override
            public void stopListeningForBandwidth(IShadowsocksServiceCallback cb) throws RemoteException {
                if (bandwidthListeners.remove(cb.asBinder()) && bandwidthListeners.isEmpty()) {
                    Core.handler.removeCallbacksAndMessages(this);
                }
            }

            @Override
            public void unregisterCallback(IShadowsocksServiceCallback cb) throws RemoteException {
                this.stopListeningForBandwidth(cb);
                Data.this.callbacks.unregister(cb);
            }
        };


        public Data(@NotNull BaseService.Interface service) {
            this.service = service;
        }

        public final void changeState(final int s, @Nullable final String msg) {
            if (this.state != s || msg != null) {
                if (this.callbacks.getRegisteredCallbackCount() > 0) {
                    Core.handler.post(new Runnable() {
                        @Override
                        public final void run() {
                            int n = Data.this.getCallbacks().beginBroadcast();

                            for (int i = 0; i < n; i++) {
                                try {
                                    ((IShadowsocksServiceCallback) Data.this.getCallbacks().getBroadcastItem(i)).stateChanged(s, Data.this.getBinder().getProfileName(), msg);
                                } catch (Exception var5) {
                                    UtilsKt.printLog((Throwable) var5);
                                }
                            }

                            Data.this.getCallbacks().finishBroadcast();
                        }
                    });
                }

                this.state = s;
            }
        }

        @NotNull
        public final RemoteCallbackList<IShadowsocksServiceCallback> getCallbacks() {
            return this.callbacks;
        }

        @NotNull
        public final IShadowsocksService.Stub getBinder() {
            return this.binder;
        }

        @Nullable
        public final ProxyInstance getUdpFallback() {
            return this.udpFallback;
        }

        @Nullable
        public final ProxyInstance getProxy() {
            return this.proxy;
        }

        @NotNull
        public final HashSet<IBinder> getBandwidthListeners() {
            return this.bandwidthListeners;
        }

        public void setCallbacks(RemoteCallbackList<IShadowsocksServiceCallback> callbacks) {
            this.callbacks = callbacks;
        }

        public void setUdpFallback(@Nullable ProxyInstance udpFallback) {
            this.udpFallback = udpFallback;
        }

        public void setProxy(@Nullable ProxyInstance proxy) {
            this.proxy = proxy;
        }

        public void setBandwidthListeners(@NotNull HashSet<IBinder> bandwidthListeners) {
            this.bandwidthListeners = bandwidthListeners;
        }

        public void setNotification(@Nullable ServiceNotification notification) {
            this.notification = notification;
        }

        @Nullable
        public ServiceNotification getNotification() {
            return notification;
        }

        public void setState(int state) {
            this.state = state;
        }

        public int getState() {
            return state;
        }

        @NotNull
        public BroadcastReceiver getCloseReceiver() {
            return closeReceiver;
        }

        public void setCloseReceiver(@NotNull BroadcastReceiver closeReceiver) {
            this.closeReceiver = closeReceiver;
        }

        public void setCloseReceiverRegistered(boolean closeReceiverRegistered) {
            this.closeReceiverRegistered = closeReceiverRegistered;
        }

        public boolean isCloseReceiverRegistered() {
            return closeReceiverRegistered;
        }

        @NotNull
        public GuardedProcessPool getProcesses() {
            return processes;
        }
    }

    @NotNull
    public static Class getServiceClass() {
        switch (DataStore.getServiceMode()) {
            case Key.modeProxy:
                return ProxyService.class;
            case Key.modeVpn:
                return VpnService.class;
            case Key.modeTransproxy:
                return TransproxyService.class;
            default:
                throw new UnknownError();
        }
    }
}
