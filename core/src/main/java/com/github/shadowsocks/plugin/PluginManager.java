package com.github.shadowsocks.plugin;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.system.Os;
import android.util.Base64;
import android.util.Log;
import androidx.core.os.BundleKt;
import com.github.shadowsocks.Core;
import com.github.shadowsocks.core.R;
import com.github.shadowsocks.utils.UtilsKt;
import kotlin.Pair;
import kotlin.TuplesKt;
import kotlin.Unit;
import kotlin.collections.ArraysKt;
import kotlin.collections.CollectionsKt;
import kotlin.collections.SetsKt;
import kotlin.io.ByteStreamsKt;
import kotlin.io.FilesKt;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.RangesKt;
import kotlin.text.StringsKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;

/**
 * @author 陈志鹏
 * @date 2019/2/27
 */
public class PluginManager {

    private static int INT_MAX_POWER_OF_TWO = Integer.MAX_VALUE / 2 + 1;

    public static final class PluginNotFoundException extends FileNotFoundException {
        private final String plugin;

        @Override
        public String getLocalizedMessage() {
            return Core.app.getString(R.string.plugin_unknown, new Object[]{this.plugin});
        }

        public PluginNotFoundException(@NotNull String plugin) {
            super(plugin);
            this.plugin = plugin;
        }
    }

    public static Set<Signature> getTrustedSignatures() {
        Signature[] var10000 = UtilsKt.getSignaturesCompat(Core.getPackageInfo());
        return SetsKt.plus(SetsKt.plus(ArraysKt.toSet(var10000), new Signature(Base64.decode("|MIIDWzCCAkOgAwIBAgIEUzfv8DANBgkqhkiG9w0BAQsFADBdMQswCQYDVQQGEwJD" +
                        "|TjEOMAwGA1UECBMFTXlnb2QxDjAMBgNVBAcTBU15Z29kMQ4wDAYDVQQKEwVNeWdv" +
                        "|ZDEOMAwGA1UECxMFTXlnb2QxDjAMBgNVBAMTBU15Z29kMCAXDTE0MDUwMjA5MjQx" +
                        "|OVoYDzMwMTMwOTAyMDkyNDE5WjBdMQswCQYDVQQGEwJDTjEOMAwGA1UECBMFTXln" +
                        "|b2QxDjAMBgNVBAcTBU15Z29kMQ4wDAYDVQQKEwVNeWdvZDEOMAwGA1UECxMFTXln" +
                        "|b2QxDjAMBgNVBAMTBU15Z29kMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKC" +
                        "|AQEAjm5ikHoP3w6zavvZU5bRo6Birz41JL/nZidpdww21q/G9APA+IiJMUeeocy0" +
                        "|L7/QY8MQZABVwNq79LXYWJBcmmFXM9xBPgDqQP4uh9JsvazCI9bvDiMn92mz9HiS" +
                        "|Sg9V4KGg0AcY0r230KIFo7hz+2QBp1gwAAE97myBfA3pi3IzJM2kWsh4LWkKQMfL" +
                        "|M6KDhpb4mdDQnHlgi4JWe3SYbLtpB6whnTqjHaOzvyiLspx1tmrb0KVxssry9KoX" +
                        "|YQzl56scfE/QJX0jJ5qYmNAYRCb4PibMuNSGB2NObDabSOMAdT4JLueOcHZ/x9tw" +
                        "|agGQ9UdymVZYzf8uqc+29ppKdQIDAQABoyEwHzAdBgNVHQ4EFgQUBK4uJ0cqmnho" +
                        "|6I72VmOVQMvVCXowDQYJKoZIhvcNAQELBQADggEBABZQ3yNESQdgNJg+NRIcpF9l" +
                        "|YSKZvrBZ51gyrC7/2ZKMpRIyXruUOIrjuTR5eaONs1E4HI/uA3xG1eeW2pjPxDnO" +
                        "|zgM4t7EPH6QbzibihoHw1MAB/mzECzY8r11PBhDQlst0a2hp+zUNR8CLbpmPPqTY" +
                        "|RSo6EooQ7+NBejOXysqIF1q0BJs8Y5s/CaTOmgbL7uPCkzArB6SS/hzXgDk5gw6v" +
                        "|wkGeOtzcj1DlbUTvt1s5GlnwBTGUmkbLx+YUje+n+IBgMbohLUDYBtUHylRVgMsc" +
                        "|1WS67kDqeJiiQZvrxvyW6CZZ/MIGI+uAkkj3DqJpaZirkwPgvpcOIrjZy0uFvQM=", Base64.DEFAULT))),
                new Signature(Base64.decode("|MIICQzCCAaygAwIBAgIETV9OhjANBgkqhkiG9w0BAQUFADBmMQswCQYDVQQGEwJjbjERMA8GA1UE" +
                        "|CBMIU2hhbmdoYWkxDzANBgNVBAcTBlB1ZG9uZzEUMBIGA1UEChMLRnVkYW4gVW5pdi4xDDAKBgNV" +
                        "|BAsTA1BQSTEPMA0GA1UEAxMGTWF4IEx2MB4XDTExMDIxOTA1MDA1NFoXDTM2MDIxMzA1MDA1NFow" +
                        "|ZjELMAkGA1UEBhMCY24xETAPBgNVBAgTCFNoYW5naGFpMQ8wDQYDVQQHEwZQdWRvbmcxFDASBgNV" +
                        "|BAoTC0Z1ZGFuIFVuaXYuMQwwCgYDVQQLEwNQUEkxDzANBgNVBAMTBk1heCBMdjCBnzANBgkqhkiG" +
                        "|9w0BAQEFAAOBjQAwgYkCgYEAq6lA8LqdeEI+es9SDX85aIcx8LoL3cc//iRRi+2mFIWvzvZ+bLKr" +
                        "|4Wd0rhu/iU7OeMm2GvySFyw/GdMh1bqh5nNPLiRxAlZxpaZxLOdRcxuvh5Nc5yzjM+QBv8ECmuvu" +
                        "|AOvvT3UDmA0AMQjZqSCmxWIxc/cClZ/0DubreBo2st0CAwEAATANBgkqhkiG9w0BAQUFAAOBgQAQ" +
                        "|Iqonxpwk2ay+Dm5RhFfZyG9SatM/JNFx2OdErU16WzuK1ItotXGVJaxCZv3u/tTwM5aaMACGED5n" +
                        "|AvHaDGCWynY74oDAopM4liF/yLe1wmZDu6Zo/7fXrH+T03LBgj2fcIkUfN1AA4dvnBo8XWAm9VrI" +
                        "|1iNuLIssdhDz3IL9Yg==", Base64.DEFAULT)));
    }

