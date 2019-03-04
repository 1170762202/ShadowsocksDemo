package com.github.shadowsocks.plugin;

import android.content.Intent;
import org.jetbrains.annotations.NotNull;

/**
 * @date: 2019\2\22 0022
 * @author: zlx
 * @description:
 */
public abstract class HelpCallback extends HelpActivity {
   public abstract String produceHelpMessage(PluginOptions options);

    @Override
    protected void onInitializePluginOptions(@NotNull PluginOptions options) {
        Intent intent = new Intent();
        intent.putExtra(PluginContract.EXTRA_HELP_MESSAGE, produceHelpMessage(options));
        setResult(RESULT_OK,intent);
        finish();
    }
}
