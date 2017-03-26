package com.hiroshi.cimoc.utils;

import android.support.v4.util.LongSparseArray;

import com.hiroshi.cimoc.manager.ComicManager;
import com.hiroshi.cimoc.model.Comic;

/**
 * Created by Hiroshi on 2017/3/24.
 */

public class ComicUtils {

    public static LongSparseArray<Comic> buildDownloadComicMap(ComicManager manager) {
        LongSparseArray<Comic> array = new LongSparseArray<>();
        for (Comic comic : manager.listDownload()) {
            array.put(comic.getId(), comic);
        }
        return array;
    }

}
