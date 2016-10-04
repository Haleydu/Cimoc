package com.hiroshi.cimoc.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by Hiroshi on 2016/9/24.
 */

public abstract class FilterList<T, E> extends LinkedList<T> {

    private List<T> fullList;
    private Set<E> filterSet;

    protected FilterList(Set<E> filterSet) {
        super();
        this.fullList = new LinkedList<>();
        this.filterSet = filterSet;
    }

    public void filter() {
        List<T> list = new LinkedList<>();
        for (T t : fullList) {
            if (isFilter(filterSet, t)) {
                list.add(t);
            }
        }
        super.clear();
        super.addAll(list);
    }

    public Set<E> getFilterSet() {
        return filterSet;
    }

    @Override
    public boolean remove(Object o) {
        return fullList.remove(o) && super.remove(o);
    }

    @Override
    public T remove(int index) {
        fullList.remove(super.get(index));
        return super.remove(index);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        fullList.removeAll(c);
        return super.removeAll(c);
    }

    @Override
    public boolean add(T t) {
        fullList.add(t);
        return isFilter(filterSet, t) && super.add(t);
    }

    @Override
    public void add(int index, T element) {
        fullList.add(index, element);
        if (isFilter(filterSet, element)) {
            super.add(index, element);
        }
    }

    public void addDiff(int index1, int index2, T element) {
        fullList.add(index1, element);
        super.add(index2, element);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        List<T> tempList = new ArrayList<>(c.size());
        for (T t : c) {
            if (isFilter(filterSet, t)) {
                tempList.add(t);
            }
        }

        fullList.addAll(index, c);
        return !tempList.isEmpty() && super.addAll(index, tempList);
    }

    public List<T> getFullList() {
        return fullList;
    }

    public boolean isFull() {
        return super.size() == fullList.size();
    }

    protected abstract boolean isFilter(Set<E> filter, T data);

}
