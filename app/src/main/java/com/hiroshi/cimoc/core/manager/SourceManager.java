package com.hiroshi.cimoc.core.manager;

import android.util.SparseArray;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.source.CCTuku;
import com.hiroshi.cimoc.core.source.Dmzj;
import com.hiroshi.cimoc.core.source.EHentai;
import com.hiroshi.cimoc.core.source.ExHentai;
import com.hiroshi.cimoc.core.source.HHAAZZ;
import com.hiroshi.cimoc.core.source.IKanman;
import com.hiroshi.cimoc.core.source.NHentai;
import com.hiroshi.cimoc.core.source.U17;
import com.hiroshi.cimoc.core.source.Wnacg;
import com.hiroshi.cimoc.core.source.base.Manga;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.model.SourceDao;
import com.hiroshi.cimoc.model.SourceDao.Properties;

import java.util.List;

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

    public List<Source> list() {
        return mSourceDao.queryBuilder().orderAsc(Properties.Sid).list();
    }

    public List<Source> listEnable() {
        return mSourceDao.queryBuilder().where(Properties.Enable.eq(true)).orderAsc(Properties.Sid).list();
    }

    public boolean exist(int sid) {
        return mSourceDao.queryBuilder().where(Properties.Sid.eq(sid)).unique() != null;
    }

    public long insert(int sid) {
        return mSourceDao.insert(new Source(null, sid, true));
    }

    public void delete(long id) {
        mSourceDao.deleteByKey(id);
    }

    public void update(Source source) {
        mSourceDao.update(source);
    }

    public static int getTitle(int id) {
        switch (id) {
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
            case SOURCE_NHENTAI:
                return R.string.source_nhentai;
            case SOURCE_WNACG:
                return R.string.source_wnacg;
        }
        return R.string.common_null;
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

    private static SparseArray<Manga> sparseArray = new SparseArray<>();

    public static Manga getManga(int source) {
        Manga manga = sparseArray.get(source);
        switch (source) {
            case SOURCE_IKANMAN:
                if (manga == null) {
                    manga = new IKanman();
                    sparseArray.put(SOURCE_IKANMAN, manga);
                }
                break;
            case SOURCE_DMZJ:
                if (manga == null) {
                    manga = new Dmzj();
                    sparseArray.put(SOURCE_DMZJ, manga);
                }
                break;
            case SOURCE_HHAAZZ:
                if (manga == null) {
                    manga = new HHAAZZ();
                    sparseArray.put(SOURCE_HHAAZZ, manga);
                }
                break;
            case SOURCE_CCTUKU:
                if (manga == null) {
                    manga = new CCTuku();
                    sparseArray.put(SOURCE_CCTUKU, manga);
                }
                break;
            case SOURCE_U17:
                if (manga == null) {
                    manga = new U17();
                    sparseArray.put(SOURCE_U17, manga);
                }
                break;
            case SOURCE_EHENTAI:
                if (manga == null) {
                    manga = new EHentai();
                    sparseArray.put(SOURCE_EHENTAI, manga);
                }
                break;
            case SOURCE_EXHENTAI:
                if (manga == null) {
                    manga = new ExHentai();
                    sparseArray.put(SOURCE_EXHENTAI, manga);
                }
                break;
            case SOURCE_NHENTAI:
                if (manga == null) {
                    manga = new NHentai();
                    sparseArray.put(SOURCE_NHENTAI, manga);
                }
                break;
            case SOURCE_WNACG:
                if (manga == null) {
                    manga = new Wnacg();
                    sparseArray.put(SOURCE_WNACG, manga);
                }
        }
        return manga;
    }

    public static SourceManager getInstance() {
        if (mSourceManager == null) {
            mSourceManager = new SourceManager();
        }
        return mSourceManager;
    }

}
