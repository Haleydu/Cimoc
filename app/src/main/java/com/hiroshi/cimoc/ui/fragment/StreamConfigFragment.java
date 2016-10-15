package com.hiroshi.cimoc.ui.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.CheckBox;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.PreferenceManager;
import com.hiroshi.cimoc.ui.activity.settings.EventSettingsActivity;
import com.hiroshi.cimoc.utils.DialogUtils;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/10/13.
 */

public class StreamConfigFragment extends BaseFragment {

    @BindView(R.id.settings_reader_split_checkbox) CheckBox mSplitBox;
    @BindView(R.id.settings_reader_interval_checkbox) CheckBox mBlankBox;

    private int mReaderOrientationChoice;
    private int mReaderTurnChoice;
    private int mTempChoice;

    private DialogInterface.OnClickListener mSingleChoiceListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            mTempChoice = which;
        }
    };

    @Override
    protected void initView() {
        mReaderOrientationChoice = mPreference.getInt(PreferenceManager.PREF_READER_STREAM_ORIENTATION, PreferenceManager.READER_ORIENTATION_PORTRAIT);
        mReaderTurnChoice = mPreference.getInt(PreferenceManager.PREF_READER_STREAM_TURN, PreferenceManager.READER_TURN_LTR);
        mSplitBox.setChecked(mPreference.getBoolean(PreferenceManager.PREF_READER_STREAM_SPLIT, false));
        mBlankBox.setChecked(mPreference.getBoolean(PreferenceManager.PREF_READER_STREAM_INTERVAL, false));
    }

    @OnClick({ R.id.settings_reader_click_event_btn, R.id.settings_reader_long_click_event_btn })
    void onReaderEventBtnClick(View view) {
        boolean isLong = view.getId() == R.id.settings_reader_long_click_event_btn;
        Intent intent = EventSettingsActivity.createIntent(getActivity(), isLong,
                mReaderOrientationChoice == PreferenceManager.READER_ORIENTATION_PORTRAIT, true);
        startActivity(intent);
    }

    @OnClick(R.id.settings_reader_orientation_btn) void onReaderOrientationBtnClick() {
        DialogUtils.buildSingleChoiceDialog(getActivity(), R.string.settings_select_reader_orientation, R.array.reader_orientation_items,
                mReaderOrientationChoice, mSingleChoiceListener,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mReaderOrientationChoice = mTempChoice;
                        mPreference.putInt(PreferenceManager.PREF_READER_STREAM_ORIENTATION, mReaderOrientationChoice);
                    }
                }).show();
    }

    @OnClick(R.id.settings_reader_turn_btn) void onReaderTurnBtnClick() {
        DialogUtils.buildSingleChoiceDialog(getActivity(), R.string.settings_select_reader_turn, R.array.reader_turn_items,
                mReaderTurnChoice, mSingleChoiceListener,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mReaderTurnChoice = mTempChoice;
                        mPreference.putInt(PreferenceManager.PREF_READER_STREAM_TURN, mReaderTurnChoice);
                    }
                }).show();
    }

    @OnClick({ R.id.settings_reader_split_btn, R.id.settings_reader_interval_btn})
    void onCheckBoxClick(View view) {
        switch (view.getId()) {
            case R.id.settings_reader_split_btn:
                checkedAndSave(mSplitBox, PreferenceManager.PREF_READER_STREAM_SPLIT);
                break;
            case R.id.settings_reader_interval_btn:
                checkedAndSave(mBlankBox, PreferenceManager.PREF_READER_STREAM_INTERVAL);
                break;
        }
    }

    private void checkedAndSave(CheckBox box, String key) {
        boolean checked = !box.isChecked();
        box.setChecked(checked);
        mPreference.putBoolean(key, checked);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_stream_config;
    }

}
