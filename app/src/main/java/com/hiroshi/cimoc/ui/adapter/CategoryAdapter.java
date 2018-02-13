package com.hiroshi.cimoc.ui.adapter;

import android.content.Context;
import android.util.Pair;
import android.widget.ArrayAdapter;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.utils.CollectionUtils;

import java.util.List;

import rx.functions.Func1;

/**
 * Created by Hiroshi on 2018/2/13.
 */

public class CategoryAdapter extends ArrayAdapter<String> {

    private List<Pair<String, String>> mCategoryList;

    public CategoryAdapter(Context context, List<Pair<String, String>> list){
        super(context, R.layout.item_spinner, CollectionUtils.map(list, new Func1<Pair<String,String>, String>() {
            @Override
            public String call(Pair<String, String> pair) {
                return pair.first;
            }
        }));
        mCategoryList = list;
    }

    public String getValue(int position) {
        return mCategoryList.get(position).second;
    }

}
