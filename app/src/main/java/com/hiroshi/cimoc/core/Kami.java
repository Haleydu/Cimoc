package com.hiroshi.cimoc.core;

import com.hiroshi.cimoc.core.base.Manga;

/**
 * Created by Hiroshi on 2016/7/3.
 */
public class Kami {

    public static final int SOURCE_IKANMAN = 0;
    public static final int SOURCE_DMZJ = 1;
    public static final int SOURCE_CHUIYAO = 2;

    public static String getSourceById(int id) {
        switch (id) {
            default:
            case SOURCE_IKANMAN:
                return "看漫画";
            case SOURCE_DMZJ:
                return "动漫之家";
            case SOURCE_CHUIYAO:
                return "吹妖动漫";
        }
    }

    public static String getRefererById(int id) {
        switch (id) {
            default:
            case SOURCE_IKANMAN:
                return "http://m.ikanman.com";
            case SOURCE_DMZJ:
                return "http://m.dmzj.com/";
            case SOURCE_CHUIYAO:
                return "http://m.chuiyao.com";
        }
    }

    private static Manga[] mangaArray = new Manga[3];

    public static Manga getMangaById(int id) {
        if (id < 0 || id > 2) {
            return getMangaById(0);
        }
        if (mangaArray[id] == null) {
            switch (id) {
                case SOURCE_IKANMAN:
                    mangaArray[SOURCE_IKANMAN] = new IKanman();
                    break;
                case SOURCE_DMZJ:
                    mangaArray[SOURCE_DMZJ] = new Dmzj();
                    break;
                case SOURCE_CHUIYAO:
                    mangaArray[SOURCE_CHUIYAO] = new Chuiyao();
                    break;
            }
        }
        return mangaArray[id];
    }

}
