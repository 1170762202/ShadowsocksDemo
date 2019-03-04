package com.github.shadowsocks.plugin;

import android.graphics.drawable.Drawable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author 陈志鹏
 * @date 2019/2/21
 */
public abstract class Plugin {
    @NotNull
    public abstract String getId();

    @NotNull
    public abstract CharSequence getLabel();

    @Nullable
    public Drawable getIcon() {
        return null;
    }

    @Nullable
    public String getDefaultConfig() {
        return null;
    }

    @NotNull
    public String getPackageName() {
        return "";
    }

    public boolean getTrusted() {
        return true;
    }
}