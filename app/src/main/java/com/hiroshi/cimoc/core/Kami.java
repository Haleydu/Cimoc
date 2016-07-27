package com.hiroshi.cimoc.core;

import com.hiroshi.cimoc.core.base.Manga;

/**
 * Created by Hiroshi on 2016/7/3.
 */
public class Kami {

    public static final int SOURCE_IKANMAN = 0;
    public static final int SOURCE_DMZJ = 1;
    public static final int SOURCE_CHUIYAO = 2;
    public static final int SOURCE_HHAAZZ = 3;
    public static final int SOURCE_EHENTAI = 4;

    public static String getSourceById(int id) {
        switch (id) {
            default:
            case SOURCE_IKANMAN:
                return "看漫画";
            case SOURCE_DMZJ:
                return "动漫之家";
            case SOURCE_CHUIYAO:
                return "吹妖动漫";
            case SOURCE_HHAAZZ:
                return "汗汗漫画";
            case SOURCE_EHENTAI:
                return "E-Hentai";
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
            case SOURCE_HHAAZZ:
                return "http://hhaazz.com";
            case SOURCE_EHENTAI:
                return "http://lofi.e-hentai.org";
        }
    }

    public static Manga getMangaById(int id) {
        switch (id) {
            default:
            case SOURCE_IKANMAN:
                return new IKanman();
            case SOURCE_DMZJ:
                return new Dmzj();
            case SOURCE_CHUIYAO:
                return new Chuiyao();
            case SOURCE_HHAAZZ:
                return new HHAAZZ();
            case SOURCE_EHENTAI:
                return new EHentai();
        }
    }

}
