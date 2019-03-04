package com.github.shadowsocks.bg;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.LocalSocket;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.support.v4.content.ContextCompat;
import android.system.ErrnoException;
import android.system.Os;

import com.github.shadowsocks.Core;
import com.github.shadowsocks.JniHelper;
import com.github.shadowsocks.VpnRequestActivity;
import com.github.shadowsocks.acl.Acl;
import com.github.shadowsocks.core.R;
import com.github.shadowsocks.database.Profile;
import com.github.shadowsocks.preference.DataStore;
import com.github.shadowsocks.utils.Subnet;
import com.github.shadowsocks.utils.UtilsKt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.collections.ArraysKt;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt;

/**
 * @author 陈志鹏
 * @date 2019/2/28
 */
public class VpnService extends android.net.VpnService implements LocalDnsService.Interface {

    private static final int VPN_MTU = 1500;
    private static final String PRIVATE_VLAN = "172.19.0.%s";
    private static final String PRIVATE_VLAN6 = "fdfe:dcba:9876::%s";
    private static Method getInt = null;
    private static final NetworkRequest defaultNetworkRequest;

    static {
        try {
            getInt = FileDescriptor.class.getDeclaredMethod("getInt$");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        defaultNetworkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
                .build();
    }

    private ParcelFileDescriptor conn;
    private VpnService.ProtectWorker worker;
    private Network underlyingNetwork;
    private ConnectivityManager connectivity = ContextCompat.getSystemService(Core.app,ConnectivityManager.class);
    private ConnectivityManager.NetworkCallback defaultNetworkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            super.onAvailable(network);
            underlyingNetwork = network;
        }

