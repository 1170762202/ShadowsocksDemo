package com.mrd.news.vpn;

import android.app.Activity;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.VpnService;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceDataStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.shadowsocks.Core;
import com.github.shadowsocks.ShadowsocksConnection;
import com.github.shadowsocks.aidl.IShadowsocksService;
import com.github.shadowsocks.aidl.IShadowsocksServiceCallback;
import com.github.shadowsocks.aidl.TrafficStats;
import com.github.shadowsocks.bg.BaseService;
import com.github.shadowsocks.bg.Executable;
import com.github.shadowsocks.database.Profile;
import com.github.shadowsocks.database.ProfileManager;
import com.github.shadowsocks.preference.DataStore;
import com.github.shadowsocks.preference.OnPreferenceDataStoreChangeListener;
import com.github.shadowsocks.utils.Key;
import com.mrd.news.vpn.dialog.VPNShareDialog;
import com.mrd.news.vpn.widget.TitleBar;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.SQLException;


/**
 * @author 陈志鹏
 * @date 2019/2/20
 */
public class VPNActivity extends AppCompatActivity implements View.OnClickListener, ShadowsocksConnection.Interface, OnPreferenceDataStoreChangeListener {

    private static final int REQUEST_CONNECT = 1;
    private static final int REQUEST_ROUTE_SELECTION = 2;

    private TitleBar mTbTitle;
    private ConstraintLayout mClRoute;
    private ConstraintLayout mClRemainingFlow;
    private TextView mTvTry;
    private TextView mTvObtainFlow;
    private ImageView mIvSwitch;
    private TextView mTvStatus;
    private ImageView mIvCountry;
    private TextView mTvCountry;

    private IShadowsocksServiceCallback.Stub serviceCallback = new IShadowsocksServiceCallback.Stub() {
        @Override
        public void stateChanged(final int state, String profileName, final String msg) throws RemoteException {
            Core.handler.post(new Runnable() {
                @Override
                public void run() {
                    changeState(state);
                }
            });
        }

        @Override
        public void trafficUpdated(long profileId, TrafficStats stats) throws RemoteException {

        }

        @Override
        public void trafficPersisted(long profileId) throws RemoteException {

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vpn);
        initView();
        initEvent();
        initData();
    }

    private void initView() {
        mTbTitle = findViewById(R.id.tb_title);
        mClRoute = findViewById(R.id.cl_route);
        mClRemainingFlow = findViewById(R.id.cl_remaining_flow);
        mTvTry = findViewById(R.id.tv_try);
        mTvObtainFlow = findViewById(R.id.tv_obtain_flow);
        mIvSwitch = findViewById(R.id.iv_switch);
        mTvStatus = findViewById(R.id.tv_status);
        mIvCountry = findViewById(R.id.iv_country);
        mTvCountry = findViewById(R.id.tv_country);
        mTbTitle.setTitle("VPN");
        mTbTitle.setBackTextString("发现");
    }

    private void initEvent() {
        mClRoute.setOnClickListener(this);
        mClRemainingFlow.setOnClickListener(this);
        mTvTry.setOnClickListener(this);
        mTvObtainFlow.setOnClickListener(this);
        mIvSwitch.setOnClickListener(this);

        changeState(BaseService.IDLE);
        Core.handler.post(new Runnable() {
            @Override
            public void run() {
                getConnection().connect();
            }
        });
        DataStore.publicStore.registerChangeListener(this);
    }

