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
            builder.append(filterFilename(filename[i])).append(File.separator);
        }
        builder.append(filterFilename(filename[filename.length - 1]));
        return builder.toString();
    }

    public static String filterFilename(String text) {
        return text.replaceAll("[\\|\\?\\*\\\\:<>]", "");
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

    public static boolean createFile(String dirPath, String filename) {
        File file = new File(dirPath, filename);
        if (!file.exists()) {
            try {
                return mkDirsIfNotExist(dirPath) && file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
        return true;
    }

    private static boolean writeStringToFile(File file, String data) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));
            writer.write(data);
            writer.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean writeStringToFile(String dirPath, String filename, String data) {
        return mkDirsIfNotExist(dirPath) && writeStringToFile(new File(dirPath, filename), data);
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

    private static String[] listFilesNameHaveSuffix(File dir, final String suffix) {
        if (dir.exists() && dir.isDirectory()) {
            return dir.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.endsWith(suffix);
                }
            });
        }
        return null;
    }

    public static String[] listFilesNameHaveSuffix(String dirPath, String suffix) {
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

    private static boolean writeBinaryToFile(File file, InputStream byteStream) {
        OutputStream out = null;
        try {
            out = new FileOutputStream(file, false);
            int length;
            byte[] buffer = new byte[1024];
            while ((length = byteStream.read(buffer)) != -1){
                out.write(buffer, 0, length);
            }
            out.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeStream(out, byteStream);
        }
        return false;
    }

    public static boolean writeBinaryToFile(String dirPath, String filename, InputStream byteStream) {
        return mkDirsIfNotExist(dirPath) && writeBinaryToFile(new File(dirPath, filename), byteStream);
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
            return true;
        }

        for (File file : srcDir.listFiles()) {
            if (file.isFile() && !copyFile(file.getAbsolutePath(), getPath(dstPath, file.getName())) ||
                    file.isDirectory() && !copyFolder(file.getAbsolutePath(), getPath(dstPath, file.getName()))) {
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
