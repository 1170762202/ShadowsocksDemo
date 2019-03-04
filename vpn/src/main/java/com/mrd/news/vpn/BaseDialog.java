package com.mrd.news.vpn;

import android.app.Dialog;
import android.content.Context;
import android.view.WindowManager;

import com.mrd.news.vpn.utils.ScreenUtils;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;

/**
 * @author 陈志鹏
 * @date 2018/1/31
 */

public abstract class BaseDialog extends Dialog{

    protected Context mContext;

    public BaseDialog(@NonNull Context context) {
        this(context, R.style.AppTheme_Dialog);
    }

    public BaseDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        mContext = context;
    }

    @Override
    public void show() {
        show(0.74);
    }

    public void show(double widthPercent) {
        super.show();
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = (int) (ScreenUtils.getScreenWidth(mContext) * widthPercent);
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(params);
    }
}