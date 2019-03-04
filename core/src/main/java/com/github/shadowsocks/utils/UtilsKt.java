package com.github.shadowsocks.utils;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.util.TypedValue;

import com.github.shadowsocks.JniHelper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URLConnection;
import java.net.UnknownHostException;

import androidx.annotation.AttrRes;
import kotlin.concurrent.ThreadsKt;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt;

/**
 * @author 陈志鹏
 * @date 2019/2/27
 */
public class UtilsKt {
    public static boolean isNumericAddress(@NotNull String str) {
        return JniHelper.parseNumericAddress(str) != null;
    }

    @Nullable
    public static final InetAddress parseNumericAddress(@NotNull String str) {
        byte[] addr = JniHelper.parseNumericAddress(str);
        try {
            return addr == null ? null : InetAddress.getByAddress(str, addr);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int parsePort(@Nullable String str, int var1){
        return parsePort(str,var1,1025);
    }

    public static int parsePort(@Nullable String str, int var1, int min) {
        int var4;
        label21:
        {
            if (str != null) {
                Integer var10000 = StringsKt.toIntOrNull(str);
                if (var10000 != null) {
                    var4 = var10000;
                    break label21;
                }
            }

            var4 = var1;
        }

        int value = var4;
        return value >= min && value <= 65535 ? value : var1;
    }

    @NotNull
    public static final BroadcastReceiver broadcastReceiver(@NotNull final Function2 callback) {
        return (BroadcastReceiver) (new BroadcastReceiver() {
            @Override
            public void onReceive(@NotNull Context context, @NotNull Intent intent) {
                callback.invoke(context, intent);
            }
        });
    }

    @NotNull
    public static final Thread thread(@Nullable String name, Function0 block) {
        return thread(name, true, false, null, -1, block);
    }

    @NotNull
    public static final Thread thread(@Nullable String name, boolean start, boolean isDaemon, @Nullable ClassLoader contextClassLoader, int priority, @NotNull Function0 block) {
        Thread thread = ThreadsKt.thread(false, isDaemon, contextClassLoader, name, priority, block);
        if (start) {
            thread.start();
        }

        return thread;
    }

    public static final long getResponseLength(@NotNull URLConnection connection) {
        return Build.VERSION.SDK_INT >= 24 ? connection.getContentLengthLong() : (long) connection.getContentLength();
    }

    public static final Bitmap openBitmap(@NotNull ContentResolver resolver, @NotNull Uri uri) {
        try {
            return Build.VERSION.SDK_INT >= 28 ? ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, uri)) : BitmapFactory.decodeStream(resolver.openInputStream(uri));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static final Signature[] getSignaturesCompat(@NotNull PackageInfo packageInfo) {
        Signature[] var1;
        if (Build.VERSION.SDK_INT >= 28) {
            var1 = packageInfo.signingInfo.getApkContentsSigners();
        } else {
            var1 = packageInfo.signatures;
        }

        return var1;
    }

    public static final int resolveResourceId(@NotNull Resources.Theme theme, @AttrRes int resId) {
        TypedValue typedValue = new TypedValue();
        if (!theme.resolveAttribute(resId, typedValue, true)) {
            throw new Resources.NotFoundException();
        } else {
            return typedValue.resourceId;
        }
    }

    public static final void printLog(@NotNull Throwable t) {
        t.printStackTrace();
    }

    public static final boolean remove(@NotNull Preference preference) {
        PreferenceGroup var10000 = preference.getParent();
        if (var10000 == null) {
            Intrinsics.throwNpe();
        }

        return var10000.removePreference(preference);
    }
}
