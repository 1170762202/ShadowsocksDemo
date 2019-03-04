package com.github.shadowsocks.plugin;

import com.github.shadowsocks.Core;
import org.jetbrains.annotations.NotNull;

/**
 * @author 陈志鹏
 * @date 2019/2/21
 */
public final class NoPlugin extends Plugin {

    @Override
    @NotNull
    public String getId() {
        return "";
    }

    @Override
    @NotNull
    public CharSequence getLabel() {
        return Core.app.getText(com.github.shadowsocks.core.R.string.plugin_disabled);
    }
}