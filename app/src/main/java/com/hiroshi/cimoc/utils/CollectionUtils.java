package com.hiroshi.cimoc.utils;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Hiroshi on 2016/12/2.
 */

public class CollectionUtils {

    public static <E> Collection<E> minus(Collection<E> lhs, Collection<E> rhs) {
        Collection<E> collection = new ArrayList<>(lhs);
        lhs.removeAll(rhs);
        return collection;
    }

}
