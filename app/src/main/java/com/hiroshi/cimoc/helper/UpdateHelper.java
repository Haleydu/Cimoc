package com.hiroshi.cimoc.helper;

import com.hiroshi.cimoc.manager.PreferenceManager;
import com.hiroshi.cimoc.model.DaoSession;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.model.SourceDao;
import com.hiroshi.cimoc.source.CCTuku;
import com.hiroshi.cimoc.source.Chuiyao;
import com.hiroshi.cimoc.source.DM5;
import com.hiroshi.cimoc.source.Dmzj;
import com.hiroshi.cimoc.source.Dmzjv2;
import com.hiroshi.cimoc.source.HHAAZZ;
import com.hiroshi.cimoc.source.HHSSEE;
import com.hiroshi.cimoc.source.IKanman;
import com.hiroshi.cimoc.source.MH57;
import com.hiroshi.cimoc.source.U17;
import com.hiroshi.cimoc.source.Webtoon;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hiroshi on 2017/1/18.
 */

public class UpdateHelper {

    // 1.04.06.000
    private static final int VERSION = 10407000;

    public static void update(PreferenceManager manager, DaoSession session) {
        int version = manager.getInt(PreferenceManager.PREF_APP_VERSION, 0);
        if (version != VERSION) {
            switch (version) {
                case 0:
                    initSource(session);
                    break;
                case 10404000:
                case 10404001:
                case 10404002:
                case 10404003:
                    updateSourceServer(session);
                case 10405000:
                    session.getSourceDao().insert(Dmzjv2.getDefaultSource());
            }
            manager.putInt(PreferenceManager.PREF_APP_VERSION, VERSION);
        }
    }

    /**
     * app: 1.4.4.2 -> 1.4.4.3
     * db: 7 -> 8
     * 表 SOURCE 添加 SERVER 字段
     */
    private static void updateSourceServer(final DaoSession session) {
        session.runInTx(new Runnable() {
            @Override
            public void run() {
                SourceDao dao = session.getSourceDao();
                Source source = dao.queryBuilder().where(SourceDao.Properties.Type.eq(IKanman.TYPE)).unique();
                source.setServer(IKanman.DEFAULT_SERVER);
                dao.update(source);
                source = dao.queryBuilder().where(SourceDao.Properties.Type.eq(HHAAZZ.TYPE)).unique();
                source.setServer(HHAAZZ.DEFAULT_SERVER);
                dao.update(source);
                source = dao.queryBuilder().where(SourceDao.Properties.Type.eq(MH57.TYPE)).unique();
                source.setServer(MH57.DEFAULT_SERVER);
                dao.update(source);
            }
        });
    }

    /**
     * 初始化图源
     */
    private static void initSource(DaoSession session) {
        List<Source> list = new ArrayList<>(10);
        list.add(IKanman.getDefaultSource());
        list.add(Dmzj.getDefaultSource());
        list.add(HHAAZZ.getDefaultSource());
        list.add(CCTuku.getDefaultSource());
        list.add(U17.getDefaultSource());
        list.add(DM5.getDefaultSource());
        list.add(Webtoon.getDefaultSource());
        list.add(HHSSEE.getDefaultSource());
        list.add(MH57.getDefaultSource());
        list.add(Chuiyao.getDefaultSource());
        list.add(Dmzjv2.getDefaultSource());
        session.getSourceDao().insertOrReplaceInTx(list);
    }

}
