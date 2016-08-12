package com.hiroshi.cimoc.core.source;

import com.hiroshi.cimoc.core.source.base.Manga;
import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.utils.MachiSoup;
import com.hiroshi.cimoc.utils.MachiSoup.Node;

import java.util.LinkedList;
import java.util.List;

import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/8/9.
 */
public class Wnacg extends Manga {

    public Wnacg() {
        super(SourceManager.SOURCE_WNACG, "http://www.wnacg.com");
    }

    @Override
    protected Request buildSearchRequest(String keyword, int page) {
        String url = host + "/albums-index-page-" + page + "-sname-" + keyword + ".html";
        return new Request.Builder().url(url).header("Cookie", "ipb_member_id=2145630; ipb_pass_hash=f883b5a9dd10234c9323957b96efbd8e").build();
    }

    @Override
    protected List<Comic> parseSearch(String html, int page) {
        Node body = MachiSoup.body(html);
        List<Comic> list = new LinkedList<>();
        for (MachiSoup.Node node : body.list("#bodywrap > div.grid > div > ul > li")) {
            String cid = node.attr("div.info > div.title > a", "href", "-|\\.", 3);
            String title = node.text("div.info > div.title > a");
            String cover = node.attr("div.pic_box > a > img", "src");
            String update = node.text("div.info > div.info_col", 0, 10);
            String author = node.text("td:eq(3) > div > a");
            list.add(new Comic(source, cid, title, cover, update, author, true));
        }
        return list;
    }

    @Override
    protected Request buildIntoRequest(String cid) {
        String url = host + "/g/" + cid;
        return new Request.Builder().url(url).header("Cookie", "s=485adc1edc9d59a6a7d62cd15d1a7a213b333f5cb092bcc2d30c476419fbcb5555f19e27c606df9cdc56737cb920fe0855e9671c7109069401d8ede5b718f522; ipb_member_id=2145630; ipb_pass_hash=f883b5a9dd10234c9323957b96efbd8e; uconfig=ts_l;").build();
    }

    @Override
    protected List<Chapter> parseInto(String html, Comic comic) {
        List<Chapter> list = new LinkedList<>();
        MachiSoup.Node body = MachiSoup.body(html);
        String length = body.text("#gdd > table > tbody > tr:eq(5) > td:eq(1)", " ", 0);
        int size = Integer.parseInt(length) % 20 == 0 ? Integer.parseInt(length) / 20 : Integer.parseInt(length) / 20 + 1;
        for (int i = 0; i != size; ++i) {
            list.add(0, new Chapter("Ch" + i, String.valueOf(i)));
        }

        String update = body.text("#gdd > table > tbody > tr:eq(0) > td:eq(1)", 0, 10);
        String title = body.text("#gn");
        String intro = body.text("#gj");
        String author = body.text("#taglist > table > tbody > tr > td:eq(1) > div > a[id^=ta_artist]");
        String cover = body.attr("#gd1 > img", "src");
        comic.setInfo(title, cover, update, intro, author, true);

        return list;
    }

    @Override
    protected Request buildBrowseRequest(String cid, String path) {
        String url = host + "/g/" + cid + "?p=" + path;
        return new Request.Builder().url(url).header("Cookie", "s=485adc1edc9d59a6a7d62cd15d1a7a213b333f5cb092bcc2d30c476419fbcb5555f19e27c606df9cdc56737cb920fe0855e9671c7109069401d8ede5b718f522; ipb_member_id=2145630; ipb_pass_hash=f883b5a9dd10234c9323957b96efbd8e; uconfig=ts_l").build();
    }

    @Override
    protected String[] parseBrowse(String html) {
        MachiSoup.Node body = MachiSoup.body(html);
        List<MachiSoup.Node> list = body.list("#gdt > div > a");
        String[] array = new String[list.size()];
        for (int i = 0; i != array.length; ++i) {
            String url = list.get(i).attr("href");
            Request request = new Request.Builder().url(url).header("Cookie", "ipb_member_id=2145630; ipb_pass_hash=f883b5a9dd10234c9323957b96efbd8e").build();
            String result = execute(request);
            if (result != null) {
                MachiSoup.Node node = MachiSoup.body(result);
                array[i] = node.attr("#img", "src");
            } else {
                array[i] = null;
            }
        }
        return array;
    }

    @Override
    protected Request buildCheckRequest(String cid) {
        return null;
    }

    @Override
    protected String parseCheck(String html) {
        return null;
    }

}
