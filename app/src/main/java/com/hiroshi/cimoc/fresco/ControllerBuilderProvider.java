package com.hiroshi.cimoc.fresco;

import android.content.Context;
import android.util.SparseArray;

import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilderSupplier;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.hiroshi.cimoc.manager.SourceManager;

/**
 * Created by Hiroshi on 2016/9/5.
 */
public class ControllerBuilderProvider {

    private Context mContext;
    private SparseArray<PipelineDraweeControllerBuilderSupplier> mSupplierArray;
    private SparseArray<ImagePipeline> mPipelineArray;
    private SourceManager.HeaderGetter mHeaderGetter;
    private boolean mCover;

    public ControllerBuilderProvider(Context context, SourceManager.HeaderGetter getter, boolean cover) {
        mSupplierArray = new SparseArray<>();
        mPipelineArray = new SparseArray<>();
        mContext = context;
        mHeaderGetter = getter;
        mCover = cover;
    }

    public PipelineDraweeControllerBuilder get(int type) {
        PipelineDraweeControllerBuilderSupplier supplier = mSupplierArray.get(type);
        if (supplier == null) {
            ImagePipelineFactory factory = ImagePipelineFactoryBuilder
                    .build(mContext, type < 0 ? null : mHeaderGetter.getHeader(type), mCover);
            supplier = ControllerBuilderSupplierFactory.get(mContext, factory);
            mSupplierArray.put(type, supplier);
            mPipelineArray.put(type, factory.getImagePipeline());
        }
        return supplier.get();
    }

    public void pause() {
        for (int i = 0; i != mPipelineArray.size(); ++i) {
            mPipelineArray.valueAt(i).pause();
        }
    }

    public void resume() {
        for (int i = 0; i != mPipelineArray.size(); ++i) {
            mPipelineArray.valueAt(i).resume();
        }
    }

    public void clear() {
        for (int i = 0; i != mPipelineArray.size(); ++i) {
            mPipelineArray.valueAt(i).clearMemoryCaches();
        }
    }

}
