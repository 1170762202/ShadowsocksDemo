package com.github.shadowsocks.utils;

import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.io.TextStreamsKt;
import kotlin.jvm.functions.Function0;
import kotlin.text.Charsets;
import kotlin.text.MatchResult;
import kotlin.text.Regex;
import kotlin.text.StringsKt;
import org.jetbrains.annotations.Nullable;

import java.io.*;

/**
 * @author 陈志鹏
 * @date 2019/2/26
 */
public class TcpFastOpen {
    private static final String PATH = "/proc/sys/net/ipv4/tcp_fastopen";

    private static boolean supported;

    static {
        if (new File(PATH).canRead()){
            supported = true;
        }else{
            String var2 = "^(\\d+)\\.(\\d+)\\.(\\d+)";
            Regex var10000 = new Regex(var2);
            String var10001 = System.getProperty("os.version");
            MatchResult match = var10000.find(var10001 != null ? var10001 : "", 0);
            if (match == null) {
                supported =  false;
            } else {
                String var3 = (String)match.getGroupValues().get(1);
                int var5 = Integer.parseInt(var3);
                if (Integer.MIN_VALUE <= var5) {
                    if (2 >= var5) {
                        supported =  false;
                    }
                }

                if (var5 == 3) {
                    String var4 = (String)match.getGroupValues().get(2);
                    int var6 = Integer.parseInt(var4);
                    if (Integer.MIN_VALUE <= var6) {
                        if (6 >= var6) {
                            supported =  false;
                        }
                    }

                    if (var6 == 7) {
                        var4 = (String)match.getGroupValues().get(3);
                        supported = Integer.parseInt(var4) >= 1;
                    } else {
                        supported = true;
                    }
                } else {
                    supported =  true;
                }
            }
        }
    }

    public static boolean getSendEnabled() {
        File file = new File("/proc/sys/net/ipv4/tcp_fastopen");
        boolean var10000 = false;
        try {
            if (file.canRead()) {
                InputStream var7 = (InputStream)(new FileInputStream(file));
                Reader var6 = (Reader)(new InputStreamReader(var7, Charsets.UTF_8));
                Closeable var2 = (Closeable)(var6 instanceof BufferedReader ? (BufferedReader)var6 : new BufferedReader(var6, 8192));

                String var15 = null;
                try {
                    BufferedReader it = (BufferedReader)var2;
                    var15 = TextStreamsKt.readText((Reader)it);
                } catch (Throwable var10) {
                    var10.printStackTrace();
                }

                if (var15 == null) {
                    throw new TypeCastException("null cannot be cast to non-null type kotlin.CharSequence");
                }

                String var12 = StringsKt.trim((CharSequence)var15).toString();
                var10000 = (Integer.parseInt(var12) & 1) > 0;
            } else {
                var10000 = supported;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return var10000;
    }

    @Nullable
    public static String enable() {
        try {
            Process process = new ProcessBuilder(new String[]{"su", "-c", "echo 3 > /proc/sys/net/ipv4/tcp_fastopen"}).redirectErrorStream(true).start();
            InputStream var8 = process.getInputStream();
            Reader var4 = (Reader)(new InputStreamReader(var8, Charsets.UTF_8));
            return TextStreamsKt.readText((Reader)(var4 instanceof BufferedReader ? (BufferedReader)var4 : new BufferedReader(var4, 8192)));
        }catch (IOException e){
            e.printStackTrace();
            return e.getLocalizedMessage();
        }
    }

    public static void enableAsync() throws InterruptedException {
        Thread thread = UtilsKt.thread("TcpFastOpen", false, false, (ClassLoader) null, 0, new Function0() {
            @Override
            public Object invoke() {
                enable();
                return Unit.INSTANCE;
            }
        });
        thread.join(1000L);
    }
}
