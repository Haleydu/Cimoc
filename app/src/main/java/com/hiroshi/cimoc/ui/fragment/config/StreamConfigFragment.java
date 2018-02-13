package com.hiroshi.cimoc.ui.fragment.config;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.component.DialogCaller;
import com.hiroshi.cimoc.manager.PreferenceManager;
import com.hiroshi.cimoc.ui.activity.settings.EventSettingsActivity;
import com.hiroshi.cimoc.ui.fragment.BaseFragment;
import com.hiroshi.cimoc.ui.widget.preference.CheckBoxPreference;
import com.hiroshi.cimoc.ui.widget.preference.ChoicePreference;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/10/13.
 */

public class StreamConfigFragment extends BaseFragment implements DialogCaller {

    private static final int DIALOG_REQUEST_ORIENTATION = 0;
    private static final int DIALOG_REQUEST_TURN = 1;

    @BindView(R.id.settings_reader_interval) CheckBoxPreference mReaderInterval;
    @BindView(R.id.settings_reader_load_prev) CheckBoxPreference mReaderLoadPrev;
    @BindView(R.id.settings_reader_load_next) CheckBoxPreference mReaderLoadNext;
    @BindView(R.id.settings_reader_orientation) ChoicePreference mReaderOrientation;
    @BindView(R.id.settings_reader_turn) ChoicePreference mReaderTurn;

    @Override
    protected void initView() {
        mReaderInterval.bindPreference(PreferenceManager.PREF_READER_STREAM_INTERVAL, false);
        mReaderLoadPrev.bindPreference(PreferenceManager.PREF_READER_STREAM_LOAD_PREV, false);
        mReaderLoadNext.bindPreference(PreferenceManager.PREF_READER_STREAM_LOAD_NEXT, true);
        mReaderOrientation.bindPreference(getFragmentManager(), this, PreferenceManager.PREF_READER_STREAM_ORIENTATION,
                PreferenceManager.READER_ORIENTATION_PORTRAIT, R.array.reader_orientation_items, DIALOG_REQUEST_ORIENTATION);
        mReaderTurn.bindPreference(getFragmentManager(), this, PreferenceManager.PREF_READER_STREAM_TURN,
                PreferenceManager.READER_TURN_LTR, R.array.reader_turn_items, DIALOG_REQUEST_TURN);
    }

    @OnClick({ R.id.settings_reader_click_event, R.id.settings_reader_long_click_event })
    void onReaderEventClick(View view) {
        boolean isLong = view.getId() == R.id.settings_reader_long_click_event;
        Intent intent = EventSettingsActivity.createIntent(getActivity(), isLong,
                mReaderOrientation.getValue() == PreferenceManager.READER_ORIENTATION_PORTRAIT, true);
        startActivity(intent);
    }

    @Override
    public void onDialogResult(int requestCode, Bundle bundle) {
        switch (requestCode) {
            case DIALOG_REQUEST_ORIENTATION:
                mReaderOrientation.setValue(bundle.getInt(EXTRA_DIALOG_RESULT_INDEX));
                break;
            case DIALOG_REQUEST_TURN:
                mReaderTurn.setValue(bundle.getInt(EXTRA_DIALOG_RESULT_INDEX));
                break;
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_stream_config;
    }

}
