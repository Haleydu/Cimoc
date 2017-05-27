package com.hiroshi.cimoc.parser;

import android.support.annotation.IntDef;

import com.hiroshi.cimoc.misc.Pair;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * Created by Hiroshi on 2016/12/10.
 */

public interface Category {

    /**
     * 主题 地区 读者 年份 进度 排序
     */
    int CATEGORY_SUBJECT = 0;
    int CATEGORY_AREA = 1;
    int CATEGORY_READER = 2;
    int CATEGORY_YEAR = 3;
    int CATEGORY_PROGRESS = 4;
    int CATEGORY_ORDER = 5;

    @IntDef({CATEGORY_SUBJECT, CATEGORY_AREA, CATEGORY_READER, CATEGORY_YEAR, CATEGORY_PROGRESS, CATEGORY_ORDER})
    @Retention(RetentionPolicy.SOURCE)
    @interface Attribute {}

    /**
     * 选项是否可以组合，例如有些网站可以根据几个选项一起搜索
     */
    boolean isComposite();

    /**
     * 获取最终的格式化字符串，一般需要含有一个 %d 用于填充页码
     * @param args 各个选项的值，按照定义的顺序
     */
    String getFormat(String... args);

    /**
     * 判断是否存在某个选项，用于确定界面中的 Spinner
     */
    boolean hasAttribute(@Attribute int attr);

    /**
     * 获取选项列表
     * 左边的 String 为显示的名称，右边的 String 为用来构造 url 的值
     */
    List<Pair<String, String>> getAttrList(@Attribute int attr);

}
