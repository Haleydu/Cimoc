package com.haleydu.cimoc.ui.fragment.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import androidx.fragment.app.DialogFragment;

import com.haleydu.cimoc.R;
import com.haleydu.cimoc.component.DialogCaller;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiAdpaterDialogFragment extends DialogFragment implements DialogInterface.OnClickListener{

    private boolean[] mCheckArray;
    private SimpleAdapter adapter;
    private ArrayList<Map<String, Object>> arrayList = new ArrayList<Map<String, Object>>();
    private View getlistview;

    public static MultiAdpaterDialogFragment newInstance(int title, String[] item, boolean[] check, int requestCode) {
        MultiAdpaterDialogFragment fragment = new MultiAdpaterDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogCaller.EXTRA_DIALOG_TITLE, title);
        bundle.putStringArray(DialogCaller.EXTRA_DIALOG_ITEMS, item);
        bundle.putBooleanArray(DialogCaller.EXTRA_DIALOG_CHOICE_ITEMS, check);
        bundle.putInt(DialogCaller.EXTRA_DIALOG_REQUEST_CODE, requestCode);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String[] item = getArguments().getStringArray(DialogCaller.EXTRA_DIALOG_ITEMS);
        if (item == null) {
            item = new String[0];
        }
        initCheckArray(item.length);

        for (int i = 0; i < item.length; i++) {
            Map<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put("text", item[i]);
            arrayList.add(hashMap);
        }

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        getlistview = inflater.inflate(R.layout.listview_adapter, null);
        ListView listview = (ListView) getlistview.findViewById(R.id.listview_adapter);

        adapter = new SetSimpleAdapter(getActivity(), arrayList, R.layout.item_select_mutil, new String[]{"text"}, new int[]{R.id.item_select_title_mutil});
        listview.setAdapter(adapter);
        listview.setCacheColorHint(Color.TRANSPARENT);
        listview.setDivider(null);
        listview.setSelector(new ColorDrawable());
        listview.setItemsCanFocus(true);
        listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckBox cBox = (CheckBox) view.findViewById(R.id.item_select_checkbox_mutil);
                if (cBox.isChecked()) {
                    cBox.setChecked(false);
                    mCheckArray[position] = false;
                } else {
                    cBox.setChecked(true);
                    mCheckArray[position] = true;
                }
                adapter.notifyDataSetChanged();
            }
        });

        builder.setTitle(getArguments().getInt(DialogCaller.EXTRA_DIALOG_TITLE))
                .setView(getlistview)
                .setPositiveButton(R.string.dialog_positive, this)
                .setNeutralButton(R.string.comic_inverse_selection,this);
        return builder.create();
    }

    private void initCheckArray(int length) {
        mCheckArray = getArguments().getBooleanArray(DialogCaller.EXTRA_DIALOG_CHOICE_ITEMS);
        if (mCheckArray == null) {
            mCheckArray = new boolean[length];
            for (int i = 0; i != length; ++i) {
                mCheckArray[i] = false;
            }
        }
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        switch (which) {
            case Dialog.BUTTON_POSITIVE:
                int requestCode = getArguments().getInt(DialogCaller.EXTRA_DIALOG_REQUEST_CODE);
                Bundle bundle = new Bundle();
                bundle.putBooleanArray(DialogCaller.EXTRA_DIALOG_RESULT_VALUE, mCheckArray);
                DialogCaller target = (DialogCaller) (getTargetFragment() != null ? getTargetFragment() : getActivity());
                target.onDialogResult(requestCode, bundle);
                isCloseDialog(dialogInterface,true);
                break;
            case Dialog.BUTTON_NEUTRAL:
                for (int i=0;i<mCheckArray.length;i++){
                    mCheckArray[i]=!mCheckArray[i];
                }
                adapter.notifyDataSetChanged();
                isCloseDialog(dialogInterface,false);
                break;
            default:
                isCloseDialog(dialogInterface,true);
                break;
        }
    }

    private void isCloseDialog(DialogInterface dialog, boolean close) {
        try {
            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
            field.setAccessible(true);
            field.set(dialog, close);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class SetSimpleAdapter extends SimpleAdapter {

        public SetSimpleAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) convertView = LinearLayout.inflate(getActivity(), R.layout.item_select_mutil, null);
            CheckBox ckBox = (CheckBox) convertView.findViewById(R.id.item_select_checkbox_mutil);
            if (mCheckArray[position] == true) {
                ckBox.setChecked(true);
            } else if (mCheckArray[position] == false) {
                ckBox.setChecked(false);
            }
            return super.getView(position, convertView, parent);
        }
    }
}
