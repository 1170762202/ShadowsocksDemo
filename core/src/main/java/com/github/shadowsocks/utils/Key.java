package com.github.shadowsocks.utils;

import org.jetbrains.annotations.NotNull;

/**
 * @author 陈志鹏
 * @date 2019/2/26
 */
public interface Key {
    @NotNull
    public static final String DB_PUBLIC = "config.db";
    @NotNull
    public static final String DB_PROFILE = "profile.db";
    @NotNull
    public static final String id = "profileId";
    @NotNull
    public static final String name = "profileName";
    @NotNull
    public static final String individual = "Proxyed";
    @NotNull
    public static final String serviceMode = "serviceMode";
    @NotNull
    public static final String modeProxy = "proxy";
    @NotNull
    public static final String modeVpn = "vpn";
    @NotNull
    public static final String modeTransproxy = "transproxy";
    @NotNull
    public static final String shareOverLan = "shareOverLan";
    @NotNull
    public static final String portProxy = "portProxy";
    @NotNull
    public static final String portLocalDns = "portLocalDns";
    @NotNull
    public static final String portTransproxy = "portTransproxy";
    @NotNull
    public static final String route = "route";
    @NotNull
    public static final String isAutoConnect = "isAutoConnect";
    @NotNull
    public static final String directBootAware = "directBootAware";
    @NotNull
    public static final String proxyApps = "isProxyApps";
    @NotNull
    public static final String bypass = "isBypassApps";
    @NotNull
    public static final String udpdns = "isUdpDns";
    @NotNull
    public static final String ipv6 = "isIpv6";
    @NotNull
    public static final String host = "proxy";
    @NotNull
    public static final String password = "sitekey";
    @NotNull
    public static final String method = "encMethod";
    @NotNull
    public static final String remotePort = "remotePortNum";
    @NotNull
    public static final String remoteDns = "remoteDns";
    @NotNull
    public static final String plugin = "plugin";
    @NotNull
    public static final String pluginConfigure = "plugin.configure";
    @NotNull
    public static final String udpFallback = "udpFallback";
    @NotNull
    public static final String dirty = "profileDirty";
    @NotNull
    public static final String tfo = "tcp_fastopen";
    @NotNull
    public static final String assetUpdateTime = "assetUpdateTime";
    @NotNull
    public static final String controlStats = "control.stats";
    @NotNull
    public static final String controlImport = "control.import";
    @NotNull
    public static final String controlExport = "control.export";
    @NotNull
    public static final String about = "about";
}