    private void initData() {
        try {
            ProfileManager.clear();

            Profile profile1 = new Profile();
            profile1.setHost("103.115.44.168");
            profile1.setIpv6(true);
            profile1.setMethod("aes-256-cfb");
            profile1.setPassword("123456");
            profile1.setRemoteDns("8.8.8.8");
            profile1.setRemotePort(2444);
            profile1.setRoute("all");
            profile1.setUdpdns(false);
            profile1.setName("线路1");
            ProfileManager.createProfile(profile1);

            Profile profile2 = new Profile();
            profile2.setHost("199.247.28.183");
            profile2.setIpv6(true);
            profile2.setMethod("aes-256-cfb");
            profile2.setPassword("czp,123");
            profile2.setRemoteDns("8.8.8.8");
            profile2.setRemotePort(1314);
            profile2.setRoute("all");
            profile2.setUdpdns(false);
            profile2.setName("线路2");
            ProfileManager.createProfile(profile2);

            Core.switchProfile(profile1.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        updateSelectedRoute();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.cl_route) {
            startActivityForResult(new Intent(this, RouteSelectionActivity.class), REQUEST_ROUTE_SELECTION);
        } else if (i == R.id.cl_remaining_flow) {
        } else if (i == R.id.tv_try) {
            toWeb("https://www.google.com.hk");
        } else if (i == R.id.tv_obtain_flow) {
            new VPNShareDialog(this).show();
        } else if (i == R.id.iv_switch) {
            toggle();
        } else {
        }
    }

    private void changeState(int state) {
        Core.currentState = state;
        switch (state) {
            case BaseService.CONNECTING:
                mTvStatus.setText(getResources().getString(R.string.connecting));
                break;
            case BaseService.CONNECTED:
                mTvStatus.setText(getResources().getString(R.string.vpn_connected));
                mIvSwitch.setImageResource(R.drawable.icon_switch_open);
                break;
            case BaseService.STOPPING:
                mTvStatus.setText(getResources().getString(R.string.stopping));
                break;
            case BaseService.STOPPED:
                mTvStatus.setText(getResources().getString(R.string.not_connected));
                mIvSwitch.setImageResource(R.drawable.icon_switch_close);
                break;
            default:
                break;
        }
    }

    private void updateSelectedRoute() {
        Profile profile = null;
        try {

            profile = ProfileManager.getProfile(DataStore.getProfileId());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (profile == null) {
            mIvCountry.setVisibility(View.GONE);
            mTvCountry.setText("未知");
        } else {
            mIvCountry.setImageResource(R.drawable.icon_china);
            mIvCountry.setVisibility(View.VISIBLE);
            mTvCountry.setText(profile.getName());
        }
    }

    private void toggle() {
        if (Core.currentState == BaseService.CONNECTED) {
            Core.stopService();
        } else if (BaseService.getUsingVpnMode()) {
            Intent intent = VpnService.prepare((Context) this);
            if (intent != null) {
                this.startActivityForResult(intent, REQUEST_CONNECT);
            } else {
                this.onActivityResult(REQUEST_CONNECT, Activity.RESULT_OK, null);
            }
        } else {
            Core.startService();
        }
    }

    private void toWeb(String url) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    @Override
    public void onPreferenceDataStoreChanged(@NotNull PreferenceDataStore store, @org.jetbrains.annotations.Nullable String key) {
        if (key != null) {
            if (key.equals(Key.serviceMode)) {
                Core.handler.post((Runnable) (new Runnable() {
                    @Override
                    public void run() {
                        VPNActivity.this.getConnection().disconnect();
                        VPNActivity.this.getConnection().connect();
                    }
                }));
            }
        }
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public IShadowsocksServiceCallback getServiceCallback() {
        return serviceCallback;
    }

    @NotNull
    @Override
    public ShadowsocksConnection getConnection() {
        return DefaultImpls.getConnection(this);
    }

    @Override
    public boolean getListenForDeath() {
        return true;
    }

    @Override
    public void onServiceConnected(@NotNull IShadowsocksService service) {
        int state;
        try {
            state = service.getState();
        } catch (Exception var5) {
            state = BaseService.IDLE;
        }
        changeState(state);
    }

    @Override
    public void onServiceDisconnected() {
        changeState(BaseService.IDLE);
    }

    @Override
    public void binderDied() {
        DefaultImpls.binderDied(this);
        Core.handler.post((Runnable) (new Runnable() {
            @Override
            public void run() {
                VPNActivity.this.getConnection().disconnect();
                Executable.killAll();
                VPNActivity.this.getConnection().connect();
            }
        }));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUEST_CONNECT:
                Core.startService();
                break;
            case REQUEST_ROUTE_SELECTION:
                long profileId = data.getLongExtra("selectedId", -1);
                if (profileId < 0) {
                    return;
                }
                Core.switchProfile(profileId);
                if (Core.currentState == BaseService.CONNECTED) {
                    Core.reloadService();
                }
                updateSelectedRoute();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        getConnection().setListeningForBandwidth(true);
    }

    @Override
    protected void onStop() {
        getConnection().setListeningForBandwidth(false);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataStore.publicStore.unregisterChangeListener(this);
        this.getConnection().disconnect();
        (new BackupManager(this)).dataChanged();
        Core.handler.removeCallbacksAndMessages(null);
    }

    @Override
    public boolean onKeyShortcut(int keyCode, KeyEvent event) {
        boolean result;
        if (keyCode == KeyEvent.KEYCODE_G && event.hasModifiers(KeyEvent.META_CTRL_ON)) {
            this.toggle();
            result = true;
        } else {
            result = super.onKeyShortcut(keyCode, event);
        }

        return result;
    }
}
