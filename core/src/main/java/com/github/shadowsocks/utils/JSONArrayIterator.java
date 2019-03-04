package com.github.shadowsocks.utils;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * @author 陈志鹏
 * @date 2019/2/27
 */
public class JSONArrayIterator extends ArrayIterator<Object> {

    private JSONArray jsonArray;

    public JSONArrayIterator(JSONArray jsonArray){
        super();
        this.jsonArray = jsonArray;
    }

    @Override
    public int getSize() {
        return jsonArray.length();
    }

    @Override
    public Object get(int index) {
        try {
            return jsonArray.get(index);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
