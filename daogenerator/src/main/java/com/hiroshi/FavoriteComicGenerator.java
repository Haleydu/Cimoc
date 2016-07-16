package com.hiroshi;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class FavoriteComicGenerator {

    public static final int version = 1;
    public static final String defaultJavaPackage = "com.hiroshi.db.entity";
    public static final String className = "FavoriteComic";
    public static final String defaultJavaPackageDao = "com.hiroshi.db.dao";
    public static final String outDir = "app/src/main/java-gen";

    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(version, defaultJavaPackage);
        schema.setDefaultJavaPackageDao(defaultJavaPackageDao);

        Entity entity = schema.addEntity(className);
        entity.addIdProperty().autoincrement();
        entity.addStringProperty("title");
        entity.addStringProperty("image");
        entity.addIntProperty("source");
        entity.addStringProperty("update");
        entity.addStringProperty("path");
        entity.addLongProperty("create");
        entity.addStringProperty("last_path");
        entity.addIntProperty("last_page");

        new DaoGenerator().generateAll(schema, outDir);
    }

}
