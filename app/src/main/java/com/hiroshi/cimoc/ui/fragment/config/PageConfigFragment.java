package com.hiroshi.cimoc.ui.fragment.config;

import android.content.Intent;
import android.view.View;
import android.widget.CheckBox;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.PreferenceManager;
import com.hiroshi.cimoc.ui.activity.settings.EventSettingsActivity;
import com.hiroshi.cimoc.ui.fragment.BaseFragment;
import com.hiroshi.cimoc.ui.fragment.dialog.ChoiceDialogFragment;
import com.hiroshi.cimoc.ui.fragment.dialog.SliderDialogFragment;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/10/13.
 */

public class PageConfigFragment extends BaseFragment implements SliderDialogFragment.SliderDialogListener,
        ChoiceDialogFragment.ChoiceDialogListener {

    private static final int TYPE_ORIENTATION = 0;
    private static final int TYPE_TURN = 1;

    @BindView(R.id.settings_reader_load_prev_checkbox) CheckBox mReaderLoadPrevBox;
    @BindView(R.id.settings_reader_load_next_checkbox) CheckBox mReaderLoadNextBox;

    private int mReaderOrientationChoice;
    private int mReaderTurnChoice;
    private int mReaderTriggerValue;

    @Override
    protected void initView() {
        mReaderOrientationChoice = mPreference.getInt(PreferenceManager.PREF_READER_PAGE_ORIENTATION, PreferenceManager.READER_ORIENTATION_PORTRAIT);
        mReaderTurnChoice = mPreference.getInt(PreferenceManager.PREF_READER_PAGE_TURN, PreferenceManager.READER_TURN_LTR);
        mReaderLoadPrevBox.setChecked(mPreference.getBoolean(PreferenceManager.PREF_READER_PAGE_LOAD_PREV, true));
        mReaderLoadNextBox.setChecked(mPreference.getBoolean(PreferenceManager.PREF_READER_PAGE_LOAD_NEXT, true));
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
                getResources().getStringArray(R.array.reader_orientation_items), mReaderOrientationChoice, TYPE_ORIENTATION);
        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), null);
    }

    @OnClick(R.id.settings_reader_turn_btn) void onReaderTurnClick() {
        ChoiceDialogFragment fragment = ChoiceDialogFragment.newInstance(R.string.settings_reader_turn,
                getResources().getStringArray(R.array.reader_turn_items), mReaderTurnChoice, TYPE_TURN);
        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), null);
    }

    @OnClick({R.id.settings_reader_load_prev_btn, R.id.settings_reader_load_next_btn})
    void onCheckBoxClick(View view) {
        switch (view.getId()) {
            case R.id.settings_reader_load_prev_btn:
                checkedAndSave(mReaderLoadPrevBox, PreferenceManager.PREF_READER_PAGE_LOAD_PREV);
                break;
            case R.id.settings_reader_load_next_btn:
                checkedAndSave(mReaderLoadNextBox, PreferenceManager.PREF_READER_PAGE_LOAD_NEXT);
                break;
        }
    }

    private void checkedAndSave(CheckBox box, String key) {
        boolean checked = !box.isChecked();
        box.setChecked(checked);
        mPreference.putBoolean(key, checked);
    }

    @Override
    public void onChoicePositiveClick(int type, int choice, String value) {
        switch (type) {
            case TYPE_ORIENTATION:
                mPreference.putInt(PreferenceManager.PREF_READER_PAGE_ORIENTATION, choice);
                mReaderOrientationChoice = choice;
                break;
            case TYPE_TURN:
                mPreference.putInt(PreferenceManager.PREF_READER_PAGE_TURN, choice);
                mReaderTurnChoice = choice;
                break;
        }
    }

    @OnClick(R.id.settings_reader_trigger_btn) void onTriggerBtnClick() {
        SliderDialogFragment fragment = SliderDialogFragment.newInstance(R.string.settings_reader_trigger, 5, 60, mReaderTriggerValue);
        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), null);
    }

    @Override
    public void onSliderPositiveClick(int value) {
        mPreference.putInt(PreferenceManager.PREF_READER_PAGE_TRIGGER, value);
        mReaderTriggerValue = value;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_page_config;
    }

}