    private static BroadcastReceiver receiver;
    private static Map<String, Plugin> cachedPlugins;

    @NotNull
    public static Map<String, Plugin> fetchPlugins() {
        synchronized (PluginManager.class) {
            Map var20;
            if (receiver == null) {
                receiver = Core.listenForPackageChanges(true, new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        synchronized (this) {
                            receiver = null;
                            cachedPlugins = null;
                        }
                        return Unit.INSTANCE;
                    }
                });
            }
            if (cachedPlugins == null) {
                PackageManager pm = Core.app.getPackageManager();
                List var10000 = pm.queryIntentContentProviders(new Intent(PluginContract.ACTION_NATIVE_PLUGIN), PackageManager.GET_META_DATA);
                ArrayList destination$iv$iv = new ArrayList(var10000.size());
                Iterator var7 = var10000.iterator();

                while (var7.hasNext()) {
                    Object item$iv$iv = var7.next();
                    ResolveInfo it = (ResolveInfo) item$iv$iv;
                    NativePlugin var12 = null;
                    try {
                        var12 = new NativePlugin(it);
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                    destination$iv$iv.add(var12);
                }

                var10000 = CollectionsKt.plus(destination$iv$iv, new NoPlugin());
                int capacity$iv = RangesKt.coerceAtLeast(mapCapacity(var10000.size()), 16);
                Map map = new LinkedHashMap(capacity$iv);
                Iterator var22 = var10000.iterator();

                while (var22.hasNext()) {
                    Plugin it = (Plugin) var22.next();
                    Pair var15 = TuplesKt.to(it.getId(), it);
                    map.put(var15.getFirst(), var15.getSecond());
                }

                cachedPlugins = map;
            }

            Map var24 = cachedPlugins;
            if (cachedPlugins == null) {
                Intrinsics.throwNpe();
            }

            var20 = var24;
            return var20;
        }
    }

    private static int mapCapacity(int expectedSize) {
        if (expectedSize < 3) {
            return expectedSize + 1;
        }
        if (expectedSize < INT_MAX_POWER_OF_TWO) {
            return expectedSize + expectedSize / 3;
        }
        return Integer.MAX_VALUE; // any large value
    }

    private static Uri buildUri(String id) {
        return (new Uri.Builder()).scheme(PluginContract.SCHEME).authority(PluginContract.AUTHORITY).path('/' + id).build();
    }

    @NotNull
    public static Intent buildIntent(@NotNull String id, @NotNull String action) {
        return new Intent(action, buildUri(id));
    }

    @Nullable
    public static String init(@NotNull PluginOptions options) throws Throwable {
        String var10000 = options.getId();
        if (var10000.length() == 0) {
            return null;
        }
        Throwable throwable = null;

        try {
            String path = initNative(options);
            if (path != null) {
                return path;
            }
        } catch (Throwable var4) {
            if (throwable == null) {
                throwable = var4;
            } else {
                UtilsKt.printLog(var4);
            }
        }

        Throwable var6 = throwable;
        if (throwable == null) {
            String var10002 = options.getId();
            var6 = new PluginNotFoundException(var10002);
        }

        throw var6;
    }

    private static String initNative(PluginOptions options) {
        PackageManager packageManager = Core.app.getPackageManager();
        String var10005 = options.getId();
        List providers = packageManager.queryIntentContentProviders(new Intent(PluginContract.ACTION_NATIVE_PLUGIN, buildUri(var10005)), 0);
        if (providers.isEmpty()) {
            return null;
        } else {
            Uri.Builder var8 = (new Uri.Builder()).scheme(ContentResolver.SCHEME_CONTENT);
            Uri uri = var8.authority(((ResolveInfo) CollectionsKt.single(providers)).providerInfo.authority).build();
            ContentResolver cr = Core.app.getContentResolver();

            String var5;
            try {
                var5 = initNativeFast(cr, options, uri);
            } catch (Throwable var7) {
                Log.e("PluginManager", "Initializing native plugin fast mode failed. Falling back to slow mode.");
                UtilsKt.printLog(var7);
                var5 = initNativeSlow(cr, options, uri);
            }
            return var5;
        }
    }

    private static String initNativeFast(ContentResolver cr, PluginOptions options, Uri uri) {
        Bundle var10000 = cr.call(uri, PluginContract.METHOD_GET_EXECUTABLE, (String) null, BundleKt.bundleOf(new Pair[]{new Pair(PluginContract.EXTRA_OPTIONS, options.getId())}));
        if (var10000 == null) {
            Intrinsics.throwNpe();
        }

        String var7 = var10000.getString(PluginContract.EXTRA_ENTRY);
        if (var7 == null) {
            Intrinsics.throwNpe();
        }

        String result = var7;
        boolean var5 = (new File(result)).canExecute();
        if (!var5) {
            String var6 = "Check failed.";
            throw new IllegalStateException(var6.toString());
        } else {
            return result;
        }
    }

    @SuppressLint({"Recycle"})
    private static String initNativeSlow(ContentResolver cr, PluginOptions options, Uri uri) {
        boolean initialized = false;
        File pluginDir = new File(Core.getDeviceStorage().getNoBackupFilesDir(), "plugin");
        Cursor cursor = cr.query(uri, new String[]{PluginContract.COLUMN_PATH, PluginContract.COLUMN_MODE}, (String) null, (String[]) null, (String) null);
        if (cursor == null) {
            return null;
        }
        try {
            if (!cursor.moveToFirst()) {
                entryNotFound();
            }

            FilesKt.deleteRecursively(pluginDir);
            if (!pluginDir.mkdirs()) {
                throw new FileNotFoundException("Unable to create plugin directory");
            }

            String pluginDirPath = pluginDir.getAbsolutePath() + '/';

            do {
                String path = cursor.getString(0);
                File file = new File(pluginDir, path);
                String var54 = file.getAbsolutePath();
                boolean var14 = StringsKt.startsWith(var54, pluginDirPath, false);
                if (!var14) {
                    String var55 = "Check failed.";
                    throw new IllegalStateException(var55.toString());
                }

                InputStream inStream = cr.openInputStream(uri.buildUpon().path(path).build());
                if (inStream == null) {
                    Intrinsics.throwNpe();
                }
                try {
                    FileOutputStream outStream = new FileOutputStream(file);
                    ByteStreamsKt.copyTo(inStream, outStream, 0);
                } catch (Throwable var46) {
                    throw var46;
                }

                int var57;
                switch (cursor.getType(1)) {
                    case Cursor.FIELD_TYPE_INTEGER:
                        var57 = cursor.getInt(1);
                        break;
                    case Cursor.FIELD_TYPE_STRING:
                        String var52 = cursor.getString(1);
                        var57 = Integer.parseInt(var52, checkRadix(8));
                        break;
                    default:
                        throw new IllegalArgumentException("File mode should be of type int");
                }

                Os.chmod(file.getAbsolutePath(), var57);
                if (Intrinsics.areEqual(path, options.getId())) {
                    initialized = true;
                }
            } while (cursor.moveToNext());
        } catch (Throwable var48) {
            var48.printStackTrace();
        }
        if (!initialized) {
            entryNotFound();
        }
        return (new File(pluginDir, options.getId())).getAbsolutePath();
    }

    private static void entryNotFound() {
        throw new IndexOutOfBoundsException("Plugin entry binary not found");
    }

    private static int checkRadix(int radix) {
        for (int i = Character.MIN_RADIX; i <= Character.MAX_RADIX; i++) {
            if (radix == i) {
                return radix;
            }
        }
        throw new IllegalArgumentException("radix $radix was not in valid range ${Character.MIN_RADIX..Character.MAX_RADIX}");
    }
}
