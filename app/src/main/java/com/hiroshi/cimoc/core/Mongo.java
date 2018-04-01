package com.hiroshi.cimoc.core;

import android.content.res.Resources;
import android.support.annotation.NonNull;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.parser.Parser;
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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by FEILONG on 2018/4/1.
 */

public class Mongo {
    private MongoClientURI mongoUrl;
    private MongoClient mongoClient;
    private MongoDatabase mongoBase;
    private MongoCollection<Document> comicBaseColl;
    private MongoCollection<Document> comicChaColl;

    private Document queryStr;
    private Document setStr;

    public Mongo(){
//        mongoUrl = new MongoClientURI("mongodb://comic:ba93Z5qUerhSJE3q@ds014118.mlab.com:14118/comic");
        mongoUrl = new MongoClientURI("mongodb://173.82.232.184:27017/comic");
        mongoClient = new MongoClient(mongoUrl);
        mongoBase = mongoClient.getDatabase("comic");
        comicBaseColl = mongoBase.getCollection("comic-base");
        comicChaColl = mongoBase.getCollection("comic-chapter");
    }

    private List<Document> genChapterList(List<Chapter> list){
        List<Document> chapterList = new ArrayList<>();
        Collections.reverse(list);
        for (Chapter c : list){
            chapterList.add(
                new Document("title", c.getTitle())
                    .append("cid",c.getPath())
            );
        }
        return chapterList;
    }

    public void UpdateComicBase(Comic comic, List<Chapter> list){
        try{
            //search
            queryStr = new Document("lid",comic.getSource())
                            .append("mid",comic.getCid());
            Document d = comicBaseColl.find(queryStr).first();
            //if not exist,create it
            if(d == null){
                setStr = new Document("lid",comic.getSource())
                    .append("mid",comic.getCid())
                    .append("lastcid",list.get(0).getPath())
                    .append("path",comic.getUrl())
                    .append("title",comic.getTitle())
                    .append("intro",comic.getIntro())
                    .append("author",comic.getAuthor())
                    .append("chapter",genChapterList(list));
                comicBaseColl.insertOne(setStr);
            }else
                //if update,refersh it
                if(!d.get("lastcid").equals(list.get(0).getPath())) {
                    setStr = new Document("lastcid",list.get(0).getPath())
                                .append("chapter",genChapterList(list));
                    comicBaseColl.updateOne(queryStr, new Document("$set",setStr));
                }
        }catch (Exception ex){
            //connect to databases error
        }
    }

    private List<Document> genImageList(List<ImageUrl> list){
        List<Document> picList = new ArrayList<>();
        for (ImageUrl imageUrl : list) {
            picList.add(new Document("src",imageUrl.getUrl()));
        }
        return picList;
    }

    public void InsertComicChapter(final Comic comic,
                                   final String cid,
                                   List<ImageUrl> list){
        try{
            //search
            queryStr = new Document("lid",comic.getSource())
                .append("mid",comic.getCid())
                .append("cid",cid);
            Document d = comicChaColl.find(queryStr).first();
            //if not exist,create it
            if(d == null){
                setStr = new Document("lid",comic.getSource())
                    .append("mid",comic.getCid())
                    .append("cid",cid)
                    .append("pic",genImageList(list));
                comicChaColl.insertOne(setStr);
            }
        }catch (Exception ex){
            //connect to databases error
        }
    }
}
