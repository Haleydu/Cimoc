package com.hiroshi.cimoc.core.parser;

import android.support.annotation.IntDef;

import com.hiroshi.cimoc.model.Pair;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * Created by Hiroshi on 2016/12/10.
 */

public interface Category {

    int CATEGORY_SUBJECT = 0;
    int CATEGORY_AREA = 1;
    int CATEGORY_READER = 2;
    int CATEGORY_PROGRESS = 3;
    int CATEGORY_YEAR = 4;
    int CATEGORY_ORDER = 5;

    @IntDef({CATEGORY_SUBJECT, CATEGORY_AREA, CATEGORY_READER, CATEGORY_PROGRESS, CATEGORY_YEAR, CATEGORY_ORDER})
    @Retention(RetentionPolicy.SOURCE)
    @interface Attribute {}

    boolean isComposite();

    String getFormat(String... args);

    boolean hasAttribute(@Attribute int attr);

    List<Pair<String, String>> getAttrList(@Attribute int attr);

}
