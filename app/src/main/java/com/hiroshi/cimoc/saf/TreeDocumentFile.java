package com.hiroshi.cimoc.saf;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.support.annotation.RequiresApi;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static okhttp3.internal.Util.closeQuietly;

/**
 * Created by Hiroshi on 2017/3/24.
 */

@RequiresApi(21)
@TargetApi(21)
class TreeDocumentFile extends DocumentFile {

    private Context mContext;
    private Uri mUri;
    private String mDisplayName;
    private String mMimeType;
    private Map<String, DocumentFile> mSubFiles;

    private TreeDocumentFile(DocumentFile parent, Context context, Uri uri, String displayName, String mimeType) {
        super(parent);
        mContext = context;
        mUri = uri;
        mDisplayName = displayName;
        mMimeType = mimeType;
    }

    TreeDocumentFile(DocumentFile parent, Context context, Uri uri) {
        super(parent);
        mContext = context;
        mUri = uri;
        query();
    }

    private void list() {
        mSubFiles = new HashMap<>();

        ContentResolver resolver = mContext.getContentResolver();
        Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(mUri, DocumentsContract.getDocumentId(mUri));

        Cursor c = null;
        try {
            c = resolver.query(childrenUri, new String[] { DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_MIME_TYPE }, null, null, null);
            while (c.moveToNext()) {
                Uri documentUri = DocumentsContract.buildDocumentUriUsingTree(mUri, c.getString(0));
                String displayName = c.getString(1);
                mSubFiles.put(displayName, new TreeDocumentFile(this, mContext, documentUri, displayName, c.getString(2)));
            }
        } finally {
            closeQuietly(c);
        }
    }

    private void query() {
        Cursor c = null;
        try {
            c = mContext.getContentResolver().query(mUri, new String[] {DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_MIME_TYPE }, null, null, null);
            if (c != null && c.moveToNext()) {
                mDisplayName = c.getString(0);
                mMimeType = c.getString(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuietly(c);
        }
    }

    @Override
    public DocumentFile createFile(String displayName) {
        if (!isDirectory()) {
            return null;
        }

        if (mSubFiles == null) {
            list();
        }

        DocumentFile doc = findFile(displayName);
        if (doc != null) {
            return null;
        }

        Uri result = DocumentsContract.createDocument(mContext.getContentResolver(), mUri, null, displayName);
        if (result != null) {
            doc = new TreeDocumentFile(this, mContext, result, displayName, null);
            mSubFiles.put(displayName, doc);
        }

        return doc;
    }

    @Override
    public DocumentFile createDirectory(String displayName) {
        if (!isDirectory()) {
            return null;
        }

        if (mSubFiles == null) {
            list();
        }

        DocumentFile doc = findFile(displayName);
        if (doc != null) {
            return null;
        }

        Uri result = DocumentsContract.createDocument(mContext.getContentResolver(), mUri,
                DocumentsContract.Document.MIME_TYPE_DIR, displayName);
        if (result != null) {
            doc = new TreeDocumentFile(this, mContext, result, displayName, DocumentsContract.Document.MIME_TYPE_DIR);
            mSubFiles.put(displayName, doc);
        }

        return doc;
    }

    @Override
    public Uri getUri() {
        return mUri;
    }

    @Override
    public String getName() {
        return mDisplayName;
    }

    @Override
    public String getType() {
        return mMimeType;
    }

    @Override
    public boolean isDirectory() {
        return DocumentsContract.Document.MIME_TYPE_DIR.equals(mMimeType);
    }

    @Override
    public boolean isFile() {
        return !isDirectory();
    }

    @Override
    public boolean canRead() {
        return mContext.checkCallingOrSelfUriPermission(mUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public boolean canWrite() {
        return mContext.checkCallingOrSelfUriPermission(mUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public boolean delete() {
        return DocumentsContract.deleteDocument(mContext.getContentResolver(), mUri);
    }

    @Override
    public boolean exists() {
        final ContentResolver resolver = mContext.getContentResolver();

        Cursor c = null;
        try {
            c = resolver.query(mUri, new String[] { DocumentsContract.Document.COLUMN_DOCUMENT_ID }, null, null, null);
            return c != null && c.getCount() > 0;
        } finally {
            closeQuietly(c);
        }
    }

    @Override
    public DocumentFile[] listFiles() {
        if (!isDirectory()) {
            return new DocumentFile[0];
        }

        if (mSubFiles == null) {
            list();
        }

        int size = mSubFiles.size();
        Iterator<Map.Entry<String, DocumentFile>> iterator = mSubFiles.entrySet().iterator();
        DocumentFile[] result = new DocumentFile[size];
        for (int i = 0; i != size; ++i) {
            result[i] = iterator.next().getValue();
        }

        return result;
    }

    @Override
    public DocumentFile findFile(String displayName) {
        if (!isDirectory()) {
            return null;
        }

        if (mSubFiles == null) {
            list();
        }

        return mSubFiles.get(displayName);
    }

    @Override
    public boolean renameTo(String displayName) {
        final Uri result = DocumentsContract.renameDocument(mContext.getContentResolver(), mUri, displayName);
        if (result != null) {
            mUri = result;
            return true;
        } else {
            return false;
        }
    }

}
