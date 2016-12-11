package com.hiroshi.cimoc.core.manager;

import android.util.SparseArray;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.core.parser.Parser;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.model.SourceDao;
import com.hiroshi.cimoc.model.SourceDao.Properties;
import com.hiroshi.cimoc.source.CCTuku;
import com.hiroshi.cimoc.source.Chuiyao;
import com.hiroshi.cimoc.source.DM5;
import com.hiroshi.cimoc.source.Dmzj;
import com.hiroshi.cimoc.source.HHAAZZ;
import com.hiroshi.cimoc.source.HHSSEE;
import com.hiroshi.cimoc.source.IKanman;
import com.hiroshi.cimoc.source.Jmydm;
import com.hiroshi.cimoc.source.MH57;
import com.hiroshi.cimoc.source.U17;
import com.hiroshi.cimoc.source.Webtoon;

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
    public static final int SOURCE_DM5 = 5;
    public static final int SOURCE_WEBTOON = 6;
    public static final int SOURCE_HHSSEE = 7;
    public static final int SOURCE_57MH = 8;
    public static final int SOURCE_CHUIYAO = 9;
    public static final int SOURCE_JMYDM = 10;

    public static final int SOURCE_EHENTAI = 100;
    public static final int SOURCE_EXHENTAI = 101;
    public static final int SOURCE_NHENTAI = 102;
    public static final int SOURCE_WNACG = 103;
    public static final int SOURCE_177PIC = 104;

    private static SourceManager mSourceManager;

    private SourceDao mSourceDao;

    private SourceManager() {
        mSourceDao = CimocApplication.getDaoSession().getSourceDao();
    }

    public Observable<List<Source>> list() {
        return mSourceDao.queryBuilder()
                .orderAsc(Properties.Type)
                .rx()
                .list();
    }

    public Observable<List<Source>> listEnableInRx() {
        return mSourceDao.queryBuilder()
                .where(Properties.Enable.eq(true))
                .orderAsc(Properties.Type)
                .rx()
                .list();
    }

    public List<Source> listEnable() {
        return mSourceDao.queryBuilder()
                .where(Properties.Enable.eq(true))
                .orderAsc(Properties.Type)
                .list();
    }

    public long insert(Source source) {
        return mSourceDao.insert(source);
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
                return "手机汗汗";
            case SOURCE_CCTUKU:
                return "CC图库";
            case SOURCE_U17:
                return "有妖气";
            case SOURCE_DM5:
                return "动漫屋";
            case SOURCE_WEBTOON:
                return "Webtoon";
            case SOURCE_HHSSEE:
                return "汗汗漫画";
            case SOURCE_57MH:
                return "57漫画";
            case SOURCE_CHUIYAO:
                return "吹妖漫画";
            case SOURCE_JMYDM:
                return "精明眼";
/*            case SOURCE_EHENTAI:
                return "E-Hentai";
            case SOURCE_EXHENTAI:
                return "ExHentai";
            case SOURCE_NHENTAI:
                return "NHentai";
            case SOURCE_WNACG:
                return "绅士漫画";
            case SOURCE_177PIC:
                return "177漫画"; */
        }
        return "null";
    }

    private static SparseArray<Parser> mParserArray = new SparseArray<>();

    public static Parser getParser(int source) {
        Parser parser = mParserArray.get(source);
        if (parser == null) {
            switch (source) {
                case SOURCE_IKANMAN:
                    parser = new IKanman();
                    break;
                case SOURCE_DMZJ:
                    parser = new Dmzj();
                    break;
                case SOURCE_HHAAZZ:
                    parser = new HHAAZZ();
                    break;
                case SOURCE_CCTUKU:
                    parser = new CCTuku();
                    break;
                case SOURCE_U17:
                    parser = new U17();
                    break;
                case SOURCE_DM5:
                    parser = new DM5();
                    break;
                case SOURCE_WEBTOON:
                    parser = new Webtoon();
                    break;
                case SOURCE_HHSSEE:
                    parser = new HHSSEE();
                    break;
                case SOURCE_57MH:
                    parser = new MH57();
                    break;
                case SOURCE_CHUIYAO:
                    parser = new Chuiyao();
                    break;
                case SOURCE_JMYDM:
                    parser = new Jmydm();
                    break;
/*                case SOURCE_EHENTAI:
                    parser = new EHentai();
                    break;
                case SOURCE_EXHENTAI:
                    parser = new ExHentai();
                    break;
                case SOURCE_NHENTAI:
                    parser = new NHentai();
                    break;
                case SOURCE_WNACG:
                    parser = new Wnacg();
                    break;
                case SOURCE_177PIC:
                    parser = new Pic177();
                    break;  */
            }
            mParserArray.put(source, parser);
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
