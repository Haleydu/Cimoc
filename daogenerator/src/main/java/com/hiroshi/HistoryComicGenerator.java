package com.hiroshi;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

/**
 * Created by Hiroshi on 2016/7/5.
 */
public class HistoryComicGenerator {

    public static final int version = 1;
    public static final String defaultJavaPackage = "com.hiroshi.db.entity";
    public static final String className = "HistoryComic";
    public static final String defaultJavaPackageDao = "com.hiroshi.db.dao";
    public static final String outDir = "app/src/main/java-gen";

    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(version, defaultJavaPackage);
        schema.setDefaultJavaPackageDao(defaultJavaPackageDao);

        Entity entity = schema.addEntity(className);
        entity.addIdProperty();
        entity.addStringProperty("title");
        entity.addStringProperty("image");
        entity.addIntProperty("source");
        entity.addStringProperty("path");
        entity.addLongProperty("revise");

        new DaoGenerator().generateAll(schema, outDir);
    }

}
