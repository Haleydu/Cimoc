package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.IKanmanManga;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.ui.activity.DetailActivity;
import com.hiroshi.cimoc.utils.EventMessage;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by Hiroshi on 2016/7/4.
 */
public class DetailPresenter extends BasePresenter {

    private DetailActivity mDetailActivity;
    private IKanmanManga mIKanmanManga;

    public DetailPresenter(DetailActivity activity) {
        mDetailActivity = activity;
    }

    public void loadComic(String path, int source) {
        mIKanmanManga = new IKanmanManga();
        mIKanmanManga.parse(path, source);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessage(EventMessage msg) {
        switch (msg.getType()) {
            case EventMessage.LOAD_COMIC_SUCCESS:
                Comic comic = (Comic) msg.getData();
                mDetailActivity.setChapterList(comic.getList(), comic.getImage(), comic.getTitle(), comic.getIntro());
                break;
        }
    }

}
