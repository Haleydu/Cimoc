package com.hiroshi.cimoc.rx;

/**
 * Created by Hiroshi on 2016/10/19.
 */

public class RxObject {

    private Object[] data;

    public RxObject(Object... data) {
        this.data = data;
    }

    public Object getData() {
        return getData(0);
    }

    public Object getData(int index) {
        return index < data.length ? data[index] : null;
    }

}
