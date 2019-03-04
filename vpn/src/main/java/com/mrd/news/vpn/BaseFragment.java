package com.mrd.news.vpn;

import android.support.v4.app.Fragment;

import com.github.shadowsocks.aidl.TrafficStats;

/**
 * @author 陈志鹏
 * @date 2019/2/20
 */
public class BaseFragment extends Fragment {

    public void onTrafficUpdated(long profileId, TrafficStats stats){}
}
