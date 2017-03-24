package com.hiroshi.cimoc.saf;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;

import java.io.File;

/**
 * Created by Hiroshi on 2017/3/24.
 */

public abstract class DocumentFile {

    static final String TAG = "DocumentFile";

    private final DocumentFile mParent;

    DocumentFile(DocumentFile parent) {
        mParent = parent;
    }

    public static DocumentFile fromFile(File file) {
        return new RawDocumentFile(null, file);
    }

    public static DocumentFile fromTreeUri(Context context, Uri treeUri) {
        if (Build.VERSION.SDK_INT >= 21) {
            Uri documentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri,
                    DocumentsContract.getTreeDocumentId(treeUri));
            return new TreeDocumentFile(null, context, documentUri);
        }
        return null;
    }

    public abstract DocumentFile createFile(String displayName);

    public abstract DocumentFile createDirectory(String displayName);

    public abstract Uri getUri();

    public abstract String getName();

    public abstract String getType();

    public DocumentFile getParentFile() {
        return mParent;
    }

    public abstract boolean isDirectory();

    public abstract boolean isFile();

    public abstract boolean canRead();

    public abstract boolean canWrite();

    public abstract boolean delete();

    public abstract boolean exists();

    public abstract DocumentFile[] listFiles();

    public DocumentFile findFile(String displayName) {
        for (DocumentFile doc : listFiles()) {
            if (displayName.equals(doc.getName())) {
                return doc;
            }
        }
        return null;
    }

    public abstract boolean renameTo(String displayName);
    
}
