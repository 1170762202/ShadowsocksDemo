package com.github.shadowsocks.bg;

import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import com.github.shadowsocks.Core;
import kotlin.collections.CollectionsKt;
import kotlin.collections.SetsKt;
import kotlin.io.FilesKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.Charsets;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.Set;

/**
 * @author 陈志鹏
 * @date 2019/2/21
 */
public final class Executable {
    @NotNull
    public static final String REDSOCKS = "libredsocks.so";
    @NotNull
    public static final String SS_LOCAL = "libss-local.so";
    @NotNull
    public static final String SS_TUNNEL = "libss-tunnel.so";
    @NotNull
    public static final String TUN2SOCKS = "libtun2socks.so";
    @NotNull
    public static final String OVERTURE = "liboverture.so";
    private static final Set EXECUTABLES;

    static {
        EXECUTABLES = SetsKt.setOf(new String[]{"libss-local.so", "libss-tunnel.so", "libredsocks.so", "libtun2socks.so", "liboverture.so"});
    }

    public static final void killAll() {
        File[] var3 = (new File("/proc")).listFiles();

        for(int var2 = 0; var2 < var3.length; ++var2) {
            File process = var3[var2];

            String var6 = FilesKt.readText(new File(process, "cmdline"), Charsets.UTF_8);
            String[] split = var6.split(String.valueOf(Character.MIN_VALUE), 2);
            String var11 = (String) CollectionsKt.first(Arrays.asList(split));
            File exe = new File(var11);
            if (Intrinsics.areEqual(exe.getParent(), Core.app.getApplicationInfo().nativeLibraryDir) && EXECUTABLES.contains(exe.getName())) {
                try {
                    Intrinsics.checkExpressionValueIsNotNull(process, "process");
                    String var15 = process.getName();
                    Intrinsics.checkExpressionValueIsNotNull(var15, "process.name");
                    var6 = var15;
                    Os.kill(Integer.parseInt(var6), OsConstants.SIGKILL);
                } catch (ErrnoException var12) {
                    if (var12.errno != OsConstants.ESRCH) {
                        var12.printStackTrace();
                    }
                }
            }
        }

    }
}
