package com.hiroshi;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class ComicRecordGenerator {

    public static final int version = 1;
    public static final String defaultJavaPackage = "com.hiroshi.db.entity";
    public static final String className = "ComicRecord";
    public static final String defaultJavaPackageDao = "com.hiroshi.db.dao";
    public static final String outDir = "app/src/main/java-gen";

    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(version, defaultJavaPackage);
        schema.setDefaultJavaPackageDao(defaultJavaPackageDao);

        Entity entity = schema.addEntity(className);
        entity.addIdProperty().autoincrement();
        entity.addIntProperty("source").notNull();
        entity.addStringProperty("path").notNull();
        entity.addStringProperty("title").notNull();
        entity.addStringProperty("image").notNull();
        entity.addStringProperty("update").notNull();
        entity.addLongProperty("favorite");
        entity.addLongProperty("history");
        entity.addStringProperty("last_path");
        entity.addIntProperty("last_page");

        new DaoGenerator().generateAll(schema, outDir);
    }

}
