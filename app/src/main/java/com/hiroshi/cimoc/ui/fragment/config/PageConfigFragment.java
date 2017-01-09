package com.hiroshi.cimoc.ui.fragment.config;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.PreferenceManager;
import com.hiroshi.cimoc.ui.activity.settings.EventSettingsActivity;
import com.hiroshi.cimoc.ui.fragment.BaseFragment;
import com.hiroshi.cimoc.ui.fragment.dialog.ChoiceDialogFragment;
import com.hiroshi.cimoc.ui.fragment.dialog.SliderDialogFragment;
import com.hiroshi.cimoc.ui.view.DialogView;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/10/13.
 */

public class PageConfigFragment extends BaseFragment implements DialogView {

    private static final int DIALOG_REQUEST_ORIENTATION = 0;
    private static final int DIALOG_REQUEST_TURN = 1;
    private static final int DIALOG_REQUEST_OFFSET = 2;

    @BindView(R.id.settings_reader_load_prev_checkbox) AppCompatCheckBox mReaderLoadPrevBox;
    @BindView(R.id.settings_reader_load_next_checkbox) AppCompatCheckBox mReaderLoadNextBox;
    @BindView(R.id.settings_reader_white_edge_checkbox) AppCompatCheckBox mReaderWhiteEdgeBox;

    private int mReaderOrientationChoice;
    private int mReaderTurnChoice;
    private int mReaderTriggerValue;

    @Override
    protected void initView() {
        mReaderOrientationChoice = mPreference.getInt(PreferenceManager.PREF_READER_PAGE_ORIENTATION, PreferenceManager.READER_ORIENTATION_PORTRAIT);
        mReaderTurnChoice = mPreference.getInt(PreferenceManager.PREF_READER_PAGE_TURN, PreferenceManager.READER_TURN_LTR);
        mReaderLoadPrevBox.setChecked(mPreference.getBoolean(PreferenceManager.PREF_READER_PAGE_LOAD_PREV, true));
        mReaderLoadNextBox.setChecked(mPreference.getBoolean(PreferenceManager.PREF_READER_PAGE_LOAD_NEXT, true));
        mReaderWhiteEdgeBox.setChecked(mPreference.getBoolean(PreferenceManager.PREF_READER_PAGE_WHITE_EDGE, false));
        mReaderTriggerValue = mPreference.getInt(PreferenceManager.PREF_READER_PAGE_TRIGGER, 5);
    }

    @OnClick({ R.id.settings_reader_click_event_btn, R.id.settings_reader_long_click_event_btn })
    void onReaderEventClick(View view) {
        boolean isLong = view.getId() == R.id.settings_reader_long_click_event_btn;
        Intent intent = EventSettingsActivity.createIntent(getActivity(), isLong,
                mReaderOrientationChoice == PreferenceManager.READER_ORIENTATION_PORTRAIT, false);
        startActivity(intent);
    }

    @OnClick(R.id.settings_reader_orientation_btn) void onReaderOrientationClick() {
        ChoiceDialogFragment fragment = ChoiceDialogFragment.newInstance(R.string.settings_reader_orientation,
                getResources().getStringArray(R.array.reader_orientation_items), mReaderOrientationChoice, null, DIALOG_REQUEST_ORIENTATION);
        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), null);
    }

    @OnClick(R.id.settings_reader_turn_btn) void onReaderTurnClick() {
        ChoiceDialogFragment fragment = ChoiceDialogFragment.newInstance(R.string.settings_reader_turn,
                getResources().getStringArray(R.array.reader_turn_items), mReaderTurnChoice, null, DIALOG_REQUEST_TURN);
        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), null);
    }

    @OnClick({R.id.settings_reader_load_prev_btn, R.id.settings_reader_load_next_btn, R.id.settings_reader_white_edge_btn})
    void onCheckBoxClick(View view) {
        switch (view.getId()) {
            case R.id.settings_reader_load_prev_btn:
                checkedAndSave(mReaderLoadPrevBox, PreferenceManager.PREF_READER_PAGE_LOAD_PREV);
                break;
            case R.id.settings_reader_load_next_btn:
                checkedAndSave(mReaderLoadNextBox, PreferenceManager.PREF_READER_PAGE_LOAD_NEXT);
                break;
            case R.id.settings_reader_white_edge_btn:
                checkedAndSave(mReaderWhiteEdgeBox, PreferenceManager.PREF_READER_PAGE_WHITE_EDGE);
                break;
        }
    }

    private void checkedAndSave(AppCompatCheckBox box, String key) {
        boolean checked = !box.isChecked();
        box.setChecked(checked);
        mPreference.putBoolean(key, checked);
    }

    @Override
    public void onDialogResult(int requestCode, Bundle bundle) {
        int index;
        switch (requestCode) {
            case DIALOG_REQUEST_ORIENTATION:
                index = bundle.getInt(EXTRA_DIALOG_RESULT_INDEX);
                mPreference.putInt(PreferenceManager.PREF_READER_PAGE_ORIENTATION, index);
                mReaderOrientationChoice = index;
                break;
            case DIALOG_REQUEST_TURN:
                index = bundle.getInt(EXTRA_DIALOG_RESULT_INDEX);
                mPreference.putInt(PreferenceManager.PREF_READER_PAGE_TURN, index);
                mReaderTurnChoice = index;
                break;
            case DIALOG_REQUEST_OFFSET:
                int value = bundle.getInt(EXTRA_DIALOG_RESULT_VALUE);
                mPreference.putInt(PreferenceManager.PREF_READER_PAGE_TRIGGER, value);
                mReaderTriggerValue = value;
        }
    }

    @OnClick(R.id.settings_reader_trigger_btn) void onTriggerBtnClick() {
        SliderDialogFragment fragment = SliderDialogFragment.newInstance(R.string.settings_reader_trigger, 5, 60, mReaderTriggerValue, DIALOG_REQUEST_OFFSET);
        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), null);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_page_config;
    }

}
