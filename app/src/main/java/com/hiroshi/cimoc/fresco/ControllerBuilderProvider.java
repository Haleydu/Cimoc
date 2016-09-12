package com.hiroshi.cimoc.fresco;

import android.content.Context;
import android.util.SparseArray;

import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;

/**
 * Created by Hiroshi on 2016/9/5.
 */
public class ControllerBuilderProvider {

    private Context mContext;
    private SparseArray<PipelineDraweeControllerBuilder> mArray;

    public ControllerBuilderProvider(Context context) {
        mArray = new SparseArray<>();
        mContext = context;
    }

    public PipelineDraweeControllerBuilder get(int source) {
        PipelineDraweeControllerBuilder builder = mArray.get(source);
        if (builder == null) {
            builder = ControllerBuilderFactory.get(mContext, source);
            mArray.put(source, builder);
        }
        return builder;
    }

}
