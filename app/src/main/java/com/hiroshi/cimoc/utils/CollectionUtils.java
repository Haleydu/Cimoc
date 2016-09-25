package com.hiroshi.cimoc.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by Hiroshi on 2016/9/24.
 */

public class CollectionUtils {

    public static <E> int findFirstFromList(List<E> list, Condition<E> condition) {
        ListIterator<E> iterator = list.listIterator();
        while (iterator.hasNext()) {
            int index = iterator.nextIndex();
            if (condition.call(index, iterator.next())) {
                return index;
            }
        }
        return list.size();
    }

    public static <E> List<E> findAllToList(Collection<E> collection, Condition<E> condition) {
        List<E> list = new ArrayList<>(collection.size());
        int position = 0;
        for (E element : collection) {
            if (condition.call(position++, element)) {
                list.add(element);
            }
        }
        return list;
    }

    public static <E, T> List<T> findAllToList(Collection<E> collection, Condition<E> condition, Construct<E, T> construct) {
        List<T> list = new ArrayList<>(collection.size());
        int position = 0;
        for (E element : collection) {
            if (condition.call(position++, element)) {
                list.add(construct.call(element));
            }
        }
        return list;
    }

    public interface Condition<E> {
        boolean call(int position, E element);
    }

    public interface Construct<E, T> {
        T call(E element);
    }

}
