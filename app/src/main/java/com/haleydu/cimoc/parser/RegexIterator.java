package com.haleydu.cimoc.parser;

import com.haleydu.cimoc.model.Comic;
import com.haleydu.cimoc.soup.Node;

import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;

/**
 * Created by Hiroshi on 2016/9/21.
 */

public abstract class RegexIterator implements SearchIterator {

    private Matcher match;

    protected RegexIterator(Matcher match) {
        this.match = match;
    }

    @Override
    public boolean hasNext() {
        return  match.find();
    }

    @Override
    public Comic next() {
        return parse(match);
    }

    @Override
    public boolean empty() {
        return false;
    }

    protected abstract Comic parse(Matcher match);

}
