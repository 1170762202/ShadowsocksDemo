package com.github.shadowsocks.preference;

import android.support.v7.preference.PreferenceDataStore;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author 陈志鹏
 * @date 2019/2/21
 */
public interface OnPreferenceDataStoreChangeListener {
    void onPreferenceDataStoreChanged(@NotNull PreferenceDataStore var1, @Nullable String var2);
}
