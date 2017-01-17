package com.hiroshi.cimoc.core;

import com.hiroshi.cimoc.core.manager.PreferenceManager;
import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.global.ImageServer;
import com.hiroshi.cimoc.model.DaoSession;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.model.SourceDao;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hiroshi on 2017/1/18.
 */

public class UpdateHelper {

    // 1.04.04.003
    private static final int VERSION = 10404003;

    public static void update(PreferenceManager manager, DaoSession session) {
        int version = manager.getInt(PreferenceManager.PREF_APP_VERSION, 0);
        if (version != VERSION) {
            switch (version) {
                case 0:
                    initSource(session);
                    break;
                case 10404002:
                    updateSourceServer(session);
            }
            manager.putInt(PreferenceManager.PREF_APP_VERSION, VERSION);
        }
    }

    private static void updateSourceServer(final DaoSession session) {
        session.runInTx(new Runnable() {
            @Override
            public void run() {
                SourceDao dao = session.getSourceDao();
                Source source = dao.queryBuilder().where(SourceDao.Properties.Type.eq(SourceManager.SOURCE_IKANMAN)).unique();
                source.setServer(ImageServer.DEFAULT_SERVER_IKANMAN);
                dao.update(source);
                source = dao.queryBuilder().where(SourceDao.Properties.Type.eq(SourceManager.SOURCE_HHAAZZ)).unique();
                source.setServer(ImageServer.DEFAULT_SERVER_HHAAZZ);
                dao.update(source);
                source = dao.queryBuilder().where(SourceDao.Properties.Type.eq(SourceManager.SOURCE_57MH)).unique();
                source.setServer(ImageServer.DEFAULT_SERVER_57MH);
                dao.update(source);
            }
        });
    }

    private static void initSource(DaoSession session) {
        int[] type = {SourceManager.SOURCE_IKANMAN, SourceManager.SOURCE_DMZJ, SourceManager.SOURCE_HHAAZZ, SourceManager.SOURCE_CCTUKU,
                SourceManager.SOURCE_U17, SourceManager.SOURCE_DM5, SourceManager.SOURCE_WEBTOON, SourceManager.SOURCE_HHSSEE,
                SourceManager.SOURCE_57MH, SourceManager.SOURCE_CHUIYAO};
        String[] server = {ImageServer.DEFAULT_SERVER_IKANMAN, null, ImageServer.DEFAULT_SERVER_HHAAZZ,
                null, null, null, null, null, ImageServer.DEFAULT_SERVER_57MH, null};
        List<Source> list = new ArrayList<>(type.length);
        for (int i = 0; i != type.length; ++i) {
            list.add(new Source(null, SourceManager.getTitle(type[i]), type[i], true, server[i]));
        }
        session.getSourceDao().insertOrReplaceInTx(list);
    }

}
