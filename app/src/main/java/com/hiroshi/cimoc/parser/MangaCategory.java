package com.hiroshi.cimoc.parser;

import android.util.Pair;

import java.util.List;

/**
 * Created by Hiroshi on 2016/12/10.
 */

public abstract class MangaCategory implements Category {

    @Override
    public boolean isComposite() {
        return false;
    }

    protected abstract List<Pair<String, String>> getSubject();

    protected boolean hasArea() {
        return false;
    }

    protected List<Pair<String, String>> getArea() {
        return null;
    }

    protected boolean hasReader() {
        return false;
    }

    protected List<Pair<String, String>> getReader() {
        return null;
    }

    protected boolean hasProgress() {
        return false;
    }

    protected List<Pair<String, String>> getProgress() {
        return null;
    }

    protected boolean hasYear() {
        return false;
    }

    protected List<Pair<String, String>> getYear() {
        return null;
    }

    protected boolean hasOrder() {
        return false;
    }

    protected List<Pair<String, String>> getOrder() {
        return null;
    }

    @Override
    public boolean hasAttribute(@Attribute int attr) {
        switch (attr) {
            case CATEGORY_SUBJECT:
                return true;
            case CATEGORY_AREA:
                return hasArea();
            case CATEGORY_READER:
                return hasReader();
            case CATEGORY_PROGRESS:
                return hasProgress();
            case CATEGORY_YEAR:
                return hasYear();
            case CATEGORY_ORDER:
                return hasOrder();
        }
        return false;
    }

    @Override
    public List<Pair<String, String>> getAttrList(@Attribute int attr) {
        switch (attr) {
            case CATEGORY_SUBJECT:
                return getSubject();
            case CATEGORY_AREA:
                return getArea();
            case CATEGORY_READER:
                return getReader();
            case CATEGORY_PROGRESS:
                return getProgress();
            case CATEGORY_YEAR:
                return getYear();
            case CATEGORY_ORDER:
                return getOrder();
        }
        return null;
    }

}
