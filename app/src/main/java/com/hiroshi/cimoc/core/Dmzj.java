package com.hiroshi.cimoc.core;

import com.hiroshi.cimoc.core.base.Manga;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Hiroshi on 2016/7/8.
 */
public class Dmzj extends Manga {

    public Dmzj() {
        super(Kami.SOURCE_DMZJ, "http://m.dmzj.com");
    }

    @Override
    protected String parseSearchUrl(String keyword, int page) {
        if (page == 1) {
            return "http://s.acg.178.com/comicsum/search.php?s=" + keyword;
        }
        return null;
    }

    @Override
    protected List<Comic> parseSearch(String html) {
        Pattern pattern = Pattern.compile("g_search_data = (.*);");
        Matcher matcher = pattern.matcher(html);
        List<Comic> list = new LinkedList<>();
        if (matcher.find()) {
            try {
                JSONArray array = new JSONArray(matcher.group(1));
                for (int i = 0; i != array.length(); ++i) {
                    JSONObject object = array.getJSONObject(i);
                    String cid = object.getString("id");
                    String title = object.getString("name");
                    String image = object.getString("cover");
                    long time = object.getLong("last_updatetime") * 1000;
                    String update = new SimpleDateFormat("yyyy-MM-dd").format(new Date(time));
                    String author = object.getString("authors");
                    boolean status = object.getInt("status_tag_id") == 2310;
                    list.add(build(cid, title, image, update, author, null, status));
                }
                return list;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected String parseIntoUrl(String cid) {
        return host + "/info/" + cid + ".html";
    }

    @Override
    protected List<Chapter> parseInto(String html, Comic comic) {
        if (html.contains("此漫画暂不提供观看")) {
            return null;
        }

        Pattern pattern = Pattern.compile("\"data\":(\\[.*?\\])");
        Matcher matcher = pattern.matcher(html);
        List<Chapter> list = new LinkedList<>();
        if (matcher.find()) {
            try {
                JSONArray array = new JSONArray(matcher.group(1));
                for (int i = 0; i != array.length(); ++i) {
                    JSONObject object = array.getJSONObject(i);
                    String c_title = object.getString("chapter_name");
                    String c_path = object.getString("id");
                    list.add(new Chapter(c_title, c_path));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Document doc = Jsoup.parse(html);
        String intro = doc.select(".txtDesc").first().text().replace("介绍:", "");
        Element detail = doc.getElementsByClass("Introduct_Sub").first();
        String title = detail.select("#Cover > img").first().attr("title");
        String cover = detail.select("#Cover > img").first().attr("src");
        String author = detail.select(".sub_r > p:eq(0) > a").first().text();
        boolean status = "已完结".equals(detail.select(".sub_r > p:eq(2) > a:eq(3)").first().text());
        String update = detail.select(".sub_r > p:eq(3) > .date").first().text().split(" ")[0];

        comic.setIntro(intro);
        comic.setTitle(title);
        comic.setCover(cover);
        comic.setAuthor(author);
        comic.setStatus(status);
        comic.setUpdate(update);

        return list;
    }

    @Override
    protected String parseBrowseUrl(String cid, String path) {
        return host + "/view/" + cid + "/" + path + ".html";
    }

    @Override
    protected String[] parseBrowse(String html) {
        Pattern pattern = Pattern.compile("\"page_url\":(\\[.*?\\])");
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            try {
                JSONArray array = new JSONArray(matcher.group(1));
                String[] images = new String[array.length()];
                for (int i = 0; i != images.length; ++i) {
                    images[i] = array.getString(i);
                }
                return images;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
