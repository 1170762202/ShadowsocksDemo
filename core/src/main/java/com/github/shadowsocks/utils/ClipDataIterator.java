package com.github.shadowsocks.utils;

import android.content.ClipData;

/**
 * @author 陈志鹏
 * @date 2019/2/27
 */
public class ClipDataIterator extends ArrayIterator<ClipData.Item> {

    private ClipData data;

    public ClipDataIterator(ClipData data){
        super();
        this.data = data;
    }

    @Override
    public int getSize() {
        return data.getItemCount();
    }

    @Override
    public ClipData.Item get(int index) {
        return data.getItemAt(index);
    }
}
