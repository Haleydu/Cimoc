package com.hiroshi.cimoc.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by Hiroshi on 2016/7/25.
 */
public class FileUtils {

    public static String getPath(String... filename) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i != filename.length - 1; ++i) {
            builder.append(filename[i]).append(File.separator);
        }
        builder.append(filename[filename.length - 1]);
        return builder.toString();
    }

    public static InputStream getInputStream(String pathname) throws FileNotFoundException {
        return new FileInputStream(new File(pathname));
    }

}
