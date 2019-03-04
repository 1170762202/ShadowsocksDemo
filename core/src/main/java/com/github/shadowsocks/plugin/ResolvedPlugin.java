package com.github.shadowsocks.plugin;

import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import com.github.shadowsocks.Core;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author 陈志鹏
 * @date 2019/2/21
 */
public abstract class ResolvedPlugin extends Plugin {

    private ResolveInfo resolveInfo;

    public ResolvedPlugin(ResolveInfo resolveInfo){
        super();
        this.resolveInfo = resolveInfo;
    }

    @NotNull
    protected abstract Bundle getMetaData();

    @NotNull
    @Override
    public String getId() {
        return getMetaData().getString(PluginContract.METADATA_KEY_ID);
    }

    @NotNull
    @Override
    public CharSequence getLabel() {
       return resolveInfo.loadLabel(Core.app.getPackageManager());
    }

    @Nullable
    @Override
    public Drawable getIcon() {
        return resolveInfo.loadIcon(Core.app.getPackageManager());
    }

    @Nullable
    @Override
    public String getDefaultConfig() {
        return getMetaData().getString(PluginContract.METADATA_KEY_DEFAULT_CONFIG);
    }

    @NotNull
    @Override
    public String getPackageName() {
        return resolveInfo.resolvePackageName;
    }

    @Override
    public boolean getTrusted() {
        return PluginManager.getTrustedSignatures().contains(Core.getPackageInfo(getPackageName()));
    }

    public ResolveInfo getResolveInfo() {
        return resolveInfo;
    }
}