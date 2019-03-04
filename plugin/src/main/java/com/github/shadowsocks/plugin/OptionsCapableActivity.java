package com.github.shadowsocks.plugin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.Nullable;

/**
 * @date: 2019\2\22 0022
 * @author: zlx
 * @description:
 */
public abstract class OptionsCapableActivity extends AppCompatActivity {
    protected PluginOptions pluginOptions(Intent intent) {
        PluginOptions pluginOptions;
        try {
            pluginOptions = new PluginOptions(intent.getStringExtra(PluginContract.EXTRA_OPTIONS));
        } catch (IllegalArgumentException e) {
            pluginOptions = new PluginOptions();
        }
        return pluginOptions;
    }

    protected abstract void onInitializePluginOptions(@NotNull PluginOptions options);

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (savedInstanceState == null) {
            onInitializePluginOptions(pluginOptions(getIntent()));
        }
    }
}
