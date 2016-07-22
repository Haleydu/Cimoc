package com.hiroshi.cimoc.utils;

import android.os.Environment;

import com.hiroshi.cimoc.core.ComicManager;
import com.hiroshi.cimoc.model.Comic;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Hiroshi on 2016/7/22.
 */
public class BackupUtils {

    public static String filePath = Environment.getExternalStorageDirectory() + File.separator + "Cimoc" + File.separator + "backup";

    public static boolean saveFavoriteComic() {
        try {
            JSONArray array = new JSONArray();
            for (Comic comic : ComicManager.getInstance().listFavorite()) {
                JSONObject object = new JSONObject();
                object.put("source", comic.getSource());
                object.put("cid", comic.getCid());
                object.put("title", comic.getTitle());
                object.put("cover", comic.getCover());
                object.put("update", comic.getUpdate());
                array.put(object);
            }
            if (isFilePathExist()) {
                String filename = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".cimoc";
                File file = new File(filePath, filename);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
                writer.write(array.toString());
                ExLog.d("Backup", "save to " + filename);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String[] showBackupFiles() {
        if (isFilePathExist()) {
            File dir = new File(filePath);
            return dir.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.endsWith(".cimoc");
                }
            });
        }
        return null;
    }

    public static boolean restoreFavoriteComic(String filename) {
        try {
            File file = new File(filePath, filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String jsonString = reader.readLine();
            JSONArray array = new JSONArray(jsonString);
            ComicManager manager = ComicManager.getInstance();
            for (int i = 0; i != array.length(); ++i) {
                JSONObject object = array.getJSONObject(i);
                int source = object.getInt("source");
                String cid = object.getString("cid");
                if (!manager.isExist(source, cid)) {
                    manager.restore(source, cid, object.getString("title"), object.getString("cover"), object.getString("update"));
                    ExLog.d("Backup", "restore " + source + " " + cid);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean isFilePathExist() {
        boolean result = true;
        File dir = new File(filePath);
        if (!dir.exists()) {
            result = dir.mkdirs();
        }
        return result;
    }

}
