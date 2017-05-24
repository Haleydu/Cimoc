package com.hiroshi.cimoc.saf;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Hiroshi on 2017/3/24.
 */

public abstract class DocumentFile {

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

    public static DocumentFile fromSubTreeUri(Context context, Uri uri) {
        if (Build.VERSION.SDK_INT >= 21) {
            /*
             * https://stackoverflow.com/questions/27759915/bug-when-listing-files-with-android-storage-access-framework-on-lollipop
             * 如果使用 buildDocumentUriUsingTree 会获取到授权的那个 DocumentFile
             */
            return new TreeDocumentFile(null, context, uri);
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

    public abstract long length();

    public abstract boolean canRead();

    public abstract boolean canWrite();

    public abstract boolean delete();

    public abstract boolean exists();

    public abstract InputStream openInputStream() throws FileNotFoundException;

    public List<DocumentFile> listFiles(DocumentFileFilter filter) {
        return listFiles(filter, null);
    }

    public DocumentFile[] listFiles(Comparator<? super DocumentFile> comp) {
        DocumentFile[] files = listFiles();
        Arrays.sort(files,comp);
        return files;
    }

    public abstract List<DocumentFile> listFiles(DocumentFileFilter filter, Comparator<? super DocumentFile> comp);

    public abstract DocumentFile[] listFiles();

    public abstract void refresh();

    public abstract DocumentFile findFile(String displayName);

    public abstract boolean renameTo(String displayName);

    public interface DocumentFileFilter {
        boolean call(DocumentFile file);
    }
    
}
