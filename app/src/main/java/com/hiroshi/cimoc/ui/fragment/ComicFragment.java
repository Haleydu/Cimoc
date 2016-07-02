package com.hiroshi.cimoc.ui.fragment;

import android.graphics.Rect;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.ui.adapter.ComicAdapter;

import java.util.LinkedList;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class ComicFragment extends BaseFragment {

    @BindView(R.id.list_comic) RecyclerView mComicList;

    private ComicAdapter mComicAdapter;

    @Override
    protected void initView() {
        LinkedList<Comic> list = new LinkedList<>();
        list.add(new Comic(R.drawable.test1, "斗破苍穹", "看漫画"));
        list.add(new Comic(R.drawable.test2, "七大罪", "吹雪漫画"));
        list.add(new Comic(R.drawable.test3, "妃·夕妍雪", "汗汗漫画"));
        list.add(new Comic(R.drawable.test4, "监狱学园", "极速漫画"));
        list.add(new Comic(R.drawable.test5, "斗罗大陆", "看漫画"));
        mComicAdapter = new ComicAdapter(getActivity(), list);
        mComicList.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mComicList.setItemAnimator(new DefaultItemAnimator());
        mComicList.setAdapter(mComicAdapter);
        mComicList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.set(10, 0, 10, 30);
            }
        });
    }

    @Override
    protected int getLayoutView() {
        return R.layout.fragment_comic;
    }
}
