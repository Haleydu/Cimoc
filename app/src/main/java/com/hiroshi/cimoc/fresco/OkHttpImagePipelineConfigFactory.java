/*
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.hiroshi.cimoc.fresco;

import android.content.Context;

import com.facebook.imagepipeline.core.ImagePipelineConfig;

import okhttp3.Headers;
import okhttp3.OkHttpClient;

/**
 * Factory for getting an {@link com.facebook.imagepipeline.core.ImagePipelineConfig} that uses
 * {@link OkHttpNetworkFetcher}.
 */
public class OkHttpImagePipelineConfigFactory {

    public static ImagePipelineConfig.Builder newBuilder(Context context, OkHttpClient okHttpClient, Headers headers) {
        return ImagePipelineConfig.newBuilder(context.getApplicationContext())
                .setNetworkFetcher(new OkHttpNetworkFetcher(okHttpClient, headers));
    }

}