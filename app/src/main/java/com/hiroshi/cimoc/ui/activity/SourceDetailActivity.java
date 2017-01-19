package com.hiroshi.cimoc.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.global.Extra;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.SourceDetailPresenter;
import com.hiroshi.cimoc.ui.custom.Option;
import com.hiroshi.cimoc.ui.fragment.dialog.EditorDialogFragment;
import com.hiroshi.cimoc.ui.view.DialogView;
import com.hiroshi.cimoc.ui.view.SourceDetailView;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2017/1/18.
 */

public class SourceDetailActivity extends BackActivity implements SourceDetailView {

    private static final int DIALOG_REQUEST_EDITOR = 0;

    private SourceDetailPresenter mPresenter;

    @BindView(R.id.source_detail_type) Option mSourceType;
    @BindView(R.id.source_detail_title) Option mSourceTitle;
    @BindView(R.id.source_detail_favorite) Option mSourceFavorite;
    @BindView(R.id.source_detail_server) Option mSourceServer;

    @Override
    protected BasePresenter initPresenter() {
        mPresenter = new SourceDetailPresenter();
        mPresenter.attachView(this);
        return mPresenter;
    }

    @Override
    protected void initData() {
        mPresenter.load(getIntent().getIntExtra(Extra.EXTRA_SOURCE, -1));
    }

    @OnClick(R.id.source_detail_favorite) void onSourceFavoriteClick() {

    }

    @OnClick(R.id.source_detail_server) void onSourceServerClick() {
        EditorDialogFragment fragment = EditorDialogFragment.newInstance(R.string.source_detail_server,
                mPresenter.getServer(), DIALOG_REQUEST_EDITOR);
        fragment.show(getFragmentManager(), null);
    }

    @Override
    public void onDialogResult(int requestCode, Bundle bundle) {
        switch (requestCode) {
            case DIALOG_REQUEST_EDITOR:
                String value = bundle.getString(DialogView.EXTRA_DIALOG_RESULT_VALUE);
                mPresenter.updateServer(value);
                mSourceServer.setSummary(value);
                break;
        }
    }

    @Override
    public void onSourceLoadSuccess(int type, String title, long count, String server) {
        mSourceType.setSummary(String.valueOf(type));
        mSourceTitle.setSummary(title);
        mSourceFavorite.setSummary(String.valueOf(count));
        mSourceServer.setSummary(server);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_source_detail;
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.source_detail);
    }

    public static Intent createIntent(Context context, int type) {
        Intent intent = new Intent(context, SourceDetailActivity.class);
        intent.putExtra(Extra.EXTRA_SOURCE, type);
        return intent;
    }

}
