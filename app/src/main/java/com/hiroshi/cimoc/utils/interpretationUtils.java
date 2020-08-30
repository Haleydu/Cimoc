package com.hiroshi.cimoc.utils;

import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.source.CCMH;
import com.hiroshi.cimoc.source.Cartoonmad;
import com.hiroshi.cimoc.source.Comic18;
import com.hiroshi.cimoc.source.CopyMH;
import com.hiroshi.cimoc.source.MH50;
import com.hiroshi.cimoc.source.Manhuatai;
import com.hiroshi.cimoc.source.MiGu;
import com.hiroshi.cimoc.source.Tencent;

public class interpretationUtils {

    public static boolean isReverseOrder(Comic comic){
        int type = comic.getSource();
        return type == MH50.TYPE ||
                type == MiGu.TYPE ||
                type == CCMH.TYPE ||
                type == Cartoonmad.TYPE ||
                type == Comic18.TYPE ||
                type == Manhuatai.TYPE ||
                type == Tencent.TYPE ||
                type == CopyMH.TYPE;
    }
}

