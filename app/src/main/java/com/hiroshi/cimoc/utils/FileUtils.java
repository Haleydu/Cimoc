package com.hiroshi.cimoc.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

    public static String filterFilename(String text) {
        return text.replaceAll("[\\|\\?\\*\\\\/:<>]", "");
    }

    public static void deleteDir(File dir) {
        if (dir.exists() && dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (!file.isDirectory() || file.listFiles().length == 0) {
                    file.delete();
                } else {
                    deleteDir(file);
                }
            }
            dir.delete();
        }
    }

    public static void deleteDir(String dirPath) {
        deleteDir(new File(dirPath));
    }

    public static boolean mkDirsIfNotExist(File dir) {
        return dir.exists() || dir.mkdirs();
    }

    public static boolean mkDirsIfNotExist(String dirPath) {
        return mkDirsIfNotExist(new File(dirPath));
    }

    public static boolean isDirsExist(String dirPath) {
        return new File(dirPath).exists();
    }

    public static boolean rename(String oldPath, String newPath) {
        return new File(oldPath).renameTo(new File(newPath));
    }

    public static char[] readCharFromFile(File file, int count) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            char[] buffer = new char[count];
            reader.read(buffer, 0, count);
            reader.close();
            return buffer;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static char[] readCharFromFile(String dirPath, String name, int count) {
        return readCharFromFile(new File(dirPath, name), count);
    }

    public static String readSingleLineFromFile(File file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String result = reader.readLine();
            reader.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String readSingleLineFromFile(String dirPath, String name) {
        return readSingleLineFromFile(new File(dirPath, name));
    }

    public static File[] listFiles(String dirPath) {
        return listFiles(new File(dirPath));
    }

    public static File[] listFiles(File dir) {
        if (dir.exists() && dir.isDirectory()) {
            return dir.listFiles();
        }
        return null;
    }

    public static String[] listFilesName(String dirPath) {
        return listFilesNameHaveSuffix(new File(dirPath), "");
    }

    private static String[] listFilesNameHaveSuffix(File dir, final String... suffix) {
        if (dir.exists() && dir.isDirectory()) {
            return dir.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    for (String s : suffix) {
                        if (filename.endsWith(s)) {
                            return true;
                        }
                    }
                    return false;
                }
            });
        }
        return null;
    }

    public static String[] listFilesNameHaveSuffix(String dirPath, String... suffix) {
        return listFilesNameHaveSuffix(new File(dirPath), suffix);
    }

    private static String[] listFilesNameNoSuffix(File dir, final String suffix) {
        if (dir.exists() && dir.isDirectory()) {
            return dir.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return !filename.endsWith(suffix);
                }
            });
        }
        return null;
    }

    public static String[] listFilesNameNoSuffix(String dirPath, String suffix) {
        return listFilesNameNoSuffix(new File(dirPath), suffix);
    }

    public static InputStream getInputStream(String pathname) throws FileNotFoundException {
        return new FileInputStream(new File(pathname));
    }

    public static boolean copyFile(String src, String dst) {
        InputStream input = null;
        OutputStream output = null;
        try {
            input = new FileInputStream(src);
            output = new FileOutputStream(dst);
            byte[] buffer = new byte[1024];
            int count;
            while ((count = input.read(buffer)) > 0) {
                output.write(buffer, 0, count);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeStream(input, output);
        }
        return false;
    }

    public static boolean copyFolder(String srcPath, String dstPath) {
        File srcDir = new File(srcPath);
        if (!srcDir.exists() || !srcDir.isDirectory()) {
            return false;
        }

        File dstDir = new File(dstPath);
        if(!dstDir.exists() && !dstDir.mkdirs()) {
            return false;
        }

        if (srcDir.getAbsolutePath().equals(dstDir.getAbsolutePath())) {
            return false;
        }

        for (File file : srcDir.listFiles()) {
            if (file.isFile() && !copyFile(file.getAbsolutePath(), getPath(dstPath, filterFilename(file.getName()))) ||
                    file.isDirectory() && !copyFolder(file.getAbsolutePath(), getPath(dstPath, filterFilename(file.getName())))) {
                return false;
            }
        }
        return true;
    }

    private static void closeStream(Closeable... stream) {
        for (Closeable closeable : stream) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
