package com.hiroshi.cimoc.helper;

import com.hiroshi.cimoc.BuildConfig;
import com.hiroshi.cimoc.manager.PreferenceManager;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ComicDao;
import com.hiroshi.cimoc.model.DaoSession;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.source.*;
import com.hiroshi.cimoc.source.WebtoonDongManManHua;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hiroshi on 2017/1/18.
 */

public class UpdateHelper {

    // 1.04.08.008
    private static final int VERSION = BuildConfig.VERSION_CODE;

    public static void update(PreferenceManager manager, final DaoSession session) {
        int version = manager.getInt(PreferenceManager.PREF_APP_VERSION, 0);
        if (version != VERSION) {
            initSource(session);
            manager.putInt(PreferenceManager.PREF_APP_VERSION, VERSION);
        }
    }

    /**
     * app: 1.4.8.0 -> 1.4.8.1
     * 删除本地漫画中 download 字段的值
     */
    private static void deleteDownloadFromLocal(final DaoSession session) {
        session.runInTx(new Runnable() {
            @Override
            public void run() {
                ComicDao dao = session.getComicDao();
                List<Comic> list = dao.queryBuilder().where(ComicDao.Properties.Local.eq(true)).list();
                if (!list.isEmpty()) {
                    for (Comic comic : list) {
                        comic.setDownload(null);
                    }
                    dao.updateInTx(list);
                }
            }
        });
    }

    /**
     * 初始化图源
     */
    private static void initSource(DaoSession session) {
        List<Source> list = new ArrayList<>();
        list.add(IKanman.getDefaultSource());
        list.add(Dmzj.getDefaultSource());
        list.add(HHAAZZ.getDefaultSource());
        list.add(CCTuku.getDefaultSource());
        list.add(U17.getDefaultSource());
        list.add(DM5.getDefaultSource());
        list.add(Webtoon.getDefaultSource());
        //list.add(HHSSEE.getDefaultSource());
        list.add(MH57.getDefaultSource());
        list.add(MH50.getDefaultSource());
        list.add(Dmzjv2.getDefaultSource());
        list.add(MangaNel.getDefaultSource());
        list.add(Mangakakalot.getDefaultSource());
        list.add(PuFei.getDefaultSource());
        list.add(Cartoonmad.getDefaultSource());
        list.add(Animx2.getDefaultSource());
        list.add(MH517.getDefaultSource());
        list.add(BaiNian.getDefaultSource());
        list.add(MiGu.getDefaultSource());
        list.add(Tencent.getDefaultSource());
        list.add(BuKa.getDefaultSource());
        list.add(EHentai.getDefaultSource());
        list.add(QiManWu.getDefaultSource());
        list.add(Hhxxee.getDefaultSource());
        list.add(ChuiXue.getDefaultSource());
        list.add(BaiNian.getDefaultSource());
        list.add(TuHao.getDefaultSource());
        list.add(MangaBZ.getDefaultSource());
        list.add(ManHuaDB.getDefaultSource());
        list.add(Manhuatai.getDefaultSource());
        list.add(GuFeng.getDefaultSource());
        list.add(CCMH.getDefaultSource());
        list.add(Manhuatai.getDefaultSource());
        list.add(MHLove.getDefaultSource());
        list.add(GuFeng.getDefaultSource());
        list.add(YYLS.getDefaultSource());
        list.add(JMTT.getDefaultSource());
        list.add(Ohmanhua.getDefaultSource());
        list.add(CopyMH.getDefaultSource());
        list.add(WebtoonDongManManHua.getDefaultSource());
        list.add(MH160.getDefaultSource());
        list.add(QiMiaoMH.getDefaultSource());
        session.getSourceDao().insertOrReplaceInTx(list);
    }
}
