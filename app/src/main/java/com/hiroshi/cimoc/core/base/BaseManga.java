package com.hiroshi.cimoc.core.base;

import com.hiroshi.cimoc.utils.YuriClient;

/**
 * Created by Hiroshi on 2016/7/4.
 */
public abstract class BaseManga {

    protected YuriClient client;

    public BaseManga() {
        client = YuriClient.getInstance();
    }


}
