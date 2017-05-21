package com.hiroshi.cimoc.core;

import com.hiroshi.cimoc.misc.Pair;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.Task;
import com.hiroshi.cimoc.saf.DocumentFile;
import com.hiroshi.cimoc.source.Locality;
import com.hiroshi.cimoc.utils.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by Hiroshi on 2017/5/20.
 */

public class Local {

    private static Pattern chapterPattern = null;

    public static Observable<List<Pair<Comic, ArrayList<Task>>>> scan(final DocumentFile root) {
        return Observable.create(new Observable.OnSubscribe<List<Pair<Comic, ArrayList<Task>>>>() {
            @Override
            public void call(Subscriber<? super List<Pair<Comic, ArrayList<Task>>>> subscriber) {
                List<Pair<Comic, ArrayList<Task>>> result = new ArrayList<>();

                int count = countPicture(root.listFiles());
                if (count > 5) {
                    Pair<Comic, ArrayList<Task>> pair = Pair.create(buildComic(root), new ArrayList<Task>());
                    pair.second.add(buildTask(root, count, true));
                    result.add(pair);
                } else {
                    List<DocumentFile> list = new LinkedList<>();
                    list.add(root);

                    while (!list.isEmpty()) {
                        DocumentFile dir = list.get(0);
                        DocumentFile[] files = dir.listFiles();

                        List<Pair<DocumentFile, Integer>> guessChapter = new LinkedList<>();
                        List<Pair<DocumentFile, Integer>> guessComic = new LinkedList<>();
                        List<DocumentFile> guessOther = classify(guessChapter, guessComic, files);

                        if (guessChapter.size() + guessComic.size() > files.length / 2) {   // 章节 或 单章节漫画
                            if (guessChapter.size() > 2 * guessComic.size()) {  // 章节
                                result.add(merge(dir, guessChapter, guessComic));
                                continue;
                            } else {    // 单章节漫画
                                split(guessChapter, result);
                                split(guessComic, result);
                            }
                        }
                        list.addAll(guessOther);
                    }
                }
                subscriber.onNext(result);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io());
    }

    private static int countPicture(DocumentFile[] files) {
        int count = 0;
        int other = 0;
        for (DocumentFile file : files) {
            if (file.isFile() && StringUtils.endWith(file.getName(), "png", "jpg", "jpeg", "bmp")) {
                ++count;
            } else {
                ++other;
            }
            if (other > 5) {
                return 0;
            }
        }
        return count;
    }

    private static List<DocumentFile> classify(List<Pair<DocumentFile, Integer>> chapter,
                                              List<Pair<DocumentFile, Integer>> comic,
                                              DocumentFile[] files) {
        List<DocumentFile> other = new LinkedList<>();
        for (DocumentFile file : files) {
            if (file.isDirectory()) {
                int count = countPicture(files);
                if (count > 5) {
                    if (isNameChapter(file)) {
                        chapter.add(Pair.create(file, count));
                    } else {
                        comic.add(Pair.create(file, count));
                    }
                } else {
                    other.add(file);
                }
            }
        }
        return other;
    }

    private static boolean isNameChapter(DocumentFile file) {   // '第01话 章节标题' 这种判定为假
        if (chapterPattern == null) {
            chapterPattern = Pattern.compile("^第?\\s*[0-9零一二三四五六七八九十]+\\s*[章话卷回]?");
        }
        Matcher matcher = chapterPattern.matcher(file.getName());
        return matcher.find() && ((float) matcher.group().length() / file.getName().length() > 0.8);
    }

    private static Comic buildComic(DocumentFile dir) {
        return new Comic(null, Locality.TYPE, dir.getUri().toString(), dir.getName(), "",
                false, true, null, null, null, null, System.currentTimeMillis(), null, null);
    }

    private static Task buildTask(DocumentFile dir, int count, boolean single) {
        return single ? new Task(null, -1, dir.getUri().toString(), "第一话", count, count) :
                new Task(null, -1, dir.getUri().toString(), dir.getName(), count, count);
    }

    private static Pair<Comic, ArrayList<Task>> merge(DocumentFile dir,
                                                               List<Pair<DocumentFile, Integer>> list1,
                                                               List<Pair<DocumentFile, Integer>> list2) {
        Pair<Comic, ArrayList<Task>> pair = Pair.create(buildComic(dir), new ArrayList<Task>());
        for (Pair<DocumentFile, Integer> pr : list1) {
            pair.second.add(buildTask(pr.first, pr.second, false));
        }
        for (Pair<DocumentFile, Integer> pr : list2) {
            pair.second.add(buildTask(pr.first, pr.second, false));
        }
        return pair;
    }

    private static void split(List<Pair<DocumentFile, Integer>> list,
                                   List<Pair<Comic, ArrayList<Task>>> result) {
        for (Pair<DocumentFile, Integer> pr : list) {
            Pair<Comic, ArrayList<Task>> pair = Pair.create(buildComic(pr.first), new ArrayList<Task>());
            pair.second.add(buildTask(pr.first, pr.second, true));
            result.add(pair);
        }
    }

}
