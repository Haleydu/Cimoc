package com.hiroshi.cimoc.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rx.functions.Func1;

/**
 * Created by Hiroshi on 2016/12/2.
 */

public class CollectionUtils {

    public static <E> Collection<E> minus(Collection<E> lhs, Collection<E> rhs) {
        Collection<E> collection = new ArrayList<>(lhs);
        lhs.removeAll(rhs);
        return collection;
    }

    public static <T, R> List<R> map(Collection<T> origin, Func1<T, R> func) {
        List<R> result = new ArrayList<>(origin.size());
        for (T element : origin) {
            result.add(func.call(element));
        }
        return result;
    }

    public static int[] unbox(List<Integer> list) {
        int[] result = new int[list.size()];
        for (int i = 0; i != result.length; ++i) {
            result[i] = list.get(i);
        }
        return result;
    }

}
