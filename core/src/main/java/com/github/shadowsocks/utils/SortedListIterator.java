package com.github.shadowsocks.utils;

import android.support.v7.util.SortedList;

/**
 * @author 陈志鹏
 * @date 2019/2/27
 */
public class SortedListIterator<T> extends ArrayIterator<T> {

    private SortedList<T> list;

    public SortedListIterator(SortedList<T> list){
        super();
        this.list = list;
    }

    @Override
    public int getSize() {
        return list.size();
    }

    @Override
    public T get(int index) {
        return list.get(index);
    }
}
