package com.hiroshi.cimoc.saf;

import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Hiroshi on 2017/3/24.
 */

class RawDocumentFile extends DocumentFile {
    
    private File mFile;

    RawDocumentFile(DocumentFile parent, File file) {
        super(parent);
        mFile = file;
    }

    @Override
    public DocumentFile createFile(String displayName) {
        File target = new File(mFile, displayName);
        if (!target.exists()) {
            try {
                if (!target.createNewFile()) {
                    return null;
                }
            } catch (IOException e) {
                return null;
            }
        }
        return new RawDocumentFile(this, target);
    }

    @Override
    public DocumentFile createDirectory(String displayName) {
        final File target = new File(mFile, displayName);
        if (target.isDirectory() || target.mkdir()) {
            return new RawDocumentFile(this, target);
        }
        return null;
    }

    @Override
    public Uri getUri() {
        return Uri.fromFile(mFile);
    }

    @Override
    public String getName() {
        return mFile.getName();
    }

    @Override
    public String getType() {
        if (!mFile.isDirectory()) {
            return getTypeForName(mFile.getName());
        }
        return null;
    }

    @Override
    public boolean isDirectory() {
        return mFile.isDirectory();
    }

    @Override
    public boolean isFile() {
        return mFile.isFile();
    }

    @Override
    public boolean canRead() {
        return mFile.canRead();
    }

    @Override
    public boolean canWrite() {
        return mFile.canWrite();
    }

    @Override
    public boolean delete() {
        deleteContents(mFile);
        return mFile.delete();
    }

    @Override
    public boolean exists() {
        return mFile.exists();
    }

    @Override
    public DocumentFile[] listFiles() {
        final ArrayList<DocumentFile> results = new ArrayList<>();
        final File[] files = mFile.listFiles();
        if (files != null) {
            for (File file : files) {
                results.add(new RawDocumentFile(this, file));
            }
        }
        return results.toArray(new DocumentFile[results.size()]);
    }

    @Override
    public boolean renameTo(String displayName) {
        final File target = new File(mFile.getParentFile(), displayName);
        if (mFile.renameTo(target)) {
            mFile = target;
            return true;
        } else {
            return false;
        }
    }

    private static String getTypeForName(String name) {
        final int lastDot = name.lastIndexOf('.');
        if (lastDot >= 0) {
            final String extension = name.substring(lastDot + 1).toLowerCase();
            final String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (mime != null) {
                return mime;
            }
        }

        return "application/octet-stream";
    }

    private static boolean deleteContents(File dir) {
        File[] files = dir.listFiles();
        boolean success = true;
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    success &= deleteContents(file);
                }
                if (!file.delete()) {
                    Log.w(TAG, "Failed to delete " + file);
                    success = false;
                }
            }
        }
        return success;
    }
    
}
