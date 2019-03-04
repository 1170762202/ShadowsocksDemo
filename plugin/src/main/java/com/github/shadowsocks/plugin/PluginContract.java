package com.github.shadowsocks.plugin;

import org.jetbrains.annotations.NotNull;

/**
 * @date: 2019\2\22 0022
 * @author: zlx
 * @description:
 */
public final class PluginContract {
    @NotNull
    public static final String ACTION_NATIVE_PLUGIN = "com.github.shadowsocks.plugin.ACTION_NATIVE_PLUGIN";
    @NotNull
    public static final String ACTION_CONFIGURE = "com.github.shadowsocks.plugin.ACTION_CONFIGURE";
    @NotNull
    public static final String ACTION_HELP = "com.github.shadowsocks.plugin.ACTION_HELP";
    @NotNull
    public static final String EXTRA_ENTRY = "com.github.shadowsocks.plugin.EXTRA_ENTRY";
    @NotNull
    public static final String EXTRA_OPTIONS = "com.github.shadowsocks.plugin.EXTRA_OPTIONS";
    @NotNull
    public static final String EXTRA_HELP_MESSAGE = "com.github.shadowsocks.plugin.EXTRA_HELP_MESSAGE";
    @NotNull
    public static final String METADATA_KEY_ID = "com.github.shadowsocks.plugin.id";
    @NotNull
    public static final String METADATA_KEY_DEFAULT_CONFIG = "com.github.shadowsocks.plugin.default_config";
    @NotNull
    public static final String METHOD_GET_EXECUTABLE = "shadowsocks:getExecutable";
    public static final int RESULT_FALLBACK = 1;
    @NotNull
    public static final String COLUMN_PATH = "path";
    @NotNull
    public static final String COLUMN_MODE = "mode";
    @NotNull
    public static final String SCHEME = "plugin";
    @NotNull
    public static final String AUTHORITY = "com.github.shadowsocks";
    public static final PluginContract INSTANCE;

    static {
        PluginContract var0 = new PluginContract();
        INSTANCE = var0;
    }
}
