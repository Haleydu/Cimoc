package com.hiroshi.cimoc.ui.adapter;

import com.hiroshi.cimoc.model.Chapter;

/**
 * Created by Hiroshi on 2016/7/29.
 */
public class PreloadAdapter {
    
    private Chapter[] array;
    private int[] offset;
    private int current;
    
    private int index;
    private int prev;
    private int next;
    
    public PreloadAdapter(Chapter[] array, int index, int current) {
        this.array = array;
        this.index = index;
        this.current = current;

        offset = new int[array.length + 1];
        offset[index] = current;
        prev = index - 1;
        next = index;
    }
    
    public Chapter getPrevChapter() {
        return prev >= 0 ? array[prev] : null;
    }
    
    public Chapter getNextChapter() {
        return next < array.length ? array[next] : null;
    }

    public Chapter getValidChapter() {
        if (prev < index && index < next) {
            return array[index];
        }
        return null;
    }

    public int getMax() {
        return offset[index + 1] - offset[index];
    }

    public int getValidProgress() {
        return current - offset[index] + 1;
    }
    
    public boolean moveToPosition(int position) {
        if (current == position) {
            return false;
        }
        current = position;
        if (index == -1) {
            ++index;
            return true;
        }
        if (index == offset.length - 1) {
            --index;
            return true;
        }
        if (position == offset[index] - 1){
            --index;
            return true;
        }
        if (position == offset[index + 1]) {
            ++index;
            return true;
        }
        return false;
    }

    public int getCurrentOffset() {
        return offset[index];
    }
    
    public void movePrev(int value) {
        offset[prev] = offset[prev + 1] - value;
        --prev;
    }
    
    public void moveNext(int value) {
        offset[next + 1] = offset[next] + value;
        ++next;
    }
    
    
}
