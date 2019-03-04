package com.github.shadowsocks.plugin;

import android.content.pm.ResolveInfo;
import android.os.Bundle;
import org.jetbrains.annotations.NotNull;

/**
 * @author 陈志鹏
 * @date 2019/2/21
 */
public final class NativePlugin extends ResolvedPlugin {

    public NativePlugin(@NotNull ResolveInfo resolveInfo) throws Throwable {
        super(resolveInfo);
        boolean var2 = resolveInfo.providerInfo != null;
        if (!var2) {
            String var3 = "Check failed.";
            throw (Throwable)(new IllegalStateException(var3.toString()));
        }
    }

    @Override
    @NotNull
    protected Bundle getMetaData() {
        return this.getResolveInfo().providerInfo.metaData;
    }

    @Override
    @NotNull
    public String getPackageName() {
        return this.getResolveInfo().providerInfo.packageName;
    }
}