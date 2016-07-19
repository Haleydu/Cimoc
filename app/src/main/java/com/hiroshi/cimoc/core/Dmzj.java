package com.hiroshi.cimoc.core;

import com.hiroshi.cimoc.core.base.Manga;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.utils.Decryption;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Hiroshi on 2016/7/8.
 */
public class Dmzj extends Manga {

    String host = "http://manhua.dmzj.com";

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
                    if (object.getInt("zone_tag_id") == 2308) {
                        continue;
                    }
                    String title = object.getString("name");
                    long time = object.getLong("last_updatetime") * 1000;
                    String update = new SimpleDateFormat("yyyy-MM-dd").format(new Date(time));
                    String image = object.getString("cover");
                    String author = object.getString("authors");
                    String status = object.getInt("status_tag_id") == 2310 ? "完结" : "连载中";
                    String path = object.getString("comic_url").replace("http://manhua.dmzj.com", "");
                    list.add(new Comic(Kami.SOURCE_DMZJ, path ,image, title, author, null, status, update));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    @Override
    protected String parseIntoUrl(String path) {
        return host + path;
    }

    @Override
    protected Comic parseInto(String html, List<Chapter> list) {
        Document doc = Jsoup.parse(html);
        Elements items = doc.select(".cartoon_online_border > ul > li > a");
        for (Element item : items) {
            String c_title = item.text();
            String c_path = item.attr("href");
            list.add(new Chapter(c_title, c_path));
        }
        Collections.reverse(list);
        String intro = doc.select(".middleright_mr > .line_height_content").first().text().trim();
        Element detail = doc.getElementsByClass("week_mend_back").first();
        String title = detail.select(".anim_intro_ptext > a > img").first().attr("alt");
        String image = detail.select(".anim_intro_ptext > a > img").first().attr("src");
        String status = detail.select(".anim-main_list > table > tbody > tr:eq(4) > td > a").first().text();
        String update = "-";
        String author = detail.select(".anim-main_list > table > tbody > tr:eq(2) > td > a").first().text();
        Element node = detail.select(".anim-main_list > table > tbody > tr:eq(8) > td > span").first();
        if (node != null) {
            update = node.text();
        }
        return new Comic(0, null, image, title, author, intro, status, update);
    }

    @Override
    protected String parseBrowseUrl(String path) {
        return host + path;
    }

    @Override
    protected String[] parseBrowse(String html) {
        Pattern pattern = Pattern.compile("eval(.*?)\\s+;");
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            try {
                String result = Decryption.evalDecrypt(matcher.group(1));
                String jsonString = result.substring(17, result.length() - 2);

                JSONArray array = new JSONArray(jsonString);
                String[] images = new String[array.length()];
                for (int i = 0; i != images.length; ++i) {
                    images[i] = "http://images.dmzj.com/" + array.getString(i);
                }
                return images;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