        @Override
        public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities);
            underlyingNetwork = network;
        }

        @Override
        public void onLost(Network network) {
            super.onLost(network);
            underlyingNetwork = null;
        }
    };
    private boolean listeningForDefaultNetwork = false;

    @Override
    public void startNativeProcesses() {
        VpnService.ProtectWorker worker = new VpnService.ProtectWorker();
        worker.start();
        this.worker = worker;
        LocalDnsService.Interface.DefaultImpls.startNativeProcesses(this);
        try {
            sendFd(this.startVpn());
        } catch (ErrnoException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    @Override
    public String getTag() {
        return "ShadowsocksVpnService";
    }

    @TargetApi(24)
    private void setUnderlyingNetwork(Network value) {
        this.setUnderlyingNetworks(value == null ? null : new Network[]{value});
        this.underlyingNetwork = value;
    }

    @NotNull
    @Override
    public ServiceNotification createNotification(@NotNull String profileName) {
        return new ServiceNotification(this, profileName, "service-vpn", false);
    }

    @Override
    public void forceLoad() {
        DefaultImpls.forceLoad(this);
    }

    @NotNull
    @Override
    public ArrayList<String> buildAdditionalArguments(@NotNull ArrayList<String> cmd) {
        cmd.add("-V");
        return cmd;
    }

    @Override
    public void startRunner() {
        DefaultImpls.startRunner(this);
    }

    @Override
    public void killProcesses() {
        if (this.listeningForDefaultNetwork) {
            connectivity.unregisterNetworkCallback((ConnectivityManager.NetworkCallback) this.defaultNetworkCallback);
            this.listeningForDefaultNetwork = false;
        }

        if (this.worker != null) {
            worker.stopThread();
        }

        this.worker = null;
        DefaultImpls.killProcesses(this);
        if (this.conn != null) {
            try {
                conn.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.conn = null;
    }

    @Override
    public void stopRunner(boolean stopService, @Nullable String msg) {
        DefaultImpls.stopRunner(this, stopService, msg);
    }

    @NotNull
    @Override
    public BaseService.Data getData() {
        return DefaultImpls.getData(this);
    }

    private class ProtectWorker extends LocalSocketListener {

        public ProtectWorker() {
            super("ShadowsocksVpnThread");
        }

        @Override
        @NotNull
        protected File getSocketFile() {
            return new File(Core.getDeviceStorage().getNoBackupFilesDir(), "protect_path");
        }

        @Override
        protected void accept(@NotNull LocalSocket socket) {
            try {
                socket.getInputStream().read();
                FileDescriptor[] fileDescriptors = socket.getAncillaryFileDescriptors();
                if (fileDescriptors == null) {
                    Intrinsics.throwNpe();
                }

                FileDescriptor fd = ArraysKt.single(fileDescriptors);
                if (fd == null) {
                    Intrinsics.throwNpe();
                }
                OutputStream var6 = socket.getOutputStream();
                boolean var7 = false;
                try {
                    Network network = underlyingNetwork;
                    if (network != null && Build.VERSION.SDK_INT >= 23){
                        try {
                            network.bindSocket(fd);
                        }catch (SocketException e){
                            if (e.getCause() instanceof  ErrnoException){
                                ErrnoException exception = (ErrnoException) e.getCause();
                                if (exception.errno == 64){
                                    exception.printStackTrace();
                                }else{
                                    e.printStackTrace();
                                }
                            }else{
                                e.printStackTrace();
                            }
                        }
                        var7 = true;
                    }else{
                        try {
                            var7 = protect((Integer) getInt.invoke(fd));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                } finally {
                    try {
                        Os.close(fd);
                    } catch (ErrnoException e) {
                        e.printStackTrace();
                    }
                }

                var6.write(var7 ? 0 : 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class NullConnectionException extends NullPointerException {
        @Override
        public String getLocalizedMessage() {
            return getString(R.string.reboot_required);
        }
    }

    public VpnService() {
        BaseService.register(this);
    }

    @Override
    @Nullable
    public IBinder onBind(@NotNull Intent intent) {
        if (intent.getAction() == SERVICE_INTERFACE) {
            return LocalDnsService.Interface.DefaultImpls.onBind(this, intent);
        } else {
            return LocalDnsService.Interface.DefaultImpls.onBind(this, intent);
        }
    }

    @Override
    public void onRevoke() {
        BaseService.Interface.DefaultImpls.stopRunner(this, true, null);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if (BaseService.getUsingVpnMode()) {
            if (android.net.VpnService.prepare(this) == null) {
                return DefaultImpls.onStartCommand(this, intent, flags, startId);
            }

            this.startActivity((new Intent(this, VpnRequestActivity.class)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }

        stopRunner(true, null);
        return Service.START_NOT_STICKY;
    }

    private int startVpn() {
        Profile profile = getData().getProxy().getProfile();
        android.net.VpnService.Builder builder = new android.net.VpnService.Builder()
                .setConfigureIntent(Core.configureIntent.invoke(this))
                .setSession(profile.getFormattedName())
                .setMtu(VPN_MTU)
                .addAddress(String.format(Locale.ENGLISH, PRIVATE_VLAN, new Object[]{"1"}), 24);

        List<String> split = StringsKt.split(profile.getRemoteDns(), new String[]{","}, false, 0);
        Iterator var27 = split.iterator();

        while (var27.hasNext()) {
            String it = (String) var27.next();
            String var10 = StringsKt.trim((CharSequence) it).toString();
            builder.addDnsServer(var10);
        }
        if (profile.isIpv6()) {
            builder.addAddress(String.format(Locale.ENGLISH, PRIVATE_VLAN6, new Object[]{"1"}), 126);
            builder.addRoute("::", 0);
        }

        if (profile.isProxyApps()) {
            String me = getPackageName();
            List<String> strings = StringsKt.split(profile.getIndividual(), new char[]{'\n'}, false, 0);

            ArrayList copy = new ArrayList();
            Iterator var39 = strings.iterator();

            while (var39.hasNext()) {
                String it = (String) var39.next();
                if (Intrinsics.areEqual(it, me) ^ true) {
                    copy.add(it);
                }
            }
            Iterator iterator = copy.iterator();

            while (iterator.hasNext()) {
                String it = (String) iterator.next();
                try {
                    if (profile.isBypass()) {
                        builder.addDisallowedApplication(it);
                    } else {
                        builder.addAllowedApplication(it);
                    }
                } catch (PackageManager.NameNotFoundException ex) {
                    UtilsKt.printLog((Throwable) ex);
                }
            }

            if (!profile.isBypass()) {
                try {
                    builder.addAllowedApplication(me);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        String route = profile.getRoute();
        if (route.equals(Acl.ALL) || route.equals(Acl.BYPASS_CHN) || route.equals(Acl.CUSTOM_RULES)) {
            builder.addRoute("0.0.0.0", 0);
        } else {
            String[] stringArray = getResources().getStringArray(R.array.bypass_private_route);
            for (int i = 0; i < stringArray.length; i++) {
                String s = stringArray[i];
                Subnet subnet = Subnet.fromString(s);
                builder.addRoute(subnet.getAddress().getHostAddress(), subnet.getPrefixSize());
            }
            List<String> split1 = StringsKt.split(profile.getRemoteDns(), new String[]{","}, false,0);
            List<InetAddress> newSplit1 = new ArrayList();
            Iterator var45 = split1.iterator();

            while (var45.hasNext()) {
                String it = (String) var45.next();
                if (it == null) {
                    throw new TypeCastException("null cannot be cast to non-null type kotlin.CharSequence");
                }

                InetAddress var58 = UtilsKt.parseNumericAddress(StringsKt.trim((CharSequence) it).toString());
                if (var58 != null) {
                    newSplit1.add(var58);
                }
            }
            Iterator iterator1 = newSplit1.iterator();

            while (true) {
                if (!iterator1.hasNext()) {
                    break;
                }
                InetAddress it = (InetAddress) iterator1.next();
                builder.addRoute(it, it.getAddress().length << 3);
            }
        }
        ParcelFileDescriptor conn = builder.establish();
        if (conn == null) {
            throw new NullConnectionException();
        }
        this.conn = conn;
        final int fd = conn.getFd();
        if (Build.VERSION.SDK_INT >= 24) {
            // we want REQUEST here instead of LISTEN
            connectivity.requestNetwork(defaultNetworkRequest, defaultNetworkCallback);
            listeningForDefaultNetwork = true;
        }
        String[] var56 = new String[]{(new File(this.getApplicationInfo().nativeLibraryDir, Executable.TUN2SOCKS)).getAbsolutePath(), "--netif-ipaddr", null, null, null, null, null, null, null, null, null, null, null, null, null};
        var56[2] = String.format(Locale.ENGLISH, PRIVATE_VLAN, new Object[]{"2"});
        var56[3] = "--netif-netmask";
        var56[4] = "255.255.255.0";
        var56[5] = "--socks-server-addr";
        var56[6] = DataStore.getListenAddress() + ':' + DataStore.getPortProxy();
        var56[7] = "--tunfd";
        var56[8] = String.valueOf(fd);
        var56[9] = "--tunmtu";
        var56[10] = String.valueOf(1500);
        var56[11] = "--sock-path";
        var56[12] = "sock_path";
        var56[13] = "--loglevel";
        var56[14] = "3";
        ArrayList cmd = CollectionsKt.arrayListOf(var56);
        if (profile.isIpv6()){
            cmd.add("--netif-ip6addr");
            cmd.add(String.format(Locale.ENGLISH, PRIVATE_VLAN6, new Object[]{"2"}));
        }
        cmd.add("--enable-udprelay");
        if (!profile.isUdpdns()) {
            cmd.add("--dnsgw");
            cmd.add("127.0.0.1:" + DataStore.getPortLocalDns());
        }
        getData().getProcesses().start(cmd, new Function0() {
            // $FF: synthetic method
            // $FF: bridge method
            @Override
            public Object invoke() {
                try {
                    VpnService.this.sendFd(fd);
                } catch (ErrnoException var2) {
                    VpnService.this.stopRunner(true, var2.getMessage());
                }
                return Unit.INSTANCE;
            }
        });
        return fd;
    }

    private void sendFd(int fd) throws ErrnoException{
        if (fd == -1) {
            try {
                throw new IOException("Invalid fd (-1)");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            int tries = 0;
            String path = (new File(Core.getDeviceStorage().getNoBackupFilesDir(), "sock_path")).getAbsolutePath();
            while(true) {
                try {
                    try {
                        Thread.sleep(50L << tries);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    JniHelper.sendFd(fd, path);
                    return;
                } catch (ErrnoException var5) {
                    if (tries > 5) {
                        throw var5;
                    }

                    ++tries;
                }
            }
        }
    }
}
