package com.github.shadowsocks;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.VpnService;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.github.shadowsocks.aidl.IShadowsocksService;
import com.github.shadowsocks.aidl.IShadowsocksServiceCallback;
import com.github.shadowsocks.bg.BaseService;
import com.github.shadowsocks.core.R;
import com.github.shadowsocks.utils.UtilsKt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;

/**
 * @author 陈志鹏
 * @date 2019/2/21
 */
public class VpnRequestActivity extends AppCompatActivity implements ShadowsocksConnection.Interface {
    private static final int REQUEST_CONNECT = 1;
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!BaseService.getUsingVpnMode()) {
            this.finish();
        } else {
            Object var10000 = ContextCompat.getSystemService(this, KeyguardManager.class);
            if (var10000 == null) {
                Intrinsics.throwNpe();
            }

            if (((KeyguardManager)var10000).isKeyguardLocked()) {
                this.receiver = UtilsKt.broadcastReceiver((Function2)(new Function2() {
                    // $FF: synthetic method
                    // $FF: bridge method
                    @Override
                    public Object invoke(Object var1, Object var2) {
                        this.invoke((Context)var1, (Intent)var2);
                        return Unit.INSTANCE;
                    }

                    public final void invoke(@NotNull Context $noName_0, @NotNull Intent $noName_1) {
                        VpnRequestActivity.this.getConnection().connect();
                    }
                }));
                this.registerReceiver(this.receiver, new IntentFilter(Intent.ACTION_USER_PRESENT));
            } else {
                this.getConnection().connect();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            Core.startService();
        } else {
            Toast.makeText((Context)this, R.string.vpn_permission_denied, Toast.LENGTH_LONG).show();
        }

        this.finish();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Nullable
    @Override
    public IShadowsocksServiceCallback getServiceCallback() {
        return DefaultImpls.getServiceCallback(this);
    }

    @NotNull
    @Override
    public ShadowsocksConnection getConnection() {
        return DefaultImpls.getConnection(this);
    }

    @Override
    public boolean getListenForDeath() {
        return DefaultImpls.getListenForDeath(this);
    }

    @Override
    public void onServiceConnected(@NotNull IShadowsocksService service) {
        Intent intent = VpnService.prepare((Context)this);
        if (intent == null) {
            this.onActivityResult(REQUEST_CONNECT, RESULT_OK, (Intent)null);
        } else {
            this.startActivityForResult(intent, REQUEST_CONNECT);
        }
    }

    @Override
    public void onServiceDisconnected() {
        DefaultImpls.onServiceDisconnected(this);
    }

    @Override
    public void binderDied() {
        DefaultImpls.binderDied(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.getConnection().disconnect();
        if (this.receiver != null) {
            this.unregisterReceiver(this.receiver);
        }
    }
}
