package com.hiroshi.cimoc.utils;

public class pictureUtils {

    public static boolean isPictureFormat(String suffix){
        suffix = suffix.toLowerCase();
        if (suffix.equals("bmp") ||
                suffix.equals("jpg") ||
                suffix.equals("png") ||
                suffix.equals("tif") ||
                suffix.equals("gif") ||
                suffix.equals("pcx") ||
                suffix.equals("tga") ||
                suffix.equals("exif") ||
                suffix.equals("fpx") ||
                suffix.equals("svg") ||
                suffix.equals("psd") ||
                suffix.equals("cdr") ||
                suffix.equals("pcd") ||
                suffix.equals("dxf") ||
                suffix.equals("ufo") ||
                suffix.equals("eps") ||
                suffix.equals("ai") ||
                suffix.equals("raw") ||
                suffix.equals("wmf") ||
                suffix.equals("webp")){
            return true;
        }
        return false;
    }
}
