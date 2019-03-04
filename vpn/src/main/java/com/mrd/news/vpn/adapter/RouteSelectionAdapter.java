package com.mrd.news.vpn.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.shadowsocks.database.Profile;
import com.github.shadowsocks.database.ProfileManager;
import com.github.shadowsocks.preference.DataStore;
import com.mrd.news.vpn.R;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * @author 陈志鹏
 * @date 2019/2/21
 */
public class RouteSelectionAdapter extends BaseQuickAdapter<Profile, BaseViewHolder> implements ProfileManager.Listener {

    private HashMap<Long, String> mSpeedMap;

    public RouteSelectionAdapter(HashMap<Long, String> speedMap) {
        super(R.layout.item_route_selection);
        mSpeedMap = speedMap;
    }

    @Override
    protected void convert(BaseViewHolder helper, Profile item) {
        helper.setImageResource(R.id.iv_country, R.drawable.icon_china);
        helper.setText(R.id.tv_route, item.getName());
        if (item.getId() == DataStore.getProfileId()) {
            helper.setImageResource(R.id.iv_select, R.drawable.icon_vpn_selected);
        } else {
            helper.setImageResource(R.id.iv_select, R.drawable.icon_vpn_unselected);
        }
        helper.setText(R.id.tv_speed, mSpeedMap.get(item.getId()));
    }

    @Override
    public void onAdd(@NotNull Profile profile) {
        addData(profile);
    }

    @Override
    public void onRemove(long profileId) {
        int index = getProfilePos(profileId);
        if (index < 0) {
            return;
        }
        remove(index);
        if (profileId == DataStore.getProfileId()) {
            DataStore.setProfileId(0);
        }
    }

    public int getProfilePos(long profileId) {
        for (int i = 0; i < getData().size(); i++) {
            long id = getData().get(i).getId();
            if (id == profileId) {
                return i;
            }
        }
        return -1;
    }
}
