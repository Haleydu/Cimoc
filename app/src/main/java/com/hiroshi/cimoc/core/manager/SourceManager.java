package com.hiroshi.cimoc.core.manager;

import android.util.SparseArray;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.core.source.CCTuku;
import com.hiroshi.cimoc.core.source.Dmzj;
import com.hiroshi.cimoc.core.source.EHentai;
import com.hiroshi.cimoc.core.source.ExHentai;
import com.hiroshi.cimoc.core.source.HHAAZZ;
import com.hiroshi.cimoc.core.source.IKanman;
import com.hiroshi.cimoc.core.source.NHentai;
import com.hiroshi.cimoc.core.source.U17;
import com.hiroshi.cimoc.core.source.Wnacg;
import com.hiroshi.cimoc.core.source.base.Parser;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.model.SourceDao;
import com.hiroshi.cimoc.model.SourceDao.Properties;

import java.util.List;

import rx.Observable;

/**
 * Created by Hiroshi on 2016/8/11.
 */
public class SourceManager {

    public static final int SOURCE_IKANMAN = 0;
    public static final int SOURCE_DMZJ = 1;
    public static final int SOURCE_HHAAZZ = 2;
    public static final int SOURCE_CCTUKU = 3;
    public static final int SOURCE_U17 = 4;

    public static final int SOURCE_EHENTAI = 100;
    public static final int SOURCE_EXHENTAI = 101;
    public static final int SOURCE_NHENTAI = 102;
    public static final int SOURCE_WNACG = 103;

    private static SourceManager mSourceManager;

    private SourceDao mSourceDao;

    private SourceManager() {
        mSourceDao = CimocApplication.getDaoSession().getSourceDao();
    }

    public Observable<List<Source>> list() {
        return mSourceDao.queryBuilder()
                .orderAsc(Properties.Sid)
                .rx()
                .list();
    }

    public List<Source> listEnable() {
        return mSourceDao.queryBuilder()
                .where(Properties.Enable.eq(true))
                .orderAsc(Properties.Sid)
                .list();
    }

    public long insert(Source source) {
        return mSourceDao.insert(source);
    }

    public void delete(Source source) {
        mSourceDao.delete(source);
    }

    public void update(Source source) {
        mSourceDao.update(source);
    }

    public static String getTitle(int id) {
        switch (id) {
            case SOURCE_IKANMAN:
                return "看漫画";
            case SOURCE_DMZJ:
                return "动漫之家";
            case SOURCE_HHAAZZ:
                return "汗汗漫画";
            case SOURCE_CCTUKU:
                return "CC图库";
            case SOURCE_U17:
                return "有妖气";
            case SOURCE_EHENTAI:
                return "E-Hentai";
            case SOURCE_EXHENTAI:
                return "ExHentai";
            case SOURCE_NHENTAI:
                return "NHentai";
            case SOURCE_WNACG:
                return "绅士漫画";
        }
        return "null";
    }

    public static int getId(String key) {
        switch (key) {
            case "IKanman":
                return SOURCE_IKANMAN;
            case "DMZJ":
                return SOURCE_DMZJ;
            case "HHAAZZ":
                return SOURCE_HHAAZZ;
            case "CCTuku":
                return SOURCE_CCTUKU;
            case "U17":
                return SOURCE_U17;
            case "EHentai":
                return SOURCE_EHENTAI;
            case "ExHentai":
                return SOURCE_EXHENTAI;
            case "NHentai":
                return SOURCE_NHENTAI;
            case "Wnacg":
                return SOURCE_WNACG;
        }
        return -1;
    }

    private static SparseArray<Parser> sparseArray = new SparseArray<>();

    public static Parser getParser(int source) {
        Parser parser = sparseArray.get(source);
        switch (source) {
            case SOURCE_IKANMAN:
                if (parser == null) {
                    parser = new IKanman();
                    sparseArray.put(SOURCE_IKANMAN, parser);
                }
                break;
            case SOURCE_DMZJ:
                if (parser == null) {
                    parser = new Dmzj();
                    sparseArray.put(SOURCE_DMZJ, parser);
                }
                break;
            case SOURCE_HHAAZZ:
                if (parser == null) {
                    parser = new HHAAZZ();
                    sparseArray.put(SOURCE_HHAAZZ, parser);
                }
                break;
            case SOURCE_CCTUKU:
                if (parser == null) {
                    parser = new CCTuku();
                    sparseArray.put(SOURCE_CCTUKU, parser);
                }
                break;
            case SOURCE_U17:
                if (parser == null) {
                    parser = new U17();
                    sparseArray.put(SOURCE_U17, parser);
                }
                break;
            case SOURCE_EHENTAI:
                if (parser == null) {
                    parser = new EHentai();
                    sparseArray.put(SOURCE_EHENTAI, parser);
                }
                break;
            case SOURCE_EXHENTAI:
                if (parser == null) {
                    parser = new ExHentai();
                    sparseArray.put(SOURCE_EXHENTAI, parser);
                }
                break;
            case SOURCE_NHENTAI:
                if (parser == null) {
                    parser = new NHentai();
                    sparseArray.put(SOURCE_NHENTAI, parser);
                }
                break;
            case SOURCE_WNACG:
                if (parser == null) {
                    parser = new Wnacg();
                    sparseArray.put(SOURCE_WNACG, parser);
                }
        }
        return parser;
    }

    public static SourceManager getInstance() {
        if (mSourceManager == null) {
            mSourceManager = new SourceManager();
        }
        return mSourceManager;
    }

}
