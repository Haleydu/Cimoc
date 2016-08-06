package com.hiroshi.cimoc.ui.activity;

import android.content.Intent;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/8/6.
 */
public abstract class ReaderActivity extends BaseActivity {

    @BindView(R.id.reader_chapter_title) TextView mChapterTitle;
    @BindView(R.id.reader_chapter_page) TextView mChapterPage;
    @BindView(R.id.reader_tool_bar) LinearLayout mToolLayout;
    @BindView(R.id.reader_seek_bar) DiscreteSeekBar mSeekBar;

    protected int progress;
    protected int max;

    @Override
    protected void initToolbar() {}

    protected static final String EXTRA_SOURCE = "a";
    protected static final String EXTRA_CID = "b";
    protected static final String EXTRA_LAST = "c";
    protected static final String EXTRA_PAGE = "d";
    protected static final String EXTRA_TITLE = "e";
    protected static final String EXTRA_PATH = "f";
    protected static final String EXTRA_POSITION = "g";

    protected static void putExtras(Intent intent, Comic comic, List<Chapter> list, int position) {
        intent.putExtra(EXTRA_SOURCE, comic.getSource());
        intent.putExtra(EXTRA_CID, comic.getCid());
        intent.putExtra(EXTRA_LAST, comic.getLast());
        intent.putExtra(EXTRA_PAGE, comic.getPage());
        String[][] array = fromList(list);
        intent.putExtra(EXTRA_TITLE, array[0]);
        intent.putExtra(EXTRA_PATH, array[1]);
        intent.putExtra(EXTRA_POSITION, position);
    }

    protected static String[][] fromList(List<Chapter> list) {
        int size = list.size();
        String[] title = new String[size];
        String[] path = new String[size];
        for (int i = 0; i != size; ++i) {
            title[i] = list.get(i).getTitle();
            path[i] = list.get(i).getPath();
        }
        return new String[][] { title, path };
    }

    protected static Chapter[] fromArray(String[] title, String[] path) {
        int size = title.length;
        Chapter[] array = new Chapter[size];
        for (int i = 0; i != size; ++i) {
            array[i] = new Chapter(title[i], path[i]);
        }
        return array;
    }

}
