package com.hiroshi.cimoc.utils;

import android.content.ContentResolver;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Hiroshi on 2016/12/4.
 */

public class DocumentUtils {

    public static DocumentFile createFile(DocumentFile parent, String displayName) {
        if (parent.isDirectory()) {
            DocumentFile file = parent.findFile(displayName);
            if (file == null) {
                return parent.createFile(null, displayName);
            }
            return file;
        }
        return null;
    }

    public static DocumentFile findFile(DocumentFile parent, String... filenames) {
        if (parent != null) {
            for (String filename : filenames) {
                parent = parent.findFile(filename);
                if (parent == null) {
                    return null;
                }
            }
        }
        return parent;
    }

    public static Uri[] listUrisWithoutSuffix(DocumentFile dir, String suffix) {
        List<Uri> list = new ArrayList<>();
        if (dir.isDirectory()) {
            DocumentFile[] files = dir.listFiles();
            Arrays.sort(files, new Comparator<DocumentFile>() {
                @Override
                public int compare(DocumentFile lhs, DocumentFile rhs) {
                    return lhs.getName().compareTo(rhs.getName());
                }
            });
            for (DocumentFile file : files) {
                if (file.isFile() && !file.getName().endsWith(suffix)) {
                    list.add(file.getUri());
                }
            }
        }
        return list.toArray(new Uri[list.size()]);
    }

    public static int countWithoutSuffix(DocumentFile dir, String suffix) {
        int count = 0;
        if (dir.isDirectory()) {
            for (DocumentFile file : dir.listFiles()) {
                if (file.isFile() && !file.getName().endsWith(suffix)) {
                    ++count;
                }
            }
        }
        return count;
    }

    public static String[] listFilesWithSuffix(DocumentFile dir, String... suffix) {
        List<String> list = new ArrayList<>();
        if (dir.isDirectory()) {
            for (DocumentFile file : dir.listFiles()) {
                if (file.isFile()) {
                    String name = file.getName();
                    for (String str : suffix) {
                        if (name.endsWith(str)) {
                            list.add(name);
                            break;
                        }
                    }
                }
            }
        }
        return list.toArray(new String[list.size()]);
    }

    public static DocumentFile getOrCreateSubDirectory(DocumentFile parent, String displayName) {
        if (parent.isDirectory()) {
            DocumentFile file = parent.findFile(displayName);
            if (file != null && file.isDirectory()) {
                return file;
            }
            return parent.createDirectory(displayName);
        }
        return null;
    }

    public static String readLineFromFile(ContentResolver resolver, DocumentFile file) {
        InputStream input = null;
        BufferedReader reader = null;
        try {
            input = resolver.openInputStream(file.getUri());
            if (input != null) {
                reader = new BufferedReader(new InputStreamReader(input));
                return reader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeStream(input, reader);
        }
        return null;
    }

    public static char[] readCharFromFile(ContentResolver resolver, DocumentFile file, int count) {
        InputStream input = null;
        BufferedReader reader = null;
        try {
            input = resolver.openInputStream(file.getUri());
            if (input != null) {
                reader = new BufferedReader(new InputStreamReader(input));
                char[] buffer = new char[count];
                if (reader.read(buffer, 0, count) == count) {
                    return buffer;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeStream(input, reader);
        }
        return null;
    }

    public static void writeStringToFile(ContentResolver resolver, DocumentFile file, String data) throws IOException {
        OutputStream output = null;
        BufferedWriter writer = null;
        try {
            output = resolver.openOutputStream(file.getUri());
            if (output != null) {
                writer = new BufferedWriter(new OutputStreamWriter(output));
                writer.write(data);
                writer.flush();
            } else {
                throw new IOException();
            }
        } finally {
            closeStream(output, writer);
        }
    }

    public static void writeBinaryToFile(ContentResolver resolver, DocumentFile file, InputStream input) throws IOException {
        BufferedInputStream inputStream = null;
        BufferedOutputStream outputStream = null;

        try {
            OutputStream output = resolver.openOutputStream(file.getUri());

            if (output != null) {
                inputStream = new BufferedInputStream(input, 8192);
                outputStream = new BufferedOutputStream(output, 8192);

                int length;
                byte[] buffer = new byte[8192];
                while ((length = inputStream.read(buffer)) != -1){
                    outputStream.write(buffer, 0, length);
                }
                output.flush();
            } else {
                closeStream(input);
                throw new FileNotFoundException();
            }
        } finally {
            closeStream(inputStream, outputStream);
        }
    }

    public static void writeBinaryToFile(ContentResolver resolver, DocumentFile src, DocumentFile dst) throws IOException {
        InputStream input = resolver.openInputStream(src.getUri());
        writeBinaryToFile(resolver, dst, input);
    }

    private static boolean copyFile(ContentResolver resolver, DocumentFile src, DocumentFile parent) {
        if (src.isFile() && parent.isDirectory()) {
            DocumentFile old = parent.findFile(src.getName());
            if (old != null) {
                old.delete();
            }
            DocumentFile file = createFile(parent, src.getName());
            if (file != null) {
                try {
                    writeBinaryToFile(resolver, src, file);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public static boolean copyDir(ContentResolver resolver, DocumentFile src, DocumentFile parent) {
        if (src.isDirectory()) {
            DocumentFile dir = getOrCreateSubDirectory(parent, src.getName());
            for (DocumentFile file : src.listFiles()) {
                if (file.isDirectory()) {
                    if (!copyDir(resolver, file, dir)) {
                        return false;
                    }
                } else if (!copyFile(resolver, file, dir)) {
                    return false;
                }
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
