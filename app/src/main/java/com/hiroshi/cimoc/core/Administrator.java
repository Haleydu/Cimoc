package com.hiroshi.cimoc.core;

import com.hiroshi.cimoc.core.base.Manga;

/**
 * Created by Hiroshi on 2016/7/3.
 */
public class Administrator {

    public static final int SOURCE_IKANMAN = 1;

    public static String getSourceById(int id) {
        switch (id) {
            case SOURCE_IKANMAN:
                return "看漫画";
            default:
                return "";
        }
    }

    public static String getHostById(int id) {
        switch (id) {
            case SOURCE_IKANMAN:
                return "http://m.ikanman.com";
            default:
                return "";
        }
    }

    public static Manga getMangaById(int id) {
        switch (id) {
            case SOURCE_IKANMAN:
                return IKanman.getInstance();
            default:
                return null;
        }
    }

}
