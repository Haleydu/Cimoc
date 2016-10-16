package com.hiroshi.cimoc.ui.fragment.config;

import android.content.Intent;
import android.view.View;
import android.widget.CheckBox;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.PreferenceManager;
import com.hiroshi.cimoc.ui.activity.settings.EventSettingsActivity;
import com.hiroshi.cimoc.ui.fragment.BaseFragment;
import com.hiroshi.cimoc.ui.fragment.dialog.ChoiceDialogFragment;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/10/13.
 */

public class StreamConfigFragment extends BaseFragment implements ChoiceDialogFragment.ChoiceDialogListener {

    private static final int TYPE_ORIENTATION = 0;
    private static final int TYPE_TURN = 1;

    @BindView(R.id.settings_reader_split_checkbox) CheckBox mSplitBox;
    @BindView(R.id.settings_reader_interval_checkbox) CheckBox mBlankBox;

    private int mReaderOrientationChoice;
    private int mReaderTurnChoice;

    @Override
    protected void initView() {
        mReaderOrientationChoice = mPreference.getInt(PreferenceManager.PREF_READER_STREAM_ORIENTATION, PreferenceManager.READER_ORIENTATION_PORTRAIT);
        mReaderTurnChoice = mPreference.getInt(PreferenceManager.PREF_READER_STREAM_TURN, PreferenceManager.READER_TURN_LTR);
        mSplitBox.setChecked(mPreference.getBoolean(PreferenceManager.PREF_READER_STREAM_SPLIT, false));
        mBlankBox.setChecked(mPreference.getBoolean(PreferenceManager.PREF_READER_STREAM_INTERVAL, false));
    }

    @OnClick({ R.id.settings_reader_click_event_btn, R.id.settings_reader_long_click_event_btn })
    void onReaderEventClick(View view) {
        boolean isLong = view.getId() == R.id.settings_reader_long_click_event_btn;
        Intent intent = EventSettingsActivity.createIntent(getActivity(), isLong,
                mReaderOrientationChoice == PreferenceManager.READER_ORIENTATION_PORTRAIT, true);
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

    @Override
    public void onChoicePositiveClick(int type, int choice, String value) {
        switch (type) {
            case TYPE_ORIENTATION:
                mPreference.putInt(PreferenceManager.PREF_READER_STREAM_ORIENTATION, choice);
                mReaderOrientationChoice = choice;
                break;
            case TYPE_TURN:
                mPreference.putInt(PreferenceManager.PREF_READER_STREAM_TURN, choice);
                mReaderTurnChoice = choice;
                break;
        }
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
