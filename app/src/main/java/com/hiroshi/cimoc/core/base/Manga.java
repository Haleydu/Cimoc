package com.hiroshi.cimoc.core.base;

import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.utils.YuriClient;

import java.util.List;

/**
 * Created by Hiroshi on 2016/7/3.
 */
public abstract class Manga {

    protected YuriClient client;

    private int searchPage;
    private String searchKeyword;

    public Manga() {
        client = YuriClient.getInstance();
    }

    public void searchFirst(String keyword) {
        searchPage = 1;
        searchKeyword = keyword;
        search(keyword, 1);
    }

    public void searchNext() {
        search(searchKeyword, ++searchPage);
    }

    protected abstract void search(String keyword, int page);

}
