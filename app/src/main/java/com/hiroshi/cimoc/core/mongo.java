package com.hiroshi.cimoc.core;

import android.content.res.Resources;

import com.hiroshi.cimoc.R;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

/**
 * Created by FEILONG on 2018/4/1.
 */

public class mongo {
    private MongoClientURI mongoUrl = new MongoClientURI(Resources.getSystem().getString(R.string.mongo_str));
    private MongoClient mongoClient;


}
