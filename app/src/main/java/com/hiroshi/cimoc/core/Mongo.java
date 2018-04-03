package com.hiroshi.cimoc.core;

import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;

import java.util.ArrayList;
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

    private int hourLimit = 12;//todo: add to setting

    public Mongo() {
        //mlab 连接不上，什么鬼？？
//        mongoUrl = new MongoClientURI("mongodb://android:android@ds014118.mlab.com:14118/comic?authSource=comic");
        //Mongo clould 咕咕咕...
//        mongoUrl = new MongoClientURI("mongodb+srv://feilong:8ChSLt3Rnuf6nhpHq9@cimoc-agcb8.mongodb.net/test");
        //自己搭的，临时用下，不设密码了
        mongoUrl = new MongoClientURI("mongodb://173.82.232.184:27017/comic");
        mongoClient = new MongoClient(mongoUrl);
        mongoBase = mongoClient.getDatabase(mongoUrl.getDatabase());
        comicBaseColl = mongoBase.getCollection("comic-base");
        comicChaColl = mongoBase.getCollection("comic-chapter");
    }

    private List<Document> genDocumentListFromChapterList(List<Chapter> list) {
        List<Document> chapterList = new ArrayList<>();
        ListIterator<Chapter> iterator = list.listIterator(list.size());
        while (iterator.hasPrevious()) {
            Chapter c = iterator.previous();
            chapterList.add(
                new Document("title", c.getTitle())
                    .append("cid", c.getPath())
            );
        }
        return chapterList;
    }

    private List<Chapter> genChapterListFromDocumentList(List<Document> list) {
        List<Chapter> chapterList = new ArrayList<>();
        ListIterator<Document> iterator = list.listIterator(list.size());
        while (iterator.hasPrevious()) {
            Document c = iterator.previous();
            chapterList.add(
                new Chapter(c.getString("title"), c.getString("cid"))
            );
        }
        return chapterList;
    }

    private Document QueryBaseDoc(int source, String mid) {
        Document queryStr = new Document("lid", source)
            .append("mid", mid);
        Document resault = comicBaseColl.find(queryStr).first();
        if (resault != null) {
            return resault;
        } else {
            return new Document();
        }
    }

    private void InsertBaseByDoc(Comic comic, List<Chapter> list) {
        Document setStr = new Document("lid", comic.getSource())
            .append("mid", comic.getCid())
            .append("lastcid", list.get(0).getPath())
//            .append("lastdate",getDate())
            .append("lastdate", new Date())
            .append("path", comic.getUrl())
            .append("title", comic.getTitle())
            .append("intro", comic.getIntro())
            .append("author", comic.getAuthor())
            .append("chapter", genDocumentListFromChapterList(list));
        try {
            comicBaseColl.insertOne(setStr);
        } catch (Exception ex) {
            //
        }
    }

    private void UpdateOneBase(int source, String mid, List<Chapter> list) {
        Document queryStr = new Document("lid", source)
            .append("mid", mid);
        Document setStr = new Document("lastcid", list.get(0).getPath())
            .append("lastdate", new Date())
            .append("chapter", genDocumentListFromChapterList(list));
        try {
            comicBaseColl.updateOne(queryStr, new Document("$set", setStr));
        } catch (Exception ex) {
            //
        }
    }

    public void UpdateComicBase(Comic comic, List<Chapter> list) {
        try {
            //search
            Document d = QueryBaseDoc(comic.getSource(), comic.getCid());

            //if not exist,create it
            if (d.isEmpty()) {
                InsertBaseByDoc(comic, list);
            } else
                //if update,refersh it
                if (!d.get("lastcid").equals(list.get(0).getPath()) || getDateDiffHour(d.getDate("lastdate")) >= hourLimit) {
                    UpdateOneBase(comic.getSource(), comic.getCid(), list);
                }
        } catch (Exception ex) {
            //connect to databases error
        }
    }

    private int getDateDiffHour(Date fromDate) {
        return getDateDiffHour(fromDate, new Date());
    }

    private int getDateDiffHour(Date fromDate, Date toDate) {
        if (fromDate == null || toDate == null) {
            return hourLimit + 1;
        }
        long from = fromDate.getTime();
        long to = toDate.getTime();
        return (int) ((to - from) / (60 * 60 * 1000));
    }

    public List<Chapter> QueryComicBase(Comic comic) {
        List<Chapter> list = new ArrayList<>();
        Document d = QueryBaseDoc(comic.getSource(), comic.getCid());

        if (!d.isEmpty()) {
            //数据库数据只在一定时间内有效
            if (getDateDiffHour(d.getDate("lastdate")) < hourLimit) {
                comic.setTitle(d.getString("title"));
                comic.setUrl(d.getString("path"));
                comic.setIntro(d.getString("intro"));
                comic.setAuthor(d.getString("author"));

                return genChapterListFromDocumentList((List<Document>) d.get("chapter"));
            }
        }

        return list;
    }

    private List<Document> genDocumentListFromImageUrlList(List<ImageUrl> list) {
        List<Document> picList = new ArrayList<>();
        for (ImageUrl imageUrl : list) {
            picList.add(new Document("src", imageUrl.getUrl()));
        }
        return picList;
    }

    private List<ImageUrl> genImageUrlListFromDocumentList(List<Document> list) {
        List<ImageUrl> picList = new ArrayList<>();
        int i = 0;
        for (Document document : list) {
            picList.add(new ImageUrl(++i, (String) document.get("src"), false));
        }
        return picList;
    }

    public void InsertComicChapter(final Comic comic,
                                   final String cid,
                                   List<ImageUrl> list) {
        InsertComicChapter(comic.getSource(), comic.getCid(), cid, list);
    }

    public void InsertComicChapter(final int source,
                                   final String mid,
                                   final String cid,
                                   List<ImageUrl> list) {
        try {
            //search
            Document d = QueryCollDoc(source, mid, cid);
            //if not exist,create it
            if (d.isEmpty()) {
                Document setStr = new Document("lid", source)
                    .append("mid", mid)
                    .append("cid", cid)
                    .append("pic", genDocumentListFromImageUrlList(list));
                comicChaColl.insertOne(setStr);
            }
        } catch (Exception ex) {
            //connect to databases error
        }
    }

    public List<ImageUrl> QueryComicChapter(final Comic comic,
                                            final String cid) {
        return QueryComicChapter(comic.getSource(), comic.getCid(), cid);
    }

    public List<ImageUrl> QueryComicChapter(final int source,
                                            final String mid,
                                            final String cid) {
        try {
            Document d = QueryCollDoc(source, mid, cid);
            if (d.isEmpty()) {
                return new ArrayList<>();
            } else {
                List<Document> listdoc = (List<Document>) d.get("pic");
                return genImageUrlListFromDocumentList(listdoc);
            }
        } catch (Exception ex) {
            //connect to databases error
            return new ArrayList<>();
        }
    }

    private Document QueryCollDoc(int source, String mid, String cid) {
        Document queryStr = new Document("lid", source)
            .append("mid", mid)
            .append("cid", cid);
        Document resault = comicChaColl.find(queryStr).first();
        if (resault != null) {
            return resault;
        } else {
            return new Document();
        }
    }
}
