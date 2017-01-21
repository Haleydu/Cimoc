package com.hiroshi.cimoc.parser;

import com.hiroshi.cimoc.model.Comic;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Hiroshi on 2016/9/21.
 */

public abstract class JsonIterator implements SearchIterator {

    private int index;
    private JSONArray array;

    public JsonIterator(JSONArray array) {
        this.index = 0;
        this.array = array;
    }

    @Override
    public boolean hasNext() {
        return index < array.length();
    }

    @Override
    public Comic next() {
        try {
            return parse(array.getJSONObject(index++));
        } catch (JSONException e) {
            return null;
        }
    }

    @Override
    public boolean empty() {
        return array == null || array.length() == 0;
    }

    protected abstract Comic parse(JSONObject object);

}
