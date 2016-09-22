package com.hiroshi.cimoc.fresco;

import android.content.Context;
import android.util.SparseArray;

import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineFactory;

/**
 * Created by Hiroshi on 2016/9/5.
 */
public class ControllerBuilderProvider {

    private Context mContext;
    private SparseArray<PipelineDraweeControllerBuilder> mBuilderArray;
    private SparseArray<ImagePipeline> mPipelineArray;

    public ControllerBuilderProvider(Context context) {
        mBuilderArray = new SparseArray<>(10);
        mPipelineArray = new SparseArray<>(10);
        mContext = context;
    }

    public PipelineDraweeControllerBuilder get(int source) {
        PipelineDraweeControllerBuilder builder = mBuilderArray.get(source);
        if (builder == null) {
            ImagePipelineFactory factory = ImagePipelineFactoryBuilder.build(mContext, source);
            builder = ControllerBuilderFactory.get(mContext, factory);
            mBuilderArray.put(source, builder);
            mPipelineArray.put(source, factory.getImagePipeline());
        }
        return builder;
    }

    public void clear() {
        for (int i = 0; i != mPipelineArray.size(); ++i) {
            mPipelineArray.valueAt(i).clearMemoryCaches();
        }
    }

}
