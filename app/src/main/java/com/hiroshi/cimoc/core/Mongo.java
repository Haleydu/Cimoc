package com.hiroshi.cimoc.core;

import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.utils.StringUtils;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

//    private Document queryStr;
//    private Document setStr;

    public Mongo(){
//        mongoUrl = new MongoClientURI("mongodb://comic:ba93Z5qUerhSJE3q@ds014118.mlab.com:14118/comic");
        mongoUrl = new MongoClientURI("mongodb://173.82.232.184:27017/comic");
        mongoClient = new MongoClient(mongoUrl);
        mongoBase = mongoClient.getDatabase("comic");
        comicBaseColl = mongoBase.getCollection("comic-base");
        comicChaColl = mongoBase.getCollection("comic-chapter");
    }

    private List<Document> genChapterListFromDocumentList(List<Chapter> list){
        List<Document> chapterList = new ArrayList<>();
        ListIterator<Chapter> iterator=list.listIterator(list.size());
        while(iterator.hasPrevious()){
            Chapter c = iterator.previous();
            chapterList.add(
                new Document("title", c.getTitle())
                    .append("cid",c.getPath())
            );
        }
        return chapterList;
    }

    private Document QueryBaseDoc(int source,String mid){
        Document queryStr = new Document("lid",source)
            .append("mid",mid);
        return comicBaseColl.find(queryStr).first();
    }

    private String getDate(){
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
//        int hour = c.get(Calendar.HOUR_OF_DAY);
//        int minute = c.get(Calendar.MINUTE);
//        int second = c.get(Calendar.SECOND);
//        int week_month = c.get(Calendar.WEEK_OF_MONTH);
//        int week_year = c.get(Calendar.WEEK_OF_YEAR);
//        int week_day = c.get(Calendar.DAY_OF_WEEK);
//        int week_day_month = c.get(Calendar.DAY_OF_WEEK_IN_MONTH);
        return StringUtils.format("%d%d%d",year,month,day);
    }

    private void InsertBaseByDoc(Comic comic, List<Chapter> list){
        Document setStr = new Document("lid",comic.getSource())
            .append("mid",comic.getCid())
            .append("lastcid",list.get(0).getPath())
//            .append("lastdate",getDate())
            .append("lastdate",new Date())
            .append("path",comic.getUrl())
            .append("title",comic.getTitle())
            .append("intro",comic.getIntro())
            .append("author",comic.getAuthor())
            .append("chapter", genChapterListFromDocumentList(list));
        comicBaseColl.insertOne(setStr);
    }

    private void UpdateOneBase(int source,String mid,List<Chapter> list){
        Document queryStr = new Document("lid",source)
            .append("mid",mid);
        Document setStr = new Document("lastcid",list.get(0).getPath())
            .append("chapter", genChapterListFromDocumentList(list));
        comicBaseColl.updateOne(queryStr, new Document("$set",setStr));
    }

    public void UpdateComicBase(Comic comic, List<Chapter> list){
        try{
            //search
            Document d = QueryBaseDoc(comic.getSource(),comic.getCid());
            //if not exist,create it
            if(d == null){
                InsertBaseByDoc(comic,list);
            }else
                //if update,refersh it
                if(!d.get("lastcid").equals(list.get(0).getPath())) {
                    UpdateOneBase(comic.getSource(),comic.getCid(),list);
                }
        }catch (Exception ex){
            //connect to databases error
        }
    }

    private List<Document> genDocumentListFromImageUrlList(List<ImageUrl> list){
        List<Document> picList = new ArrayList<>();
        for (ImageUrl imageUrl : list) {
            picList.add(new Document("src",imageUrl.getUrl()));
        }
        return picList;
    }

    private List<ImageUrl> genImageUrlListFromDocumentList(List<Document> list) {
        List<ImageUrl> picList = new ArrayList<>();
        int i = 0;
        for (Document document : list) {
            picList.add(new ImageUrl(++i,(String)document.get("src"),false));
        }
        return picList;
    }

    public void InsertComicChapter(final Comic comic,
                                   final String cid,
                                   List<ImageUrl> list){
        InsertComicChapter(comic.getSource(),comic.getCid(),cid,list);
    }

    public void InsertComicChapter(final int source,
                                   final String mid,
                                   final String cid,
                                   List<ImageUrl> list){
        try{
            //search
            Document d = QueryCollDoc(source,mid,cid);
            //if not exist,create it
            if(d == null){
                Document setStr = new Document("lid",source)
                    .append("mid",mid)
                    .append("cid",cid)
                    .append("pic", genDocumentListFromImageUrlList(list));
                comicChaColl.insertOne(setStr);
            }
        }catch (Exception ex){
            //connect to databases error
        }
    }

    public List<ImageUrl> QueryComicChapter(final Comic comic,
                                   final String cid){
        return QueryComicChapter(comic.getSource(),comic.getCid(),cid);
    }

    public List<ImageUrl> QueryComicChapter(final int source,
                             final String mid,
                             final String cid) {
        try {
            Document d = QueryCollDoc(source,mid,cid);
            if(d==null){
                return new ArrayList<>();
            }else{
                List<Document> listdoc = (List<Document>)d.get("pic");
                return genImageUrlListFromDocumentList(listdoc);
            }
        } catch (Exception ex) {
            //connect to databases error
            return new ArrayList<>();
        }
    }

    private Document QueryCollDoc(int source,String mid,String cid){
        Document queryStr = new Document("lid",source)
            .append("mid",mid)
            .append("cid",cid);
        return comicChaColl.find(queryStr).first();
    }
}
