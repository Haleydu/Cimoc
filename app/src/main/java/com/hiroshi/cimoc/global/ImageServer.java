package com.hiroshi.cimoc.global;

import com.hiroshi.cimoc.core.manager.PreferenceManager;
import com.hiroshi.cimoc.core.manager.SourceManager;

/**
 * Created by Hiroshi on 2016/12/14.
 */

public class ImageServer {

    // 临时解决方案

    private static String SERVER_IKANMAN = "";
    private static String SERVER_HHAAZZ = "";
    private static String SERVER_57MH = "";

    public static void init(PreferenceManager manager) {
        SERVER_IKANMAN = manager.getString(PreferenceManager.PREF_SOURCE_SERVER_IKANMAN, getDefaultServer(SourceManager.SOURCE_IKANMAN));
        SERVER_HHAAZZ = manager.getString(PreferenceManager.PREF_SOURCE_SERVER_HHAAZZ, getDefaultServer(SourceManager.SOURCE_HHAAZZ));
        SERVER_57MH = manager.getString(PreferenceManager.PREF_SOURCE_SERVER_57MH, getDefaultServer(SourceManager.SOURCE_57MH));
    }

    public static void update(PreferenceManager manager, int source, String value) {
        switch (source) {
            case SourceManager.SOURCE_IKANMAN:
                manager.putString(PreferenceManager.PREF_SOURCE_SERVER_IKANMAN, value.trim());
                SERVER_IKANMAN = value.trim();
                break;
            case SourceManager.SOURCE_HHAAZZ:
                manager.putString(PreferenceManager.PREF_SOURCE_SERVER_HHAAZZ, value.trim());
                SERVER_HHAAZZ = value.trim();
                break;
            case SourceManager.SOURCE_57MH:
                manager.putString(PreferenceManager.PREF_SOURCE_SERVER_57MH, value.trim());
                SERVER_57MH = value.trim();
                break;
        }
    }

    public static String get(int source) {
        switch (source) {
            case SourceManager.SOURCE_IKANMAN:
                return SERVER_IKANMAN;
            case SourceManager.SOURCE_HHAAZZ:
                return SERVER_HHAAZZ;
            case SourceManager.SOURCE_57MH:
                return SERVER_57MH;
        }
        return "";
    }

    private static String getDefaultServer(int source) {
        switch (source) {
            case SourceManager.SOURCE_IKANMAN:
                return "http://p.yogajx.com http://idx0.hamreus.com:8080 http://ilt2.hamreus.com:8080";
            case SourceManager.SOURCE_HHAAZZ:
                return "http://x8.1112223333.com/dm01/ http://x8.1112223333.com/dm02/ http://x8.1112223333.com/dm03/ " +
                        "http://x8.1112223333.com/dm04/ http://x8.1112223333.com/dm05/ http://x8.1112223333.com/dm06/ " +
                        "http://x8.1112223333.com/dm07/ http://x8.1112223333.com/dm08/ http://x8.1112223333.com/dm09/ " +
                        "http://x8.1112223333.com/dm10/ http://x8.1112223333.com/dm11/ http://x8.1112223333.com/dm12/";
            case SourceManager.SOURCE_57MH:
                return "http://images.333dm.com http://cartoon.akshk.com";
        }
        return "";
    }

}
