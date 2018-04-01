package com.hiroshi.cimoc.core;

import android.content.res.Resources;

import com.hiroshi.cimoc.R;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by FEILONG on 2018/4/1.
 */

public class Mongo {
    private MongoClientURI mongoUrl;
    private MongoClient mongoClient;
    private MongoDatabase mongoBase;
    private MongoCollection<Document> comicColl;

    public Mongo(){
        mongoUrl = new MongoClientURI("mongodb://comic:ba93Z5qUerhSJE3q@ds014118.mlab.com:14118/comic");
        try{
            List<MongoCredential> credentials = new ArrayList<MongoCredential>();
            credentials.add(MongoCredential.createCredential("comic","cmoic","ba93Z5qUerhSJE3q".toCharArray()));
            mongoClient = new MongoClient(new ServerAddress("ds014118.mlab.com",14118),credentials);
            Document s = mongoClient.listDatabases().first();
            mongoBase = mongoClient.getDatabase("comic");
            comicColl = mongoBase.getCollection("comic-base");
            Document t = new Document("lid","3")
                .append("mid","2323");
            comicColl.insertOne(t);
            Document f = comicColl.find().first();
            int a = 1;//debug
        } catch (Exception ex){
            int a = 1;//debug
        }
    }
}
