package com.haleydu.cimoc.ui.fragment.recyclerview;

import android.content.Intent;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.haleydu.cimoc.R;
import com.haleydu.cimoc.manager.SourceManager;
import com.haleydu.cimoc.model.Source;
import com.haleydu.cimoc.presenter.BasePresenter;
import com.haleydu.cimoc.presenter.SourcePresenter;
import com.haleydu.cimoc.ui.activity.CategoryActivity;
import com.haleydu.cimoc.ui.activity.SearchActivity;
import com.haleydu.cimoc.ui.activity.SourceDetailActivity;
import com.haleydu.cimoc.ui.adapter.BaseAdapter;
import com.haleydu.cimoc.ui.adapter.SourceAdapter;
import com.haleydu.cimoc.ui.view.SourceView;
import com.haleydu.cimoc.utils.HintUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hiroshi on 2016/8/11.
 */
public class SourceFragment extends RecyclerViewFragment implements SourceView, SourceAdapter.OnItemCheckedListener {

    private SourcePresenter mPresenter;
    private SourceAdapter mSourceAdapter;

    @Override
    protected BasePresenter initPresenter() {
        mPresenter = new SourcePresenter();
        mPresenter.attachView(this);
        return mPresenter;
    }

    @Override
    protected void initView() {
        setHasOptionsMenu(true);
        super.initView();
    }

    @Override
    protected BaseAdapter initAdapter() {
        mSourceAdapter = new SourceAdapter(getActivity(), new ArrayList<Source>());
        mSourceAdapter.setOnItemCheckedListener(this);
        return mSourceAdapter;
    }

    @Override
    protected RecyclerView.LayoutManager initLayoutManager() {
        return new GridLayoutManager(getActivity(), 2);
    }

    @Override
    protected void initData() {
        mPresenter.load();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_source, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.comic_search:
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
                break;
            case R.id.comic_inverseSelection:
                for (int i = 0; i < mSourceAdapter.getItemCount(); i++) {
                    Source source = mSourceAdapter.getItem(i);
                    source.setEnable(!source.getEnable());
                    mPresenter.update(source);
                }
                mSourceAdapter.notifyDataSetChanged();
                break;
            case R.id.comic_allSelection:
                for (int i = 0; i < mSourceAdapter.getItemCount(); i++) {
                    Source source = mSourceAdapter.getItem(i);
                    source.setEnable(true);
                    mPresenter.update(source);
                }
                mSourceAdapter.notifyDataSetChanged();
                break;
            case R.id.comic_AllDeselect:
                for (int i = 0; i < mSourceAdapter.getItemCount(); i++) {
                    Source source = mSourceAdapter.getItem(i);
                    source.setEnable(false);
                    mPresenter.update(source);
                }
                mSourceAdapter.notifyDataSetChanged();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(View view, int position) {
        Source source = mSourceAdapter.getItem(position);
        if (SourceManager.getInstance(this).getParser(source.getType()).getCategory() == null) {
            HintUtils.showToast(getActivity(), R.string.common_execute_fail);
        } else {
            Intent intent = CategoryActivity.createIntent(getActivity(), source.getType(), source.getTitle());
            startActivity(intent);
        }
    }

    @Override
    public boolean onItemLongClick(View view, int position) {
        Intent intent = SourceDetailActivity.createIntent(getActivity(), mSourceAdapter.getItem(position).getType());
        startActivity(intent);
        return true;
    }

    @Override
    public void onItemCheckedListener(boolean isChecked, int position) {
        Source source = mSourceAdapter.getItem(position);
        source.setEnable(isChecked);
        mPresenter.update(source);
    }

    @Override
    public void onSourceLoadSuccess(List<Source> list) {
        hideProgressBar();
        mSourceAdapter.addAll(list);
    }

    @Override
    public void onSourceLoadFail() {
        hideProgressBar();
        HintUtils.showToast(getActivity(), R.string.common_data_load_fail);
    }

    @Override
    public void onThemeChange(@ColorRes int primary, @ColorRes int accent) {
        mSourceAdapter.setColor(ContextCompat.getColor(getActivity(), accent));
        mSourceAdapter.notifyDataSetChanged();
    }

}
