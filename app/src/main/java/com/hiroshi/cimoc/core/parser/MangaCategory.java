package com.hiroshi.cimoc.core.parser;

import com.hiroshi.cimoc.model.Pair;

import java.util.List;

/**
 * Created by Hiroshi on 2016/12/10.
 */

public abstract class MangaCategory implements Category {

    @Override
    public boolean isComposite() {
        return false;
    }

    @Override
    public String composite(String... args) {
        return null;
    }

    @Override
    public boolean hasClassification() {
        return true;
    }

    @Override
    public boolean hasArea() {
        return false;
    }

    @Override
    public List<Pair<String, String>> getArea() {
        return null;
    }

    @Override
    public boolean hasReader() {
        return false;
    }

    @Override
    public List<Pair<String, String>> getReader() {
        return null;
    }

    @Override
    public boolean hasProgress() {
        return false;
    }

    @Override
    public List<Pair<String, String>> getProgress() {
        return null;
    }

    @Override
    public boolean hasYear() {
        return false;
    }

    @Override
    public List<Pair<String, String>> getYear() {
        return null;
    }

    @Override
    public boolean hasOrder() {
        return false;
    }

    @Override
    public List<Pair<String, String>> getOrder() {
        return null;
    }

}
