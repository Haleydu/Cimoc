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
                object.put("s", comic.getSource());
                object.put("i", comic.getCid());
                object.put("t", comic.getTitle());
                object.put("c", comic.getCover());
                object.put("u", comic.getUpdate());
                object.put("l", comic.getLast());
                object.put("p", comic.getPage());
                array.put(object);
            }
            if (FileUtils.mkDirsIfNotExist(dirPath)) {
                String name = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".cimoc";
                if (FileUtils.writeStringToFile(dirPath, name, array.toString())) {
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
                int source = object.getInt("s");
                String cid = object.getString("i");
                String title = object.getString("t");
                String cover = object.getString("c");
                String update = object.getString("u");
                String last = object.has("l") ? object.getString("l") : null;
                Integer page = object.has("p") ? object.getInt("p") : null;
                list.add(new Comic(null, source, cid, title, cover, update, null, null, last, page));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

}
