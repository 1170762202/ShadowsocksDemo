package com.github.shadowsocks.utils;

import android.content.ClipData;
import android.support.v7.util.SortedList;

import org.json.JSONArray;

import java.util.Iterator;


/**
 * @author 陈志鹏
 * @date 2019/2/27
 */
public class IteratorUtils {
    public static Iterator asIterable(ClipData data){
        return new ClipDataIterator(data);
    }
    public static Iterator<Object> asIterable(JSONArray array){
        return new JSONArrayIterator(array);
    }
    public static Iterator<?> asIterable(SortedList list){
        return new SortedListIterator(list);
    }
}
