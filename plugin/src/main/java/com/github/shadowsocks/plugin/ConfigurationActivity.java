package com.github.shadowsocks.plugin;

import android.app.Activity;
import android.content.Intent;
import org.jetbrains.annotations.NotNull;

/**
 * @date: 2019\2\22 0022
 * @author: zlx
 * @description:
 */
public abstract class ConfigurationActivity extends OptionsCapableActivity {
    /**
     * Equivalent to setResult(RESULT_CANCELED).
     */
    public final void discardChanges() {
        this.setResult(Activity.RESULT_CANCELED);
    }

    /**
     * Equivalent to setResult(RESULT_OK, args_with_correct_format).
     *
     * @param options PluginOptions to save.
     */
    public final void saveChanges(@NotNull PluginOptions options) {
        Intent intent = new Intent().putExtra(PluginContract.EXTRA_OPTIONS, options.toString());
        this.setResult(Activity.RESULT_OK, intent);
    }

    /**
     * Finish this activity and request manual editor to pop up instead.
     */
    public final void fallbackToManualEditor() {
        this.setResult(PluginContract.RESULT_FALLBACK);
        this.finish();
    }
}
