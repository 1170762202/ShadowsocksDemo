package com.mrd.news.vpn;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.shadowsocks.Core;
import com.github.shadowsocks.bg.BaseService;
import com.github.shadowsocks.database.Profile;
import com.github.shadowsocks.database.ProfileManager;
import com.github.shadowsocks.preference.DataStore;
import com.mrd.news.vpn.adapter.RouteSelectionAdapter;
import com.mrd.news.vpn.bean.PingBean;
import com.mrd.news.vpn.utils.PingUtils;
import com.mrd.news.vpn.widget.TitleBar;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class RouteSelectionActivity extends AppCompatActivity {

    private TitleBar mTbTitle;
    private RecyclerView mRvContent;
    private RouteSelectionAdapter mAdapter;
    private HashMap<Long, String> mSpeedMap = new HashMap<>();
    private CompositeDisposable mDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_selection);
        initView();
        initEvent();
        initData();
    }

    private void initView() {
        mTbTitle = findViewById(R.id.tb_title);
        mRvContent = findViewById(R.id.rv_content);
        mTbTitle.setBackTextString("VPN");
        mTbTitle.setRightTextString("测速");

        mRvContent.setLayoutManager(new LinearLayoutManager(this));
        mRvContent.setHasFixedSize(true);
        mRvContent.setItemAnimator(null);
        mAdapter = new RouteSelectionAdapter(mSpeedMap);
        mRvContent.setAdapter(mAdapter);
    }

    private void initEvent() {
        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (isEnabled()) {
                    long profileId = mAdapter.getData().get(position).getId();
                    Intent intent = new Intent();
                    intent.putExtra("selectedId", profileId);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
        mTbTitle.setOnRightTextClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testSpeed();
            }
        });
    }

    private void initData() {
        try {
            List<Profile> profiles = ProfileManager.getAllProfiles();
            mAdapter.setNewData(profiles);

            ProfileManager.ensureNotEmpty();
            mRvContent.smoothScrollToPosition(mAdapter.getProfilePos(DataStore.getProfileId()));
            ProfileManager.listener = mAdapter;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        testSpeed();
    }

    private void testSpeed() {
        List<Profile> profiles = mAdapter.getData();
        for (int i = 0; i < profiles.size(); i++) {
            Profile profile = profiles.get(i);
            testSpeed(profile, i);
        }
    }

    private void testSpeed(final Profile profile, final int pos) {
        Disposable disposable = Observable.create(new ObservableOnSubscribe<PingBean>() {
            @Override
            public void subscribe(ObservableEmitter<PingBean> emitter) throws Exception {
                PingBean bean = new PingBean(profile.getHost(), 1, 5, null, pos);
                PingUtils.ping(bean);
                emitter.onNext(bean);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<PingBean>() {
                    @Override
                    public void accept(PingBean pingBean) throws Exception {
                        Profile profile = mAdapter.getData().get(pingBean.getPos());
                        if (profile != null) {
                            mSpeedMap.put(profile.getId(), pingBean.getPingTime());
                            mAdapter.notifyItemChanged(pingBean.getPos());
                        }
                    }
                });
        mDisposable.add(disposable);
    }

    private boolean isEnabled() {
        int currentState = Core.currentState;
        if (currentState == BaseService.CONNECTED || currentState == BaseService.STOPPED) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        ProfileManager.listener = null;
        super.onDestroy();
        if (mDisposable != null) {
            mDisposable.dispose();
        }
    }
}
