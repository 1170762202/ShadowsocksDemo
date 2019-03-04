package com.github.shadowsocks.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import kotlin.jvm.JvmOverloads;

/**
 * @author 陈志鹏
 * @date 2019/2/21
 */
public class AutoCollapseTextView extends AppCompatTextView {

    @JvmOverloads
    public AutoCollapseTextView(@NotNull Context context, @Nullable AttributeSet attrs) {
        this(context,attrs,0);
    }

    @JvmOverloads
    public AutoCollapseTextView(@NotNull Context context) {
        this(context, null, 0);
    }

    @JvmOverloads
    public AutoCollapseTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onTextChanged(@Nullable CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        boolean var9 = text == null || text.length() == 0;
        this.setVisibility(var9 ? GONE : VISIBLE);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, @Nullable Rect previouslyFocusedRect) {
        try {
            super.onFocusChanged(focused, direction, previouslyFocusedRect);
        } catch (IndexOutOfBoundsException var5) {
            var5.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(@Nullable MotionEvent event) {
        boolean var2;
        try {
            var2 = super.onTouchEvent(event);
        } catch (IndexOutOfBoundsException var4) {
            var4.printStackTrace();
            var2 = false;
        }

        return var2;
    }
}
