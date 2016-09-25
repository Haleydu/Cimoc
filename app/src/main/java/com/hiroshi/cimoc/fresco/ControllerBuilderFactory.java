package com.hiroshi.cimoc.fresco;

import android.content.Context;

import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilderSupplier;
import com.facebook.imagepipeline.core.ImagePipelineFactory;

/**
 * Created by Hiroshi on 2016/9/5.
 */
public class ControllerBuilderFactory {

    public static PipelineDraweeControllerBuilder get(Context context, ImagePipelineFactory factory) {
        return new PipelineDraweeControllerBuilderSupplier(context, factory).get();
    }

    public static PipelineDraweeControllerBuilder get(Context context, int source) {
        ImagePipelineFactory factory = ImagePipelineFactoryBuilder.build(context, source);
        return new PipelineDraweeControllerBuilderSupplier(context.getApplicationContext(), factory).get();
    }

}
