package com.hiroshi.cimoc.parser;

import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.soup.Node;

import java.util.List;
import java.util.ListIterator;

/**
 * Created by Hiroshi on 2016/9/21.
 */

public abstract class NodeIterator implements SearchIterator {

    private ListIterator<Node> iterator;

    protected NodeIterator(List<Node> list) {
        this.iterator = list.isEmpty() ? null : list.listIterator();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public Comic next() {
        return parse(iterator.next());
    }

    @Override
    public boolean empty() {
        return iterator == null;
    }

    protected abstract Comic parse(Node node);

}
