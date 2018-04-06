package com.hiroshi.cimoc.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.manager.SourceManager;
import com.hiroshi.cimoc.source.BuKa;
import com.hiroshi.cimoc.source.DM5;
import com.hiroshi.cimoc.source.Dmzjv2;
import com.hiroshi.cimoc.source.IKanman;
import com.hiroshi.cimoc.source.MH57;
import com.hiroshi.cimoc.source.PuFei;
import com.hiroshi.cimoc.source.Tencent;
import com.hiroshi.cimoc.source.U17;

import java.util.ArrayList;
import java.util.List;

public class BrowserFilter extends BaseActivity {

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_browser_filter;
    }

    @Override
    protected String getDefaultTitle() {
        return "jumping...";
    }

//    private Parser parser;
//    private SourceManager mSourceManager;

    public void openDetailActivity(int source, String comicId) {
        Intent intent = DetailActivity.createIntent(this, null, source, comicId);
        startActivity(intent);
    }

//    public void openReaderActivity(int source,String comicId) {
//        Intent intent = DetailActivity.createIntent(this, null, source, comicId);
//        startActivity(intent);
//    }

    private List<Integer> registUrlListener() {
        List<Integer> list = new ArrayList<>();

        list.add(Dmzjv2.TYPE);
        list.add(BuKa.TYPE);
        list.add(PuFei.TYPE);
        list.add(Tencent.TYPE);
        list.add(U17.TYPE);
        list.add(MH57.TYPE);
        list.add(DM5.TYPE);
        list.add(IKanman.TYPE);

        return list;
    }

    private void openReader(Uri uri) {
        SourceManager mSourceManager = SourceManager.getInstance(this);
        String comicId;

        for (int i : registUrlListener()) {
            if (mSourceManager.getParser(i).isHere(uri)
                && ((comicId = mSourceManager.getParser(i).getComicId(uri)) != null)) {
                openDetailActivity(i, comicId);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser_filter);

        Intent i_getvalue = getIntent();
        String action = i_getvalue.getAction();

        if (Intent.ACTION_VIEW.equals(action)) {
            Uri uri = i_getvalue.getData();
            if (uri != null) {
                openReader(uri);
            }
        }
        finish();
    }
}
