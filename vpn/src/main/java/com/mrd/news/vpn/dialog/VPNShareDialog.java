package com.mrd.news.vpn.dialog;

import android.content.Context;
import android.os.Bundle;

import com.mrd.news.vpn.BaseDialog;
import com.mrd.news.vpn.R;

import androidx.annotation.NonNull;

/**
 * @author 陈志鹏
 * @date 2019/2/20
 */
public class VPNShareDialog extends BaseDialog {

    public VPNShareDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_vpn_share);
    }
}
