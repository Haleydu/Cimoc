package com.hiroshi.cimoc.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.support.v4.provider.DocumentFile;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hiroshi on 2016/12/4.
 */

public class DocumentUtils {

    public static Uri[] listToUri(DocumentFile directory) {
        List<Uri> list = new ArrayList<>();
        if (directory.isDirectory()) {
            for (DocumentFile file : directory.listFiles()) {
                if (file.isFile()) {
                    list.add(file.getUri());
                }
            }
        }
        return list.toArray(new Uri[list.size()]);
    }

    public static DocumentFile createFile(DocumentFile parent, String mimeType, String displayName) {
        if (parent.isDirectory()) {
            return parent.createFile(mimeType, displayName);
        }
        return null;
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

    public static void writeStringToFile(ContentResolver resolver, DocumentFile file, String data) throws IOException {
        OutputStream out = null;
        BufferedWriter writer = null;
        try {
            out = resolver.openOutputStream(file.getUri());
            if (out != null) {
                writer = new BufferedWriter(new OutputStreamWriter(out));
                writer.write(data);
                writer.close();
            } else {
                throw new IOException();
            }
        } finally {
            closeStream(out, writer);
        }
    }

    public static void writeBinaryToFile(ContentResolver resolver, DocumentFile file, InputStream byteStream) throws IOException {
        OutputStream out = null;
        try {
            out = resolver.openOutputStream(file.getUri());
            if (out != null) {
                int length;
                byte[] buffer = new byte[1024];
                while ((length = byteStream.read(buffer)) != -1){
                    out.write(buffer, 0, length);
                }
                out.flush();
            } else {
                throw new IOException();
            }
        } finally {
            closeStream(out, byteStream);
        }
    }

    public static void writeBinaryToFile(ContentResolver resolver, DocumentFile dst, DocumentFile src) throws IOException {
        InputStream in = resolver.openInputStream(src.getUri());
        writeBinaryToFile(resolver, dst, in);
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
