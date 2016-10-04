package com.hiroshi.cimoc.fresco;

import android.content.Context;
import android.util.SparseArray;

import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilderSupplier;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineFactory;

/**
 * Created by Hiroshi on 2016/9/5.
 */
public class ControllerBuilderProvider {

    private Context mContext;
    private SparseArray<PipelineDraweeControllerBuilderSupplier> mSupplierArray;
    private SparseArray<ImagePipeline> mPipelineArray;

    public ControllerBuilderProvider(Context context) {
        mSupplierArray = new SparseArray<>();
        mPipelineArray = new SparseArray<>();
        mContext = context;
    }

    public PipelineDraweeControllerBuilder get(int source) {
        PipelineDraweeControllerBuilderSupplier supplier = mSupplierArray.get(source);
        if (supplier == null) {
            ImagePipelineFactory factory = ImagePipelineFactoryBuilder.build(mContext, source);
            supplier = ControllerBuilderSupplierFactory.get(mContext, factory);
            mSupplierArray.put(source, supplier);
            mPipelineArray.put(source, factory.getImagePipeline());
        }
        return supplier.get();
    }

    public void clear() {
        for (int i = 0; i != mPipelineArray.size(); ++i) {
            mPipelineArray.valueAt(i).clearMemoryCaches();
        }
    }

}
