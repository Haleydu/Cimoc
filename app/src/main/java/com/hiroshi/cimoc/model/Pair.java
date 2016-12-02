package com.hiroshi.cimoc.model;

/**
 * Created by Hiroshi on 2016/12/1.
 */

public class Pair<F, S> {

    public F first;
    public S second;

    private Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public static <F, S> Pair<F, S> create(F first, S second) {
        return new Pair<>(first, second);
    }

}
