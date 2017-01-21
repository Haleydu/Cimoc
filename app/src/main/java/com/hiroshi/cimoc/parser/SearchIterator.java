package com.hiroshi.cimoc.parser;

import com.hiroshi.cimoc.model.Comic;

/**
 * Created by Hiroshi on 2016/9/21.
 */

public interface SearchIterator {

    boolean empty();

    boolean hasNext();

    Comic next();

}
