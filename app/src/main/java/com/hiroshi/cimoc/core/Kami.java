package com.hiroshi.cimoc.core;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.base.Manga;

/**
 * Created by Hiroshi on 2016/7/3.
 */
public class Kami {

    public static final int SOURCE_IKANMAN = 0;
    public static final int SOURCE_DMZJ = 1;
    public static final int SOURCE_HHAAZZ = 2;
    public static final int SOURCE_CCTUKU = 3;
    public static final int SOURCE_U17 = 4;
    public static final int SOURCE_EHENTAI = 100;
    public static final int SOURCE_EXHENTAI = 101;

    public static int getSourceTitle(int id) {
        switch (id) {
            default:
            case SOURCE_IKANMAN:
                return R.string.source_ikanman;
            case SOURCE_DMZJ:
                return R.string.source_dmzj;
            case SOURCE_HHAAZZ:
                return R.string.source_hhaazz;
            case SOURCE_CCTUKU:
                return R.string.source_cctuku;
            case SOURCE_U17:
                return R.string.source_u17;
            case SOURCE_EHENTAI:
                return R.string.source_ehentai;
            case SOURCE_EXHENTAI:
                return R.string.source_exhentai;
        }
    }

    public static String getReferer(int id) {
        switch (id) {
            default:
            case SOURCE_IKANMAN:
                return "http://m.ikanman.com";
            case SOURCE_DMZJ:
                return "http://m.dmzj.com/";
            case SOURCE_HHAAZZ:
                return "http://hhaazz.com";
            case SOURCE_CCTUKU:
                return "http://m.tuku.cc";
            case SOURCE_U17:
                return "http://www.u17.com";
            case SOURCE_EHENTAI:
                return "http://lofi.e-hentai.org";
            case SOURCE_EXHENTAI:
                return "https://exhentai.org";
        }
    }

    private static Manga mIkanman, mDmzj, mHHAAZZ, mCCTuku, mU17, mExHentai, mEHentai;

    public static Manga getManga(int id) {
        switch (id) {
            default:
            case SOURCE_IKANMAN:
                if (mIkanman == null) {
                    mIkanman = new IKanman();
                }
                return mIkanman;
            case SOURCE_DMZJ:
                if (mDmzj == null) {
                    mDmzj = new Dmzj();
                }
                return mDmzj;
            case SOURCE_HHAAZZ:
                if (mHHAAZZ == null) {
                    mHHAAZZ = new HHAAZZ();
                }
                return mHHAAZZ;
            case SOURCE_CCTUKU:
                if (mCCTuku == null) {
                    mCCTuku = new CCTuku();
                }
                return mCCTuku;
            case SOURCE_U17:
                if (mU17 == null) {
                    mU17 = new U17();
                }
                return mU17;
            case SOURCE_EHENTAI:
                if (mEHentai == null) {
                    mEHentai = new EHentai();
                }
                return mEHentai;
            case SOURCE_EXHENTAI:
                if (mExHentai == null) {
                    mExHentai = new ExHentai();
                }
                return mExHentai;
        }
    }

}
