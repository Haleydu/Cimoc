package com.hiroshi.cimoc.core.parser;

import com.hiroshi.cimoc.model.Pair;

import java.util.List;

/**
 * Created by Hiroshi on 2016/12/10.
 */

public interface Category {

    boolean isComposite();

    String composite(String... args);

    boolean hasClassification();

    List<Pair<String, String>> getClassification();

    boolean hasArea();

    List<Pair<String, String>> getArea();

    boolean hasReader();

    List<Pair<String, String>> getReader();

    boolean hasProgress();

    List<Pair<String, String>> getProgress();

    boolean hasYear();

    List<Pair<String, String>> getYear();

    boolean hasOrder();

    List<Pair<String, String>> getOrder();

}
