package com.hiroshi.cimoc.ui.adapter;

import com.hiroshi.cimoc.model.Chapter;

/**
 * Created by Hiroshi on 2016/7/29.
 */
public class PreloadAdapter {
    
    private Chapter[] array;
    private int index;
    private int prev;
    private int next;
    
    public PreloadAdapter(Chapter[] array, int index) {
        this.array = array;
        this.index = index;
        prev = index + 1;
        next = index;
    }
    
    public Chapter getPrevChapter() {
        return prev < array.length ? array[prev] : null;
    }
    
    public Chapter getNextChapter() {
        return next >= 0 ? array[next] : null;
    }

    public Chapter prevChapter() {
        if (index + 1 < prev) {
            return array[++index];
        }
        return null;
    }

    public Chapter nextChapter() {
        if (index - 1 > next) {
            return array[--index];
        }
        return null;
    }

    public Chapter currentChapter() {
        return array[index];
    }

    public int getCurrent() {
        return index;
    }

    public Chapter movePrev() {
        return array[prev++];
    }

    public Chapter moveNext() {
        return array[next--];
    }

    public boolean isLoad() {
        return prev != next + 1;
    }
    
}
