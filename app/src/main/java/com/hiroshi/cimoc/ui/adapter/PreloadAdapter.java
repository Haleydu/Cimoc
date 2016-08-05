package com.hiroshi.cimoc.ui.adapter;

import com.hiroshi.cimoc.model.Chapter;

import java.util.List;

/**
 * Created by Hiroshi on 2016/7/29.
 */
public class PreloadAdapter {
    
    private List<Chapter> list;
    private int index;
    private int prev;
    private int next;
    
    public PreloadAdapter(List<Chapter> list, int index) {
        this.list = list;
        this.index = index;
        prev = index + 1;
        next = index;
    }
    
    public Chapter getPrevChapter() {
        return prev < list.size() ? list.get(prev) : null;
    }
    
    public Chapter getNextChapter() {
        return next >= 0 ? list.get(next) : null;
    }

    public Chapter prevChapter() {
        if (++index < prev) {
            return list.get(index);
        }
        return null;
    }

    public Chapter nextChapter() {
        if (--index > next) {
            return list.get(index);
        }
        return null;
    }

    public Chapter movePrev() {
        return list.get(prev++);
    }

    public Chapter moveNext() {
        return list.get(next--);
    }

    public int getOffset() {
        int offset = 0;
        for (int i = prev - 1; i > index; --i) {
            offset += list.get(i).getCount();
        }
        return offset;
    }

    public boolean isLoad() {
        return index != next;
    }
    
}
