package com.hiroshi.cimoc.utils;

import android.os.Environment;

import com.hiroshi.cimoc.model.Comic;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Hiroshi on 2016/7/22.
 */
public class BackupUtils {

    public static String dirPath = Environment.getExternalStorageDirectory() + File.separator + "Cimoc" + File.separator + "backup";

    public static boolean saveComic(List<Comic> list) {
        try {
            JSONArray array = new JSONArray();
            for (Comic comic : list) {
                JSONObject object = new JSONObject();
                object.put("source", comic.getSource());
                object.put("cid", comic.getCid());
                object.put("title", comic.getTitle());
                object.put("cover", comic.getCover());
                object.put("update", comic.getUpdate());
                array.put(object);
            }
            if (FileUtils.mkDirsIfNotExist(dirPath)) {
                String name = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".cimoc";
                if (FileUtils.writeStringToFile(dirPath, name, array.toString())) {
                    ExLog.d("Backup", "save to " + name);
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String[] showBackupFiles() {
        return FileUtils.listFilesHaveSuffix(dirPath, "cimoc");
    }

    public static List<Comic> restoreComic(String name) {
        List<Comic> list = new LinkedList<>();
        try {
            String jsonString = FileUtils.readSingleLineFromFile(dirPath, name);
            JSONArray array = new JSONArray(jsonString);
            for (int i = 0; i != array.length(); ++i) {
                JSONObject object = array.getJSONObject(i);
                int source = object.getInt("source");
                String cid = object.getString("cid");
                String title = object.getString("title");
                String cover = object.getString("cover");
                String update = object.getString("update");
                list.add(new Comic(null, source, cid, title, cover, update, null, null, null, null));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ExLog.d("BackupUtils", "the number of comic is " + list.size());
        return list;
    }

}
